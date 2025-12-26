import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {DialogModule} from 'primeng/dialog';
import {AdjustmentType, StockAdjustmentRequest} from '@features/inventory/stock-adjustment/stock-adjustment-request';
import {InventoryApi} from '@features/inventory/inventory-api';
import {Inventory} from '@features/inventory/inventory';
import {RadioButton} from 'primeng/radiobutton';
import {FormsModule} from '@angular/forms';
import {InputNumber} from 'primeng/inputnumber';
import {Button} from 'primeng/button';

@Component({
  selector: 'app-stock-adjustment',
  standalone: true,
  imports: [DialogModule, RadioButton, FormsModule, InputNumber, Button],
  templateUrl: './stock-adjustment.html',
  styleUrl: './stock-adjustment.scss',
})
export class StockAdjustment {

  private inventoryApi = inject(InventoryApi);

  @Input() visible: boolean = false;
  @Output() visibleChange = new EventEmitter<boolean>();

  @Input() sku: string = '';
  @Output() stockChanged = new EventEmitter<Inventory>();

  quantity: number = 0;
  type: AdjustmentType = AdjustmentType.ADD;

  AdjustmentType = AdjustmentType;
  loading = false;

  close() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.resetForm();
  }

  saveAdjustment() {
    if (!this.quantity || this.quantity <= 0) return;

    this.loading = true;
    const request: StockAdjustmentRequest = {
      sku: this.sku,
      adjustmentQuantity: this.quantity,
      type: this.type
    };

    this.inventoryApi.adjustStock(request).subscribe({
      next: (updatedInventory) => {
        this.stockChanged.emit(updatedInventory);
        this.loading = false;
        this.close();
      },
      error: (err) => {
        console.error('Adjustment failed', err);
        this.loading = false;
      }
    });
  }

  private resetForm() {
    this.quantity = 0;
    this.type = AdjustmentType.ADD;
  }
}
