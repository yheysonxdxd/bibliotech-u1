package pe.edu.upeu.bibliotech.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.EstadoEstudiante;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.service.EstudianteService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · EstudianteService (RN-02)")
class EstudianteServiceTest {

    @Mock private EstudianteRepository estudianteRepository;
    @InjectMocks private EstudianteService estudianteService;

    @Test
    @DisplayName("Registrar estudiante: siempre queda ACTIVO aunque envíen INACTIVO")
    void registrar_quedaActivo() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Quispe");
        estudiante.setEstado(EstadoEstudiante.INACTIVO);
        when(estudianteRepository.existsByDni("70123456")).thenReturn(false);
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Estudiante guardado = estudianteService.registrar(estudiante);
        // Assert
        verify(estudianteRepository).save(estudiante);
        assertThat(guardado.getEstado()).isEqualTo(EstadoEstudiante.ACTIVO);
    }

    @Test
    @DisplayName("DNI duplicado es rechazado y nunca se guarda")
    void registrar_dniDuplicado() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Quispe");
        when(estudianteRepository.existsByDni("70123456")).thenReturn(true);
        // Act + Assert
        assertThatThrownBy(() -> estudianteService.registrar(estudiante))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un estudiante con el DNI");
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Listar estudiantes delega en el repositorio")
    void listar() {
        when(estudianteRepository.findAll()).thenReturn(List.of(new Estudiante("70123456", "Ana")));
        assertThat(estudianteService.listar()).hasSize(1);
    }
}
