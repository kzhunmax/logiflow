import {Routes} from '@angular/router';
import {ProductList} from '@features/catalog/product-list/product-list';
import {ProductCreate} from '@features/catalog/product-create/product-create';
import {MainLayout} from '@core/layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      {
        path: 'catalog', component: ProductList
      },
      {
        path: 'catalog/create', component: ProductCreate
      }
    ]
  }
];
