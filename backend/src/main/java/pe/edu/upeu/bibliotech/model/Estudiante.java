package pe.edu.upeu.bibliotech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    /** Regla "DNI obligatorio y único" (Sesión 1). */
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoEstudiante estado = EstadoEstudiante.ACTIVO;

    public Estudiante() { }

    public Estudiante(String dni, String nombre) {
        this.dni = dni;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public EstadoEstudiante getEstado() { return estado; }
    public void setEstado(EstadoEstudiante estado) { this.estado = estado; }
}
