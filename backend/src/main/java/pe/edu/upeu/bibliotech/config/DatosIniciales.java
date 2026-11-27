package pe.edu.upeu.bibliotech.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.bibliotech.model.EstadoEstudiante;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;
import pe.edu.upeu.bibliotech.repository.LibroRepository;

/** Datos de demostración (solo si bibliotech.datos-iniciales=true; en pruebas está desactivado). */
@Configuration
@ConditionalOnProperty(name = "bibliotech.datos-iniciales", havingValue = "true")
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarDatos(LibroRepository libros, EstudianteRepository estudiantes) {
        return args -> {
            if (libros.count() == 0) {
                libros.save(new Libro("9780132350884", "Clean Code", "Robert C. Martin", 3));
                libros.save(new Libro("9780134685991", "Effective Java", "Joshua Bloch", 2));
                libros.save(new Libro("9780201633610", "Design Patterns", "Gamma et al.", 0));
            }
            if (estudiantes.count() == 0) {
                estudiantes.save(new Estudiante("70123456", "Ana Quispe"));
                estudiantes.save(new Estudiante("70234567", "Luis Mamani"));
                Estudiante inactivo = new Estudiante("70345678", "Rosa Condori");
                inactivo.setEstado(EstadoEstudiante.INACTIVO);
                estudiantes.save(inactivo);
            }
        };
    }
}
