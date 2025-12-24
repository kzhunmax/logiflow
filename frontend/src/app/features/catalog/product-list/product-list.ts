import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule, TableLazyLoadEvent} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {ProductApi} from '../product-api'
import {Product} from '../product';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-product-list',
  imports: [CommonModule, TableModule, ButtonModule, RouterLink],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
  standalone: true
})

export class ProductList {
  private productApi = inject(ProductApi);

  products: Product[] = [];

  totalRecords: number = 0;
  loading: boolean = true;
  pageSize: number = 10;


  loadProducts(event: TableLazyLoadEvent) {
    this.loading = true;
    const page = (event.first ?? 0) / (event.rows ?? this.pageSize);
    const size = event.rows ?? this.pageSize;

    this.productApi.getProducts(page, size).subscribe({
      next: (response) => {
        this.products = response.content;
        this.totalRecords = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading products', err);
        this.loading = false;
      }
    })
  }

  deleteProduct(id: string) {
    if (!confirm('Are you sure you want to delete this product?')) return;

    this.productApi.deleteProduct(id).subscribe({
      next: () => {
        location.reload();
      },
      error: (err) => alert('Failed to delete product')
    });
  }

}
