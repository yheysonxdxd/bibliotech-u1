package pe.edu.upeu.bibliotech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    /** CA-01 y CA-02: ISBN obligatorio y único. */
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "ejemplares_disponibles", nullable = false)
    private Integer ejemplaresDisponibles;

    public Libro() { }

    public Libro(String isbn, String titulo, String autor, Integer ejemplaresDisponibles) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    /** RN-01 / CA-06 y CA-07: mayor que 0 = DISPONIBLE; igual a 0 = NO_DISPONIBLE. */
    public EstadoLibro getEstado() {
        return ejemplaresDisponibles != null && ejemplaresDisponibles > 0
                ? EstadoLibro.DISPONIBLE
                : EstadoLibro.NO_DISPONIBLE;
    }

    public void prestarEjemplar() { ejemplaresDisponibles--; }

    public void devolverEjemplar() { ejemplaresDisponibles++; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(Integer ejemplaresDisponibles) { this.ejemplaresDisponibles = ejemplaresDisponibles; }
}
