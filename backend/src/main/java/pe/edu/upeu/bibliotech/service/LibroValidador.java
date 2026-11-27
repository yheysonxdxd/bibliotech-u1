package pe.edu.upeu.bibliotech.service;

import pe.edu.upeu.bibliotech.exception.ReglaNegocioException;
import pe.edu.upeu.bibliotech.model.Libro;

import java.util.ArrayList;
import java.util.List;

/**
 * RN-01 (validaciones de formato). La unicidad del ISBN la comprueba LibroService
 * porque necesita consultar el repositorio.
 */
public class LibroValidador {

    public List<String> validar(Libro libro) {
        List<String> errores = new ArrayList<>();
        if (libro == null) {
            errores.add("Los datos del libro son obligatorios");
            return errores;
        }
        if (vacio(libro.getIsbn())) {
            errores.add("El ISBN es obligatorio");
        }
        if (vacio(libro.getTitulo())) {
            errores.add("El título es obligatorio");
        }
        if (vacio(libro.getAutor())) {
            errores.add("El autor es obligatorio");
        }
        if (libro.getEjemplaresDisponibles() == null) {
            errores.add("Los ejemplares son obligatorios");
        } else if (libro.getEjemplaresDisponibles() < 0) {
            errores.add("Los ejemplares no pueden ser negativos");
        }
        return errores;
    }

    public void validarOLanzar(Libro libro) {
        List<String> errores = validar(libro);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException(errores);
        }
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
