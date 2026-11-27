package pe.edu.upeu.bibliotech.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.*;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.repository.LibroRepository;
import pe.edu.upeu.bibliotech.repository.PrestamoRepository;

import java.util.List;

@Service
public class PrestamoService {

    public static final int MAXIMO_PRESTAMOS_ACTIVOS = 3;

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final EstudianteRepository estudianteRepository;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           LibroRepository libroRepository,
                           EstudianteRepository estudianteRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    /**
     * RN-03. Decisiones analizadas en el camino básico (docs/camino-basico.md):
     * D1 ¿el libro existe?  D2 ¿tiene ejemplares?  D3 ¿estudiante ACTIVO?  D4 ¿menos de 3 activos?
     * V(G) = 4 decisiones + 1 = 5.
     */
    @Transactional
    public Prestamo registrarPrestamo(Long estudianteId, Long libroId) {
        Libro libro = libroRepository.findById(libroId).orElse(null);
        if (libro == null) {                                                          // D1
            throw new RecursoNoEncontradoException("El libro no existe");
        }
        if (libro.getEjemplaresDisponibles() <= 0) {                                  // D2
            throw new ReglaNegocioException("El libro no tiene ejemplares disponibles");
        }
        Estudiante estudiante = buscarEstudiante(estudianteId);
        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {                      // D3
            throw new ReglaNegocioException("El estudiante no está ACTIVO");
        }
        long activos = prestamoRepository.countByEstudianteIdAndEstado(estudianteId, EstadoPrestamo.ACTIVO);
        if (activos >= MAXIMO_PRESTAMOS_ACTIVOS) {                                    // D4
            throw new ReglaNegocioException("El estudiante ya tiene 3 préstamos activos");
        }
        libro.prestarEjemplar();
        libroRepository.save(libro);
        return prestamoRepository.save(new Prestamo(estudiante, libro));
    }

    /**
     * RN-04. Decisiones: D1 ¿el préstamo existe?  D2 ¿está ACTIVO?   V(G) = 2 + 1 = 3.
     */
    @Transactional
    public Prestamo devolverLibro(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);
        if (prestamo == null) {                                                       // D1
            throw new RecursoNoEncontradoException("El préstamo no existe");
        }
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {                          // D2
            throw new ReglaNegocioException("El préstamo ya fue devuelto");
        }
        Libro libro = prestamo.getLibro();
        libro.devolverEjemplar();
        libroRepository.save(libro);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        return prestamoRepository.save(prestamo);
    }

    /** Método auxiliar (V(G) = 2): se analiza aparte para que registrarPrestamo() conserve D1..D4. */
    private Estudiante buscarEstudiante(Long estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El estudiante no existe"));
    }
}
