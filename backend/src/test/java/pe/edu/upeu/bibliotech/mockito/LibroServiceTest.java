package pe.edu.upeu.bibliotech.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.repository.LibroRepository;
import pe.edu.upeu.bibliotech.service.LibroService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · LibroService (unicidad del ISBN)")
class LibroServiceTest {

    @Mock private LibroRepository libroRepository;
    @InjectMocks private LibroService libroService;

    @Test
    @DisplayName("Registrar libro nuevo: limpia espacios y guarda")
    void registrar_exitoso() {
        // Arrange
        Libro libro = new Libro(" 9780132350884 ", " Clean Code ", " Robert C. Martin ", 3);
        when(libroRepository.existsByIsbn("9780132350884")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Libro guardado = libroService.registrar(libro);
        // Assert
        verify(libroRepository).save(libro);
        assertThat(guardado.getIsbn()).isEqualTo("9780132350884");
        assertThat(guardado.getTitulo()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("CA-02 · ISBN duplicado es rechazado y nunca se guarda")
    void registrar_isbnDuplicado() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 3);
        when(libroRepository.existsByIsbn("9780132350884")).thenReturn(true);
        // Act + Assert
        assertThatThrownBy(() -> libroService.registrar(libro))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe un libro con el ISBN");
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Datos inválidos: ni siquiera consulta la unicidad")
    void registrar_datosInvalidos() {
        // Arrange
        Libro libro = new Libro("", "Clean Code", "Robert C. Martin", -1);
        // Act + Assert
        assertThatThrownBy(() -> libroService.registrar(libro)).isInstanceOf(ReglaNegocioException.class);
        verifyNoInteractions(libroRepository);
    }

    @Test
    @DisplayName("Listar y buscar por id")
    void listarYBuscar() {
        // Arrange
        Libro libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", 3);
        when(libroRepository.findAll()).thenReturn(List.of(libro));
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.findById(2L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThat(libroService.listar()).containsExactly(libro);
        assertThat(libroService.buscarPorId(1L)).isSameAs(libro);
        assertThatThrownBy(() -> libroService.buscarPorId(2L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
