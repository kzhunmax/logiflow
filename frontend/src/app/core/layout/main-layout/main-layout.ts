import {Component} from '@angular/core';
import {Menubar} from 'primeng/menubar';
import {MenuItem, PrimeTemplate} from 'primeng/api';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-main-layout',
  imports: [Menubar, RouterOutlet, PrimeTemplate],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
  standalone: true
})
export class MainLayout {
   items: MenuItem[] = [
    {label: 'Home', routerLink: '/'},
    {label: 'Catalog', routerLink: '/catalog'},
    {label: 'Inventory', routerLink: '/inventory'}
  ];
}
