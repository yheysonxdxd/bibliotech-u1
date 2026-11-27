package pe.edu.upeu.bibliotech.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bibliotech.exception.RecursoNoEncontradoException;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Libro;
import pe.edu.upeu.bibliotech.repository.LibroRepository;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final LibroValidador validador = new LibroValidador();

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El libro con id " + id + " no existe"));
    }

    /** RN-01: ISBN obligatorio y único; título y autor obligatorios; ejemplares >= 0. */
    @Transactional
    public Libro registrar(Libro libro) {
        validador.validarOLanzar(libro);
        String isbn = libro.getIsbn().trim();
        if (libroRepository.existsByIsbn(isbn)) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + isbn);
        }
        libro.setId(null);
        libro.setIsbn(isbn);
        libro.setTitulo(libro.getTitulo().trim());
        libro.setAutor(libro.getAutor().trim());
        return libroRepository.save(libro);
    }
}
