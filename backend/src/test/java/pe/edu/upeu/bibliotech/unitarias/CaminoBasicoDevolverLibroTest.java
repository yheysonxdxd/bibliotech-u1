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
 * Método del camino básico · devolverLibro() (RN-04).
 * V(G) = 3  →  3 caminos independientes (ver docs/camino-basico.md).
 */
@DisplayName("Camino básico · devolverLibro() · V(G)=3")
class CaminoBasicoDevolverLibroTest {

    private PrestamoRepository prestamoRepo;
    private PrestamoService service;

    @BeforeEach
    void setUp() {
        prestamoRepo = mock(PrestamoRepository.class);
        service = new PrestamoService(prestamoRepo, mock(LibroRepository.class), mock(EstudianteRepository.class));
    }

    private Prestamo prestamo(EstadoPrestamo estado, Libro libro) {
        Prestamo p = new Prestamo(new Estudiante("70123456", "Ana Quispe"), libro);
        p.setId(5L);
        p.setEstado(estado);
        return p;
    }

    @Test
    @DisplayName("C1 · 1-2-3-7 · D1=V: el préstamo no existe → 404")
    void camino1_prestamoNoExiste() {
        // Arrange
        when(prestamoRepo.findById(99L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> service.devolverLibro(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El préstamo no existe");
    }

    @Test
    @DisplayName("C2 · 1-2-4-5-7 · D2=V: el préstamo ya fue DEVUELTO → rechazo")
    void camino2_yaDevuelto() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "R. Martin", 1);
        when(prestamoRepo.findById(5L)).thenReturn(Optional.of(prestamo(EstadoPrestamo.DEVUELTO, libro)));
        // Act + Assert
        assertThatThrownBy(() -> service.devolverLibro(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El préstamo ya fue devuelto");
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(1);
    }

    @Test
    @DisplayName("C3 · 1-2-4-6-7 · D1=F, D2=F: devolución exitosa, sube un ejemplar")
    void camino3_devolucionExitosa() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "R. Martin", 0);
        when(prestamoRepo.findById(5L)).thenReturn(Optional.of(prestamo(EstadoPrestamo.ACTIVO, libro)));
        when(prestamoRepo.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Prestamo devuelto = service.devolverLibro(5L);
        // Assert
        assertThat(devuelto.getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(1);
        assertThat(libro.getEstado()).isEqualTo(EstadoLibro.DISPONIBLE);
    }
}
