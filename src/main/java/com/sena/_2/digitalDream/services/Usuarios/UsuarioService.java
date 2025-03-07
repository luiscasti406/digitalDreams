package com.sena._2.digitalDream.services.Usuarios;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Rol;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.RolRepository;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Se inyecta correctamente

    @Autowired
    private RolRepository rolRepository;

    @Value("${upload.dir.usuarios}")
    private String uploadDir;

    public String crearUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            return "Error: El correo ya está registrado";
        }

        Usuario usuario = new Usuario();

        if (usuarioDTO.getFotoUsuario() != null && !usuarioDTO.getFotoUsuario().isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + usuarioDTO.getFotoUsuario().getOriginalFilename();
            Path path = Path.of(uploadDir, fileName);

            try {
                Files.createDirectories(path.getParent());
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("png")) {
                    return "Error: Solo se permiten archivos .jpg y .png";
                }
                Files.copy(usuarioDTO.getFotoUsuario().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                usuario.setFotoUsuario(fileName);
            } catch (IOException e) {
                return "Error al subir la imagen.";
            }
        }

        usuario.setPrim_nombre(usuarioDTO.getPrim_nombre());
        usuario.setSeg_nombre(usuarioDTO.getSeg_nombre());
        usuario.setPrim_apellido(usuarioDTO.getPrim_apellido());
        usuario.setSeg_apellido(usuarioDTO.getSeg_apellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));

        Rol rol = rolRepository.findById(usuarioDTO.getId_rol()).orElse(null);
        usuario.setRol(rol);

        usuarioRepository.save(usuario);
        return "Usuario creado correctamente";
    }


    // Obtener todos los usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por correo
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Obtener usuario por ID
    public Optional<Usuario> obtenerUsuarioPorId(int id) {
        return usuarioRepository.findById(id);
    }

    // Actualizar usuario
    public Usuario actualizarUsuario(int id, UsuarioDTO usuarioDTO, MultipartFile fotoUsuario) throws IOException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();

            usuarioExistente.setPrim_nombre(usuarioDTO.getPrim_nombre());
            usuarioExistente.setSeg_nombre(usuarioDTO.getSeg_nombre());
            usuarioExistente.setPrim_apellido(usuarioDTO.getPrim_apellido());
            usuarioExistente.setSeg_apellido(usuarioDTO.getSeg_apellido());
            usuarioExistente.setEmail(usuarioDTO.getEmail());

            if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            }

            if (usuarioDTO.getId_rol() > 0) {
                Rol rol = new Rol();
                rol.setIdRol(usuarioDTO.getId_rol());
                usuarioExistente.setRol(rol);
            }

            if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                if (usuarioExistente.getFotoUsuario() != null) {
                    Path rutaAnterior = Path.of(uploadDir, usuarioExistente.getFotoUsuario());
                    Files.deleteIfExists(rutaAnterior);
                }

                String fileName = System.currentTimeMillis() + "_" + fotoUsuario.getOriginalFilename();
                Path path = Path.of(uploadDir, fileName);
                Files.copy(fotoUsuario.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                usuarioExistente.setFotoUsuario(fileName);
            }

            return usuarioRepository.save(usuarioExistente);
        }
        return null;
    }

    // Eliminar usuario y su imagen
    public void eliminarUsuario(int id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            if (usuario.getFotoUsuario() != null && !usuario.getFotoUsuario().isEmpty()) {
                Path rutaImagen = Paths.get(uploadDir + usuario.getFotoUsuario());
                try {
                    Files.deleteIfExists(rutaImagen);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            usuarioRepository.delete(usuario);
        }
    }

    // Exportar lista de usuarios a Excel
    public void exportarExcel(HttpServletResponse response) throws IOException {
        List<Usuario> usuarios = usuarioRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            String[] columnas = {"ID", "Primer Nombre", "Segundo Nombre", "Primer Apellido", "Segundo Apellido", "Email", "Rol"};

            // Estilo de encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezado
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos de usuarios
            int rowNum = 1;
            for (Usuario usuario : usuarios) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getIdUsuario());
                row.createCell(1).setCellValue(usuario.getPrim_nombre());
                row.createCell(2).setCellValue(usuario.getSeg_nombre());
                row.createCell(3).setCellValue(usuario.getPrim_apellido());
                row.createCell(4).setCellValue(usuario.getSeg_apellido());
                row.createCell(5).setCellValue(usuario.getEmail());
                row.createCell(6).setCellValue(usuario.getRol() != null ? usuario.getRol().getTipoRol() : "Sin Rol");

            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=usuarios.xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    // Exportar lista de usuarios a PDF
    public void exportarPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Usuario> usuarios = usuarioRepository.findAll();
        Document document = new Document();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=usuarios.pdf");
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Título
        com.itextpdf.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
        Paragraph title = new Paragraph("Lista de Usuarios", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Tabla
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 3f, 2f});

        com.itextpdf.text.Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(0, 102, 204);
        String[] columnas = {"ID", "Primer Nombre", "Segundo Nombre", "Primer Apellido", "Segundo Apellido", "Email", "Rol"};

        for (String columna : columnas) {
            PdfPCell cell = new PdfPCell(new Phrase(columna, fontHeader));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Usuario usuario : usuarios) {
            table.addCell(String.valueOf(usuario.getIdUsuario()));
            table.addCell(usuario.getPrim_nombre());
            table.addCell(usuario.getSeg_nombre());
            table.addCell(usuario.getPrim_apellido());
            table.addCell(usuario.getSeg_apellido());
            table.addCell(usuario.getEmail());
            table.addCell(usuario.getRol() != null ? usuario.getRol().getTipoRol() : "Sin Rol");
        }

        document.add(table);
        document.close();
    }

}
