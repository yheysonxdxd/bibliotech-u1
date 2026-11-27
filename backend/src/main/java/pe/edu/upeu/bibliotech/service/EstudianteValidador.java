package pe.edu.upeu.bibliotech.service;

import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Estudiante;

import java.util.ArrayList;
import java.util.List;

/** RN-02 (validaciones de formato): DNI obligatorio de 8 dígitos y nombre obligatorio. */
public class EstudianteValidador {

    public List<String> validar(Estudiante estudiante) {
        List<String> errores = new ArrayList<>();
        if (estudiante == null) {
            errores.add("Los datos del estudiante son obligatorios");
            return errores;
        }
        String dni = estudiante.getDni();
        if (dni == null || dni.isBlank()) {
            errores.add("El DNI es obligatorio");
        } else if (!dni.matches("\\d{8}")) {
            errores.add("El DNI debe tener 8 dígitos");
        }
        if (estudiante.getNombre() == null || estudiante.getNombre().isBlank()) {
            errores.add("El nombre es obligatorio");
        }
        return errores;
    }

    public void validarOLanzar(Estudiante estudiante) {
        List<String> errores = validar(estudiante);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException(errores);
        }
    }
}
