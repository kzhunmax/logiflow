import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Inventory} from '@features/inventory/inventory';
import {StockAdjustmentRequest} from '@features/inventory/stock-adjustment/stock-adjustment-request';

@Injectable({
  providedIn: 'root',
})
export class InventoryApi {
  private http = inject(HttpClient)
  private readonly apiUrl = 'api/v1/inventory'

  getInventory(sku: string): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.apiUrl}/${sku}`);
  }

  adjustStock(request: StockAdjustmentRequest): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.apiUrl}/stock`, request);
  }
}
