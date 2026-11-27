package pe.edu.upeu.bibliotech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bibliotech.model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    boolean existsByIsbn(String isbn);
}
