package pe.edu.upeu.bibliotech.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.EstadoEstudiante;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final EstudianteValidador validador = new EstudianteValidador();

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listar() {
        return estudianteRepository.findAll();
    }

    /** RN-02: DNI obligatorio y único; nombre obligatorio; estado inicial ACTIVO. */
    @Transactional
    public Estudiante registrar(Estudiante estudiante) {
        validador.validarOLanzar(estudiante);
        if (estudianteRepository.existsByDni(estudiante.getDni())) {
            throw new ReglaNegocioException("Ya existe un estudiante con el DNI " + estudiante.getDni());
        }
        estudiante.setId(null);
        estudiante.setNombre(estudiante.getNombre().trim());
        estudiante.setEstado(EstadoEstudiante.ACTIVO);
        return estudianteRepository.save(estudiante);
    }
}
