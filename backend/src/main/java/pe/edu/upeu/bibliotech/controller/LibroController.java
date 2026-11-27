package pe.edu.upeu.bibliotech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.service.LibroService;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<Libro> listar() {
        return libroService.listar();
    }

    @GetMapping("/{id}")
    public Libro buscar(@PathVariable Long id) {
        return libroService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro registrar(@RequestBody Libro libro) {
        return libroService.registrar(libro);
    }
}
