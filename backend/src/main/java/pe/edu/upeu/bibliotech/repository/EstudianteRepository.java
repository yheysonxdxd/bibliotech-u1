package pe.edu.upeu.bibliotech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bibliotech.model.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    boolean existsByDni(String dni);
}
