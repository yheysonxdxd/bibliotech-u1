export type EstadoLibro = 'DISPONIBLE' | 'NO_DISPONIBLE';
export type EstadoEstudiante = 'ACTIVO' | 'INACTIVO';
export type EstadoPrestamo = 'ACTIVO' | 'DEVUELTO';

export interface Libro {
  id?: number;
  isbn: string;
  titulo: string;
  autor: string;
  ejemplaresDisponibles: number;
  estado?: EstadoLibro;
}

export interface Estudiante {
  id?: number;
  dni: string;
  nombre: string;
  estado?: EstadoEstudiante;
}

export interface Prestamo {
  id: number;
  fechaPrestamo: string;
  estado: EstadoPrestamo;
  estudiante: Estudiante;
  libro: Libro;
}

/** Cuerpo de error que devuelve el backend (ManejadorErrores). */
export interface ErrorApi {
  status: number;
  mensaje: string;
  errores: string[];
}
