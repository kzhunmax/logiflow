import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableLazyLoadEvent, TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {ProductApi} from '../product-api'
import {Product} from '../product';
import {RouterLink} from '@angular/router';
import {InventoryApi} from '@features/inventory/inventory-api';
import {forkJoin} from 'rxjs';
import {StockAdjustment} from '@features/inventory/stock-adjustment/stock-adjustment';
import {Inventory} from '@features/inventory/inventory';
import {Tooltip} from 'primeng/tooltip';

@Component({
  selector: 'app-product-list',
  imports: [CommonModule, TableModule, ButtonModule, RouterLink, StockAdjustment, Tooltip],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
  standalone: true
})

export class ProductList {
  private productApi = inject(ProductApi);
  private inventoryApi = inject(InventoryApi);

  products: Product[] = [];

  totalRecords: number = 0;
  loading: boolean = true;
  pageSize: number = 10;
  isDialogVisible = false;
  selectedSku = '';

  openAdjustmentDialog(sku: string) {
    this.selectedSku = sku;
    this.isDialogVisible = true;
  }

  onStockUpdated(updatedInventory: Inventory) {
    const index = this.products.findIndex(p => p.sku === updatedInventory.sku);
    if (index !== -1) {
      this.products[index].quantity = updatedInventory.availableQuantity;
      this.products = [...this.products];
    }
  }

  loadProducts(event: TableLazyLoadEvent) {
    this.loading = true;
    const page = (event.first ?? 0) / (event.rows ?? this.pageSize);
    const size = event.rows ?? this.pageSize;

    this.productApi.getProducts(page, size).subscribe({
      next: (response) => {
        this.products = response.content;
        this.totalRecords = response.totalElements;

        this.loadInventoryForProduct(this.products);

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
      error: (err) => alert(`Failed to delete product: ${err}`)
    });
  }

  private loadInventoryForProduct(products: Product[]) {
    if (!products.length) return;

    forkJoin(
      products.map(p =>
        this.inventoryApi.getInventory(p.sku)
      )
    ).subscribe({
      next: (inventories) => {
        inventories.forEach((inv, index) => {
          products[index].quantity = inv.availableQuantity;
        });
      },
      error: err => console.error('Inventory load failed', err)
    });
  }
}
