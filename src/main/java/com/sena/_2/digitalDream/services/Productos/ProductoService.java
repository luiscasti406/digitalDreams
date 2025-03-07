package com.sena._2.digitalDream.services.Productos;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sena._2.digitalDream.dtos.ProductoDTO;
import com.sena._2.digitalDream.modelos.Marca;
import com.sena._2.digitalDream.modelos.Producto;
import com.sena._2.digitalDream.repository.MarcaRepository;
import com.sena._2.digitalDream.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Value("${upload.dir.productos}")
    private String uploadDir;

    public String crearProducto(ProductoDTO productoDTO) {
        // Validar si la marca existe
        Optional<Marca> marcaOptional = marcaRepository.findById(productoDTO.getIdMarca());
        if (marcaOptional.isEmpty()) {
            return "Error: La marca especificada no existe.";
        }

        Producto producto = new Producto();

        // Manejo de imagen
        if (productoDTO.getFotoProducto() != null && !productoDTO.getFotoProducto().isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + productoDTO.getFotoProducto().getOriginalFilename();
            Path path = Path.of(uploadDir, fileName);

            try {
                Files.createDirectories(path.getParent());
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("png")) {
                    return "Error: Solo se permiten archivos .jpg y .png";
                }
                Files.copy(productoDTO.getFotoProducto().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                producto.setFotoProducto(fileName);
            } catch (IOException e) {
                return "Error al subir la imagen.";
            }
        }

        producto.setNomProducto(productoDTO.getNomProducto());
        producto.setMarca(marcaOptional.get());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setCantidad(productoDTO.getCantidad().shortValue());
        producto.setEstado(productoDTO.getEstado());
        producto.setDetalles(productoDTO.getDetalles());

        productoRepository.save(producto);
        return "Producto creado exitosamente.";
    }
    public List<ProductoDTO> obtenerProductosConMarcaYFoto() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .filter(producto -> producto.getMarca() != null) // Evita posibles errores si la marca es nula
                .map(producto -> {
                    String fotoUrl = (producto.getFotoProducto() != null && !producto.getFotoProducto().isEmpty())
                            ? producto.getFotoProducto()
                            : "/img/productos/imagen_default.jpg";

                    return new ProductoDTO(
                            producto.getIdProducto(),
                            producto.getNomProducto(),
                            producto.getMarca().getIdMarca(),
                            producto.getMarca().getMarca(),
                            producto.getPrecio(),
                            producto.getCantidad(),
                            producto.getEstado(),
                            producto.getDetalles(),
                            fotoUrl
                    );
                })
                .collect(Collectors.toList());
    }


    // Consultar producto
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    // Actualizar producto
    public String modificarProducto(int id, ProductoDTO productoDTO, MultipartFile fotoProducto) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            producto.setNomProducto(productoDTO.getNomProducto());
            producto.setPrecio(productoDTO.getPrecio());
            producto.setCantidad(productoDTO.getCantidad());
            producto.setEstado(productoDTO.getEstado());
            producto.setDetalles(productoDTO.getDetalles());

            Marca marca = marcaRepository.findById(productoDTO.getIdMarca())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
            producto.setMarca(marca);

            // Si se sube una nueva foto
            if (!fotoProducto.isEmpty()) {
                try {
                    String nombreImagen = System.currentTimeMillis() + "_" + fotoProducto.getOriginalFilename();
                    Path path = Paths.get("src/main/resources/static/img/productos/" + nombreImagen);
                    Files.copy(fotoProducto.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    if (producto.getFotoProducto() != null) {
                        Path rutaAnterior = Paths.get("src/main/resources/static/img/productos/" + producto.getFotoProducto());
                        Files.deleteIfExists(rutaAnterior);
                    }

                    producto.setFotoProducto(nombreImagen);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error al subir la imagen";
                }
            }
            productoRepository.save(producto);
            return "Producto actualizado con éxito";
        } else {
            return "Producto no encontrado";
        }
    }

    // Eliminar producto
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            if (producto.getFotoProducto() != null && !producto.getFotoProducto().isEmpty()) {
                Path rutaImagen = Paths.get(uploadDir + producto.getFotoProducto());
                try {
                    Files.deleteIfExists(rutaImagen);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            productoRepository.delete(producto);
        }
    }

    // Descargar lista de productos en Excel
    public void exportarExcel(HttpServletResponse response) throws IOException {
        List<Producto> productos = productoRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");
            String[] columnas = {"ID", "Nombre", "Precio", "Cantidad", "Estado", "Detalles"};

            // Estilo de encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(producto.getIdProducto());
                row.createCell(1).setCellValue(producto.getNomProducto());
                row.createCell(2).setCellValue(producto.getPrecio());
                row.createCell(3).setCellValue(producto.getCantidad());
                row.createCell(4).setCellValue(producto.getEstado());
                row.createCell(5).setCellValue(producto.getDetalles());
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=productos.xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    // Descargar lista de productos en PDF
    public void exportarPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Producto> productos = productoRepository.findAll();
        Document document = new Document();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=productos.pdf");
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Título
        com.itextpdf.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
        Paragraph title = new Paragraph("Lista de Productos", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Tabla
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f, 1f, 1f, 1f, 3f}); // Anchos de columnas

        com.itextpdf.text.Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(0, 102, 204);
        String[] columnas = {"ID", "Nombre", "Precio", "Cantidad", "Estado", "Detalles"};

        for (String columna : columnas) {
            PdfPCell cell = new PdfPCell(new Phrase(columna, fontHeader));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Producto producto : productos) {
            table.addCell(String.valueOf(producto.getIdProducto()));
            table.addCell(producto.getNomProducto());
            table.addCell(String.valueOf(producto.getPrecio()));
            table.addCell(String.valueOf(producto.getCantidad()));
            table.addCell(producto.getEstado());
            table.addCell(producto.getDetalles());
        }

        document.add(table);
        document.close();
    }

    // Subida de archivos CSV
    public void importarCSV(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] datos = line.split(",");
            if (datos.length >= 5) {
                Producto producto = new Producto();
                producto.setNomProducto(datos[0]);
                producto.setPrecio(Integer.parseInt(datos[1]));
                producto.setCantidad(Short.parseShort(datos[2]));
                producto.setEstado(datos[3]);
                producto.setDetalles(datos[4]);
                productoRepository.save(producto);
            }
        }
    }

    public Producto obtenerPorId(int id) {
        return productoRepository.findById(id).orElse(null);
    }

}

