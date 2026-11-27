package pe.edu.upeu.bibliotech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.service.EstudianteService;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public List<Estudiante> listar() {
        return estudianteService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante registrar(@RequestBody Estudiante estudiante) {
        return estudianteService.registrar(estudiante);
    }
}
