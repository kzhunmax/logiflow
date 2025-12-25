import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Inventory} from '@features/inventory/inventory';

@Injectable({
  providedIn: 'root',
})
export class InventoryApi {
  private http = inject(HttpClient)
  private readonly apiUrl = 'api/inventory'

  getInventory(sku: string): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.apiUrl}/${sku}`);
  }
}
