import { Routes } from '@angular/router';
import { LibrosComponent } from './libros/libros.component';
import { PrestamosComponent } from './prestamos/prestamos.component';

export const routes: Routes = [
  { path: '', redirectTo: 'libros', pathMatch: 'full' },
  { path: 'libros', component: LibrosComponent },
  { path: 'prestamos', component: PrestamosComponent },
];
