package pe.edu.upeu.bibliotech.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.EstadoEstudiante;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.service.EstudianteValidador;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RN-02 · Validación de estudiantes")
class EstudianteValidadorTest {

    private EstudianteValidador validador;

    @BeforeEach
    void setUp() {
        validador = new EstudianteValidador();
    }

    @Test
    @DisplayName("Estudiante válido: sin errores y estado inicial ACTIVO")
    void estudianteValido() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Quispe");
        // Act
        List<String> errores = validador.validar(estudiante);
        // Assert
        assertThat(errores).isEmpty();
        assertThat(estudiante.getEstado()).isEqualTo(EstadoEstudiante.ACTIVO);
    }

    @Test
    @DisplayName("DNI nulo es rechazado")
    void dniObligatorio() {
        // Arrange
        Estudiante estudiante = new Estudiante(null, "Ana Quispe");
        // Act
        List<String> errores = validador.validar(estudiante);
        // Assert
        assertThat(errores).containsExactly("El DNI es obligatorio");
    }

    @ParameterizedTest(name = "Borde: DNI [{0}] no tiene 8 dígitos")
    @ValueSource(strings = {"7012345", "701234567", "7012345A"})
    void dniConFormatoInvalido(String dni) {
        // Arrange
        Estudiante estudiante = new Estudiante(dni, "Ana Quispe");
        // Act
        List<String> errores = validador.validar(estudiante);
        // Assert
        assertThat(errores).containsExactly("El DNI debe tener 8 dígitos");
    }

    @Test
    @DisplayName("Nombre en blanco es rechazado")
    void nombreObligatorio() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "  ");
        // Act + Assert
        assertThat(validador.validar(estudiante)).containsExactly("El nombre es obligatorio");
        assertThatThrownBy(() -> validador.validarOLanzar(estudiante))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Estudiante nulo es rechazado")
    void estudianteNulo() {
        assertThat(validador.validar(null)).containsExactly("Los datos del estudiante son obligatorios");
    }

    @Test
    @DisplayName("DNI en blanco y nombre nulo acumulan dos errores")
    void dosErrores() {
        // Arrange
        Estudiante estudiante = new Estudiante(" ", null);
        // Act
        List<String> errores = validador.validar(estudiante);
        // Assert
        assertThat(errores).containsExactly("El DNI es obligatorio", "El nombre es obligatorio");
    }
}
