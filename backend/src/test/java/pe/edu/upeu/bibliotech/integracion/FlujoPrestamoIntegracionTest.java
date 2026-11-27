package pe.edu.upeu.bibliotech.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.bibliotech.model.EstadoEstudiante;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.repository.LibroRepository;
import pe.edu.upeu.bibliotech.repository.PrestamoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @SpringBootTest del flujo completo (Sesión 4) con base de datos de prueba H2.
 * Cada prueba parte de datos controlados que se crean en @BeforeEach.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integración · flujo completo de préstamo")
class FlujoPrestamoIntegracionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LibroRepository libroRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PrestamoRepository prestamoRepository;

    private Long estudianteId;

    @BeforeEach
    void datosControlados() {
        prestamoRepository.deleteAll();
        libroRepository.deleteAll();
        estudianteRepository.deleteAll();
        estudianteId = estudianteRepository.save(new Estudiante("70123456", "Ana Quispe")).getId();
    }

    private Long crearLibro(String isbn, int ejemplares) {
        return libroRepository.save(new Libro(isbn, "Libro " + isbn, "Autor", ejemplares)).getId();
    }

    private String prestamoJson(Long estId, Long libroId) {
        return "{\"estudianteId\":" + estId + ",\"libroId\":" + libroId + "}";
    }

    private int ejemplares(Long libroId) {
        return libroRepository.findById(libroId).orElseThrow().getEjemplaresDisponibles();
    }

    @Test
    @DisplayName("Registrar préstamo baja un ejemplar y devolver lo restituye")
    void prestarYDevolver() throws Exception {
        // Arrange
        Long libroId = crearLibro("9780132350884", 1);

        // Act 1: prestar
        String respuesta = mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON).content(prestamoJson(estudianteId, libroId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.libro.estado").value("NO_DISPONIBLE"))
                .andReturn().getResponse().getContentAsString();
        Long prestamoId = objectMapper.readTree(respuesta).get("id").asLong();

        // Assert 1
        assertThat(ejemplares(libroId)).isZero();

        // Act 2: devolver
        mockMvc.perform(put("/api/prestamos/{id}/devolucion", prestamoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));

        // Assert 2
        assertThat(ejemplares(libroId)).isEqualTo(1);

        // Devolver dos veces se rechaza
        mockMvc.perform(put("/api/prestamos/{id}/devolucion", prestamoId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sin ejemplares el préstamo se rechaza (400) y no se crea nada")
    void sinEjemplaresSeRechaza() throws Exception {
        // Arrange
        Long libroId = crearLibro("9780134685991", 0);
        // Act + Assert
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON).content(prestamoJson(estudianteId, libroId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El libro no tiene ejemplares disponibles"));
        assertThat(prestamoRepository.count()).isZero();
        assertThat(ejemplares(libroId)).isZero();
    }

    @Test
    @DisplayName("El cuarto préstamo activo se rechaza (máximo 3)")
    void maximoTresPrestamos() throws Exception {
        // Arrange
        Long libroId = crearLibro("9780201633610", 10);
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/prestamos")
                            .contentType(MediaType.APPLICATION_JSON).content(prestamoJson(estudianteId, libroId)))
                    .andExpect(status().isCreated());
        }
        // Act + Assert
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON).content(prestamoJson(estudianteId, libroId)))
                .andExpect(status().isBadRequest());
        assertThat(ejemplares(libroId)).isEqualTo(7);
    }

    @Test
    @DisplayName("Estudiante INACTIVO no puede prestar")
    void estudianteInactivo() throws Exception {
        // Arrange
        Estudiante inactivo = new Estudiante("70999999", "Rosa Condori");
        inactivo.setEstado(EstadoEstudiante.INACTIVO);
        Long inactivoId = estudianteRepository.save(inactivo).getId();
        Long libroId = crearLibro("9780321125217", 2);
        // Act + Assert
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON).content(prestamoJson(inactivoId, libroId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El estudiante no está ACTIVO"));
    }

    @Test
    @DisplayName("Registrar libro y estudiante por la API; duplicados → 400")
    void registroYUnicidad() throws Exception {
        String libro = "{\"isbn\":\"9781617294945\",\"titulo\":\"Spring in Action\",\"autor\":\"Craig Walls\",\"ejemplaresDisponibles\":2}";
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(libro))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(libro))
                .andExpect(status().isBadRequest());

        String estudiante = "{\"dni\":\"70123456\",\"nombre\":\"Otra persona\"}";
        mockMvc.perform(post("/api/estudiantes").contentType(MediaType.APPLICATION_JSON).content(estudiante))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un estudiante con el DNI 70123456"));

        JsonNode lista = objectMapper.readTree(mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(lista).hasSize(1);
    }
}
