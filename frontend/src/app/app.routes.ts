import {Routes} from '@angular/router';
import {ProductList} from '@features/catalog/product-list/product-list';
import {ProductCreate} from '@features/catalog/product-create/product-create';

export const routes: Routes = [
  {path: '', redirectTo: 'catalog', pathMatch: 'full'},
  {path: 'catalog', component: ProductList},
  {path: 'catalog/create', component: ProductCreate}
];
