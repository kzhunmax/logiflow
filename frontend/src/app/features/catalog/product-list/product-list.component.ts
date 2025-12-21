import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {ProductService} from '../../../core/services/product.service';
import {Product} from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  imports: [CommonModule, TableModule, ButtonModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
  standalone: true
})

export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);

  products: Product[] = [];
  loading: boolean = true;

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loading = true;
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading products', err);
        this.loading = false;
      }
    })
  }

}
