package com.sena._2.digitalDream.controller;

import com.itextpdf.text.DocumentException;
import com.sena._2.digitalDream.dtos.ProductoDTO;
import com.sena._2.digitalDream.modelos.Marca;
import com.sena._2.digitalDream.modelos.Producto;
import com.sena._2.digitalDream.repository.MarcaRepository;
import com.sena._2.digitalDream.repository.ProductoRepository;
import com.sena._2.digitalDream.services.Productos.ProductoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @GetMapping({"/","/Index"})
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        model.addAttribute("productos", productos);
        return "productos/index";
    }

    // Mostrar formulario de creación con la lista de marcas
    @GetMapping("/crear")
    public String mostrarFormulario(Model model) {
        List<Marca> marcas = marcaRepository.findAll();
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("marcas", marcas);
        return "productos/crear";
    }

    @PostMapping("/create")
    public String crearProducto(@ModelAttribute @Valid ProductoDTO productoDTO, Model model) {
        String mensaje = productoService.crearProducto(productoDTO);
        model.addAttribute("message", mensaje);
        return "redirect:/productos/";
    }

    @GetMapping("/modificar/{id}")
    public String mostrarFormularioModificar(@PathVariable("id") int id, Model model) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            ProductoDTO productoDTO = new ProductoDTO(producto);
            model.addAttribute("producto", productoDTO);

            // Cargar las marcas para el select
            List<Marca> marcas = marcaRepository.findAll();
            model.addAttribute("marcas", marcas);

            return "productos/modificar";
        } else {
            model.addAttribute("message", "Producto no encontrado");
            return "redirect:/productos";
        }
    }


    @PostMapping("/modificar/{id}")
    public String actualizarProducto(@PathVariable("id") int id,
                                     @ModelAttribute ProductoDTO productoDTO,
                                     @RequestParam(value = "fotoProducto", required = false) MultipartFile fotoProducto,
                                     Model model) {
        // Llamamos al servicio para actualizar el producto
        String mensaje = productoService.modificarProducto(id, productoDTO, fotoProducto);
        model.addAttribute("message", mensaje);
        return "redirect:/productos/";
    }




    @GetMapping("/eliminar")
    public String eliminarProducto(@RequestParam int id) {
        productoService.eliminarProducto(id);
        return "redirect:/productos/";
    }

    @GetMapping("/exportar/excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        productoService.exportarExcel(response);
    }

    @GetMapping("/exportar/pdf")
    public void exportarPdf(HttpServletResponse response) throws IOException, DocumentException {
        productoService.exportarPdf(response);
    }

    @PostMapping("/importar/csv")
    public String importarCSV(@RequestParam("file") MultipartFile file) throws IOException {
        productoService.importarCSV(file.getInputStream());
        return "redirect:/productos/Index";
    }
}

