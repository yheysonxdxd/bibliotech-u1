package pe.edu.upeu.bibliotech.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.bibliotech.controller.EstudianteController;
import pe.edu.upeu.bibliotech.controller.PrestamoController;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.model.Prestamo;
import pe.edu.upeu.bibliotech.service.EstudianteService;
import pe.edu.upeu.bibliotech.service.PrestamoService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PrestamoController.class, EstudianteController.class})
@DisplayName("Controlador · /api/prestamos y /api/estudiantes")
class PrestamoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PrestamoService prestamoService;
    @MockBean private EstudianteService estudianteService;

    @Test
    @DisplayName("PUT /api/prestamos/{id}/devolucion inexistente → 404")
    void devolver_404() throws Exception {
        when(prestamoService.devolverLibro(99L)).thenThrow(new RecursoNoEncontradoException("El préstamo no existe"));
        mockMvc.perform(put("/api/prestamos/99/devolucion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El préstamo no existe"));
    }

    @Test
    @DisplayName("POST /api/prestamos sin libroId → 400 y no llama al servicio")
    void registrar_sinIds_400() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{\"estudianteId\":1}"))
                .andExpect(status().isBadRequest());
        verify(prestamoService, never()).registrarPrestamo(any(), any());
    }

    @Test
    @DisplayName("POST /api/prestamos sin estudianteId → 400 y no llama al servicio")
    void registrar_sinEstudianteId_400() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{\"libroId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("estudianteId y libroId son obligatorios"));
        verify(prestamoService, never()).registrarPrestamo(any(), any());
    }

    @Test
    @DisplayName("GET /api/prestamos → 200 con la lista de préstamos")
    void listarPrestamos_200() throws Exception {
        Estudiante e = new Estudiante("70123456", "Ana Quispe");
        Libro l = new Libro("9786070000001", "Cien años de soledad", "García Márquez", 2);
        when(prestamoService.listar()).thenReturn(List.of(new Prestamo(e, l)));
        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[0].libro.titulo").value("Cien años de soledad"));
    }

    @Test
    @DisplayName("GET /api/estudiantes → 200 con la lista de estudiantes")
    void listarEstudiantes_200() throws Exception {
        when(estudianteService.listar()).thenReturn(List.of(
                new Estudiante("70123456", "Ana Quispe"),
                new Estudiante("70654321", "Luis Mamani")));
        mockMvc.perform(get("/api/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].dni").value("70654321"));
    }

    @Test
    @DisplayName("POST /api/estudiantes → 201 con estado ACTIVO")
    void registrarEstudiante_201() throws Exception {
        Estudiante e = new Estudiante("70123456", "Ana Quispe");
        e.setId(1L);
        when(estudianteService.registrar(any(Estudiante.class))).thenReturn(e);
        mockMvc.perform(post("/api/estudiantes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70123456\",\"nombre\":\"Ana Quispe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }
}
