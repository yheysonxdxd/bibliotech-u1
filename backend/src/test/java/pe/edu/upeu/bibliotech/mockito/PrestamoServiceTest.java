package pe.edu.upeu.bibliotech.mockito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.*;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.repository.LibroRepository;
import pe.edu.upeu.bibliotech.repository.PrestamoRepository;
import pe.edu.upeu.bibliotech.service.PrestamoService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PrestamoService con repositorios simulados (replica MatriculaServiceTest de la Sesión 3).
 * Caso exitoso con verify(save) y casos de rechazo con verify(never()).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · PrestamoService")
class PrestamoServiceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private LibroRepository libroRepository;
    @Mock private EstudianteRepository estudianteRepository;

    @InjectMocks private PrestamoService prestamoService;

    private Libro libro;
    private Estudiante estudiante;

    @BeforeEach
    void setUp() {
        libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 2);
        libro.setId(10L);
        estudiante = new Estudiante("70123456", "Ana Quispe");
        estudiante.setId(1L);
    }

    @Test
    @DisplayName("Registrar préstamo válido: guarda préstamo ACTIVO y baja un ejemplar")
    void registrarPrestamo_exitoso() {
        // Arrange
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Prestamo resultado = prestamoService.registrarPrestamo(1L, 10L);

        // Assert
        ArgumentCaptor<Prestamo> captor = ArgumentCaptor.forClass(Prestamo.class);
        verify(prestamoRepository).save(captor.capture());
        verify(libroRepository).save(libro);
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(captor.getValue().getEstudiante()).isSameAs(estudiante);
        assertThat(resultado.getFechaPrestamo()).isNotNull();
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rechazo: libro inexistente → no consulta estudiante ni guarda nada")
    void registrarPrestamo_libroInexistente() {
        // Arrange
        when(libroRepository.findById(10L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.registrarPrestamo(1L, 10L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(estudianteRepository, never()).findById(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo: libro sin ejemplares → nunca guarda préstamo ni libro")
    void registrarPrestamo_sinEjemplares() {
        // Arrange
        libro.setEjemplaresDisponibles(0);
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class);
        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo: estudiante inexistente → 404 y no guarda")
    void registrarPrestamo_estudianteInexistente() {
        // Arrange
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.registrarPrestamo(1L, 10L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El estudiante no existe");
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo: estudiante INACTIVO → no cuenta préstamos ni guarda")
    void registrarPrestamo_estudianteInactivo() {
        // Arrange
        estudiante.setEstado(EstadoEstudiante.INACTIVO);
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class);
        verify(prestamoRepository, never()).countByEstudianteIdAndEstado(any(), any());
        verify(prestamoRepository, never()).save(any());
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rechazo: 3 préstamos activos → nunca guarda y no baja ejemplares")
    void registrarPrestamo_limiteAlcanzado() {
        // Arrange
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(3L);
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.registrarPrestamo(1L, 10L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("3 préstamos activos");
        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(2);
    }

    @Test
    @DisplayName("Devolver préstamo ACTIVO: pasa a DEVUELTO y sube un ejemplar")
    void devolverLibro_exitoso() {
        // Arrange
        Prestamo prestamo = new Prestamo(estudiante, libro);
        prestamo.setId(5L);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(prestamo)).thenReturn(prestamo);
        // Act
        Prestamo resultado = prestamoService.devolverLibro(5L);
        // Assert
        verify(libroRepository).save(libro);
        verify(prestamoRepository).save(prestamo);
        assertThat(resultado.getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(libro.getEjemplaresDisponibles()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rechazo devolución: préstamo inexistente → nunca guarda")
    void devolverLibro_inexistente() {
        // Arrange
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.devolverLibro(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(prestamoRepository, never()).save(any());
        verifyNoInteractions(libroRepository);
    }

    @Test
    @DisplayName("Rechazo devolución: préstamo ya DEVUELTO → nunca guarda")
    void devolverLibro_yaDevuelto() {
        // Arrange
        Prestamo prestamo = new Prestamo(estudiante, libro);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        // Act + Assert
        assertThatThrownBy(() -> prestamoService.devolverLibro(5L))
                .isInstanceOf(ReglaNegocioException.class);
        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Listar préstamos delega en el repositorio")
    void listar() {
        // Arrange
        when(prestamoRepository.findAll()).thenReturn(List.of(new Prestamo(estudiante, libro)));
        // Act + Assert
        assertThat(prestamoService.listar()).hasSize(1);
        verify(prestamoRepository).findAll();
    }
}
