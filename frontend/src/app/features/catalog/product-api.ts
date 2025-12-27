import {Injectable, inject} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Product} from './product';
import {Page} from '@core/models/page';

@Injectable({
  providedIn: 'root'
})

export class ProductApi {
  private http = inject(HttpClient);
  private readonly apiUrl = 'api/v1/catalog/products';

  getProducts(page: number, size: number, search: string = ''): Observable<Page<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<Page<Product>>(this.apiUrl, {params});
  }

  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  deleteProduct(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`);
  }
}
