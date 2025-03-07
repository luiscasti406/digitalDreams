package com.sena._2.digitalDream.services.Resivos;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sena._2.digitalDream.modelos.Producto;
import com.sena._2.digitalDream.modelos.Recibo;
import com.sena._2.digitalDream.modelos.ReciboProducto;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.ProductoRepository;
import com.sena._2.digitalDream.repository.ReciboProductoRepository;
import com.sena._2.digitalDream.repository.ReciboRepository;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciboService {

    private final ReciboRepository reciboRepository;
    private final ReciboProductoRepository reciboProductoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Recibo confirmarCompra(int idUsuario, List<Integer> productosSeleccionados, List<Integer> cantidades, int tipoPago, String numeroTarjeta) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el recibo
        Recibo nuevoRecibo = new Recibo();
        nuevoRecibo.setUsuario(usuario);
        nuevoRecibo.setTipoPago(tipoPago);
        nuevoRecibo.setNumeroTarjeta(numeroTarjeta);
        nuevoRecibo = reciboRepository.save(nuevoRecibo);

        int totalPagar = 0;

        // Actualizar stock dentro de la transacción
        for (int i = 0; i < productosSeleccionados.size(); i++) {
            Integer idProducto = productosSeleccionados.get(i);
            Integer cantidad = cantidades.get(i);

            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNomProducto());
            }

            producto.setCantidad((short) (producto.getCantidad() - cantidad));
            productoRepository.save(producto);

            ReciboProducto rp = new ReciboProducto(nuevoRecibo, producto, cantidad);
            reciboProductoRepository.save(rp);

            totalPagar += producto.getPrecio() * cantidad;
        }

        nuevoRecibo.setValorTotal(totalPagar);
        nuevoRecibo = reciboRepository.save(nuevoRecibo);

        return nuevoRecibo;
    }


    @Transactional
    public void cancelarCompra(int idRecibo) {
        reciboProductoRepository.deleteByReciboIdRecibo(idRecibo);
        reciboRepository.deleteById(idRecibo);
    }

    public List<Recibo> consultarComprasPasadas(int idUsuario) {
        return reciboRepository.findByUsuarioIdUsuario(idUsuario);
    }


    public byte[] generarReciboPDF(int idRecibo) {
        Recibo recibo = reciboRepository.findById(idRecibo)
                .orElseThrow(() -> new RuntimeException("Recibo no encontrado"));
        List<ReciboProducto> productos = reciboProductoRepository.findByReciboIdRecibo(idRecibo);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("DigitalDreams - Recibo de Compra", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph("\n"));

            // Formateo de fecha con DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaCompraFormateada = recibo.getFechaCompra().format(formatter);

            document.add(new Paragraph("Número de Recibo: " + recibo.getIdRecibo()));
            document.add(new Paragraph("Fecha: " + fechaCompraFormateada));
            document.add(new Paragraph("Cliente: " + recibo.getUsuario().getPrim_nombre() + " " + recibo.getUsuario().getPrim_apellido()));
            document.add(new Paragraph("Correo: " + recibo.getUsuario().getEmail()));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1, 1});
            table.addCell("Producto");
            table.addCell("Cantidad");
            table.addCell("Precio Unitario");
            table.addCell("Total");

            int totalPagar = 0;
            for (ReciboProducto rp : productos) {
                table.addCell(rp.getProducto().getNomProducto());
                table.addCell(String.valueOf(rp.getCantidad()));
                table.addCell("$" + rp.getProducto().getPrecio());
                int totalProducto = rp.getCantidad() * rp.getProducto().getPrecio();
                table.addCell("$" + totalProducto);
                totalPagar += totalProducto;
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Total a pagar: $" + totalPagar));
            document.add(new Paragraph("Método de pago: " + (recibo.getTipoPago() == 1 ? "Tarjeta" : "Efectivo")));
            document.add(new Paragraph("\n"));

            Paragraph footer = new Paragraph("Gracias por su compra en DigitalDreams.\nPolítica de devoluciones: No se aceptan devoluciones después de 7 días.", new Font(Font.FontFamily.HELVETICA, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
}
