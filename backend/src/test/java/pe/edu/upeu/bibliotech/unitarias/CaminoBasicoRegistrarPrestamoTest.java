package pe.edu.upeu.bibliotech.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.*;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.repository.LibroRepository;
import pe.edu.upeu.bibliotech.repository.PrestamoRepository;
import pe.edu.upeu.bibliotech.service.PrestamoService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Método del camino básico · registrarPrestamo() (RN-03).
 * V(G) = 5  →  5 caminos independientes, un caso JUnit por camino (ver docs/camino-basico.md).
 */
@DisplayName("Camino básico · registrarPrestamo() · V(G)=5")
class CaminoBasicoRegistrarPrestamoTest {

    private LibroRepository libroRepo;
    private EstudianteRepository estudianteRepo;
    private PrestamoRepository prestamoRepo;
    private PrestamoService service;

    @BeforeEach
    void setUp() {
        libroRepo = mock(LibroRepository.class);
        estudianteRepo = mock(EstudianteRepository.class);
        prestamoRepo = mock(PrestamoRepository.class);
        service = new PrestamoService(prestamoRepo, libroRepo, estudianteRepo);
    }

    private Libro libro(int ejemplares) {
        Libro l = new Libro("9780132350884", "Clean Code", "Robert C. Martin", ejemplares);
        l.setId(10L);
        return l;
    }

    private Estudiante estudiante(EstadoEstudiante estado) {
        Estudiante e = new Estudiante("70123456", "Ana Quispe");
        e.setId(1L);
        e.setEstado(estado);
        return e;
    }

    @Test
    @DisplayName("C1 · 1-2-3-11 · D1=V: el libro no existe → 404")
    void camino1_libroNoExiste() {
        // Arrange
        when(libroRepo.findById(10L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> service.registrarPrestamo(1L, 10L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El libro no existe");
    }

    @Test
    @DisplayName("C2 · 1-2-4-5-11 · D2=V: libro sin ejemplares → rechazo")
    void camino2_sinEjemplares() {
        // Arrange
        when(libroRepo.findById(10L)).thenReturn(Optional.of(libro(0)));
        // Act + Assert
        assertThatThrownBy(() -> service.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El libro no tiene ejemplares disponibles");
    }

    @Test
    @DisplayName("C3 · 1-2-4-6-7-11 · D3=V: estudiante INACTIVO → rechazo")
    void camino3_estudianteInactivo() {
        // Arrange
        when(libroRepo.findById(10L)).thenReturn(Optional.of(libro(2)));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(estudiante(EstadoEstudiante.INACTIVO)));
        // Act + Assert
        assertThatThrownBy(() -> service.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El estudiante no está ACTIVO");
    }

    @Test
    @DisplayName("C4 · 1-2-4-6-8-9-11 · D4=V: ya tiene 3 préstamos activos → rechazo")
    void camino4_limiteDePrestamos() {
        // Arrange
        when(libroRepo.findById(10L)).thenReturn(Optional.of(libro(2)));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(estudiante(EstadoEstudiante.ACTIVO)));
        when(prestamoRepo.countByEstudianteIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(3L);
        // Act + Assert
        assertThatThrownBy(() -> service.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El estudiante ya tiene 3 préstamos activos");
    }

    @Test
    @DisplayName("C5 · 1-2-4-6-8-10-11 · todas F: préstamo registrado y baja un ejemplar")
    void camino5_prestamoExitoso() {
        // Arrange
        Libro libro = libro(2);
        when(libroRepo.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(estudiante(EstadoEstudiante.ACTIVO)));
        when(prestamoRepo.countByEstudianteIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(2L); // borde: 2 < 3
        when(prestamoRepo.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Prestamo prestamo = service.registrarPrestamo(1L, 10L);
        // Assert
        assertThat(prestamo.getEstado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(1);
    }
}
