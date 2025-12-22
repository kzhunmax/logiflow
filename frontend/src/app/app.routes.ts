import { Routes } from '@angular/router';
import { ProductListComponent } from './features/catalog/product-list/product-list.component';
import { ProductCreateComponent } from './features/catalog/product-create/product-create.component';

export const routes: Routes = [
  {path: '', redirectTo: 'catalog', pathMatch: 'full'},
  {path: 'catalog', component: ProductListComponent},
  {path: 'catalog/create', component: ProductCreateComponent}
];
