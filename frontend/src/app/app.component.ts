import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header>
      <h1>BiblioTech</h1>
      <nav>
        <a routerLink="/libros" routerLinkActive="activo" data-cy="nav-libros">Catálogo de libros</a>
        <a routerLink="/prestamos" routerLinkActive="activo" data-cy="nav-prestamos">Préstamos</a>
      </nav>
    </header>
    <main><router-outlet /></main>
  `,
})
export class AppComponent {}
