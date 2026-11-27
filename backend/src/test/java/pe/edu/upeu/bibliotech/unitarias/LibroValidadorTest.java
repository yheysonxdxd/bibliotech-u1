package pe.edu.upeu.bibliotech.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.EstadoLibro;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.service.LibroValidador;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RN-01 · Casos CP01–CP06 de la Sesión 1 convertidos en pruebas automáticas (patrón AAA).
 */
@DisplayName("RN-01 · Validación de libros")
class LibroValidadorTest {

    private LibroValidador validador;

    @BeforeEach
    void setUp() {
        validador = new LibroValidador();
    }

    @Test
    @DisplayName("CP01 · Libro con todos los datos válidos no tiene errores")
    void cp01_libroValido() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 3);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).isEmpty();
        assertThat(libro.getEstado()).isEqualTo(EstadoLibro.DISPONIBLE);
    }

    @ParameterizedTest(name = "CP02 · ISBN inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("CP02 · ISBN vacío o nulo es rechazado")
    void cp02_isbnObligatorio(String isbn) {
        // Arrange
        Libro libro = new Libro(isbn, "Clean Code", "Robert C. Martin", 3);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).containsExactly("El ISBN es obligatorio");
    }

    @Test
    @DisplayName("CP03 · Libro sin título es rechazado")
    void cp03_tituloObligatorio() {
        // Arrange
        Libro libro = new Libro("9780132350884", "", "Robert C. Martin", 3);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).containsExactly("El título es obligatorio");
    }

    @Test
    @DisplayName("CP04 · Libro sin autor es rechazado")
    void cp04_autorObligatorio() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", null, 3);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).containsExactly("El autor es obligatorio");
    }

    @Test
    @DisplayName("CP05 · Ejemplares negativos (borde -1) son rechazados")
    void cp05_ejemplaresNegativos() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", -1);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).containsExactly("Los ejemplares no pueden ser negativos");
    }

    @Test
    @DisplayName("CP06 · Borde: 0 ejemplares es válido y el estado es NO_DISPONIBLE")
    void cp06_ceroEjemplaresEsValidoYNoDisponible() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 0);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).isEmpty();
        assertThat(libro.getEstado()).isEqualTo(EstadoLibro.NO_DISPONIBLE);
    }

    @Test
    @DisplayName("Borde: 1 ejemplar es DISPONIBLE")
    void unEjemplarEsDisponible() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 1);
        // Act
        EstadoLibro estado = libro.getEstado();
        // Assert
        assertThat(estado).isEqualTo(EstadoLibro.DISPONIBLE);
    }

    @Test
    @DisplayName("Ejemplares nulos son rechazados")
    void ejemplaresNulos() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", null);
        // Act
        List<String> errores = validador.validar(libro);
        // Assert
        assertThat(errores).containsExactly("Los ejemplares son obligatorios");
        assertThat(libro.getEstado()).isEqualTo(EstadoLibro.NO_DISPONIBLE);
    }

    @Test
    @DisplayName("Libro vacío acumula todos los errores y validarOLanzar lanza excepción")
    void libroVacioAcumulaErrores() {
        // Arrange
        Libro libro = new Libro();
        // Act + Assert
        assertThat(validador.validar(libro)).hasSize(4);
        assertThatThrownBy(() -> validador.validarOLanzar(libro))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El ISBN es obligatorio");
    }

    @Test
    @DisplayName("Libro nulo es rechazado")
    void libroNulo() {
        // Act
        List<String> errores = validador.validar(null);
        // Assert
        assertThat(errores).containsExactly("Los datos del libro son obligatorios");
    }

    @Test
    @DisplayName("validarOLanzar no lanza nada con un libro válido")
    void validarOLanzarConLibroValido() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 3);
        // Act + Assert
        assertThatCode(() -> validador.validarOLanzar(libro)).doesNotThrowAnyException();
    }
}
