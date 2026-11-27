package pe.edu.upeu.bibliotech.integracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.bibliotech.controller.LibroController;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.service.LibroService;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** @WebMvcTest + MockMvc sobre el controlador de libros (Sesión 4): 200, 201, 400, 404. */
@WebMvcTest(LibroController.class)
@DisplayName("Controlador · /api/libros")
class LibroControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private LibroService libroService;

    private Libro libro(Long id, int ejemplares) {
        Libro l = new Libro("9780132350884", "Clean Code", "Robert C. Martin", ejemplares);
        l.setId(id);
        return l;
    }

    @Test
    @DisplayName("GET /api/libros → 200 con la lista y el estado calculado")
    void listar_200() throws Exception {
        // Arrange
        when(libroService.listar()).thenReturn(List.of(libro(1L, 3), libro(2L, 0)));
        // Act + Assert
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$[1].estado").value("NO_DISPONIBLE"));
    }

    @Test
    @DisplayName("POST /api/libros válido → 201")
    void registrar_201() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class))).thenReturn(libro(7L, 3));
        String body = objectMapper.writeValueAsString(Map.of(
                "isbn", "9780132350884", "titulo", "Clean Code",
                "autor", "Robert C. Martin", "ejemplaresDisponibles", 3));
        // Act + Assert
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    @DisplayName("POST /api/libros sin ISBN → 400 con mensaje")
    void registrar_400() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class)))
                .thenThrow(new ReglaNegocioException(List.of("El ISBN es obligatorio")));
        // Act + Assert
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Clean Code\",\"autor\":\"R. Martin\",\"ejemplaresDisponibles\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("El ISBN es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/libros con JSON malformado → 400 y no llama al servicio")
    void registrar_jsonMalformado_400() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es válido"));
        verify(libroService, never()).registrar(any());
    }

    @Test
    @DisplayName("GET /api/libros/{id} inexistente → 404")
    void buscar_404() throws Exception {
        // Arrange
        when(libroService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("El libro con id 99 no existe"));
        // Act + Assert
        mockMvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("no existe")));
    }

    @Test
    @DisplayName("GET /api/libros/{id} existente → 200")
    void buscar_200() throws Exception {
        when(libroService.buscarPorId(1L)).thenReturn(libro(1L, 3));
        mockMvc.perform(get("/api/libros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"));
    }
}
