package pe.edu.upeu.bibliotech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bibliotech.model.EstadoPrestamo;
import pe.edu.upeu.bibliotech.model.Prestamo;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    long countByEstudianteIdAndEstado(Long estudianteId, EstadoPrestamo estado);
}
