package com.sena._2.digitalDream.controller;

import com.sena._2.digitalDream.dtos.PedidoDTO;
import com.sena._2.digitalDream.modelos.Marca;
import com.sena._2.digitalDream.modelos.Pedido;
import com.sena._2.digitalDream.repository.MarcaRepository;
import com.sena._2.digitalDream.services.Pedidos.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    private final MarcaRepository marcaRepository;

    // Página para crear un nuevo pedido
    @GetMapping("/crear")
    public String mostrarFormularioDeCreacion(Model model) {
        model.addAttribute("pedido", new PedidoDTO());
        model.addAttribute("marcas", marcaRepository.findAll());
        return "pedidos/crear_pedido";
    }


    // Crear un nuevo pedido
    @PostMapping("/crear")
    public String crearPedido(@ModelAttribute @Valid PedidoDTO pedidoDTO, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("marcas", marcaRepository.findAll());
            return "pedidos/crear_pedido";
        }

        if (pedidoDTO.getIdMarca() == null) {
            result.rejectValue("idMarca", "error.idMarca", "Debe seleccionar una marca.");
            model.addAttribute("marcas", marcaRepository.findAll());
            return "pedidos/crear_pedido";
        }

        pedidoService.crearPedido(pedidoDTO);

        return "redirect:/pedidos/";
    }


    // Obtener todos los pedidos
    @GetMapping({"/", "lista"})
    public String obtenerTodosLosPedidos(Model model) {
        try {
            List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
            model.addAttribute("pedidos", pedidos);
        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error al obtener los pedidos.");
        }
        return "pedidos/lista_pedidos";
    }


    // Página para modificar un pedido
    @GetMapping("/modificar/{id}")
    public String mostrarFormularioDeModificacion(@PathVariable int id, Model model) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        List<Marca> marcas = marcaRepository.findAll();

        if (pedido == null) {
            return "redirect:/pedidos";
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("marcas", marcas);

        return "pedidos/modificar_pedido";
    }


    // Modificar un pedido
    @PostMapping("/modificar/{id}")
    public String modificarPedido(@PathVariable int id, @ModelAttribute Pedido pedidoDetalles,
                                  RedirectAttributes redirectAttributes) {
        Pedido pedidoModificado = pedidoService.modificarPedido(id, pedidoDetalles);
        if (pedidoModificado != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Pedido modificado exitosamente!");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró el pedido.");
        }
        return "redirect:/pedidos/";
    }

    // Cancelar un pedido
    @GetMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable int id, Model model) {
        boolean cancelado = pedidoService.cancelarPedido(id);
        if (cancelado) {
            model.addAttribute("mensaje", "Pedido cancelado exitosamente!");
        } else {
            model.addAttribute("mensaje", "Pedido no encontrado.");
        }
        return "redirect:/pedidos/";
    }

    // Obtener detalles de un pedido específico
    @GetMapping("/{id}")
    public String obtenerPedidoPorId(@PathVariable int id, Model model) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        if (pedido != null) {
            model.addAttribute("pedido", pedido);
            return "pedidos/detalle_pedido";
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/notificar/{id}")
    public String notificarEntrega(@PathVariable int id, Model model) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        if (pedido != null) {
            String correoUsuario = pedido.getUsuario().getEmail();
            String nombreUsuario = pedido.getUsuario().getPrim_nombre();
            String fechaLlegada = pedido.getFechaLlegada().toString();
            String linkSeguimiento = "http://mispedidos.com/seguimiento/" + id;

            pedidoService.enviarNotificacionPedido(correoUsuario, nombreUsuario, pedido.getIdPedido(), fechaLlegada, linkSeguimiento);
            model.addAttribute("mensaje", "Notificación de entrega enviada.");
            return "redirect:/pedidos";
        }
        return "redirect:/pedidos";
    }
}

