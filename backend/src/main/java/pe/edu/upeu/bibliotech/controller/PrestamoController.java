package pe.edu.upeu.bibliotech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bibliotech.dto.PrestamoRequest;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Prestamo;
import pe.edu.upeu.bibliotech.service.PrestamoService;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listar() {
        return prestamoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo registrar(@RequestBody PrestamoRequest request) {
        if (request.estudianteId() == null || request.libroId() == null) {
            throw new ReglaNegocioException("estudianteId y libroId son obligatorios");
        }
        return prestamoService.registrarPrestamo(request.estudianteId(), request.libroId());
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolverLibro(id);
    }
}
