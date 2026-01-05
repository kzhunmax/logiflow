import {createRouter, createWebHistory} from "vue-router";
import MainLayout from "@/components/MainLayout.vue";
import ProductListView from "@/views/ProductListView.vue";
import ProductDetailView from "@/views/ProductDetailView.vue";
import ProductCreateView from "@/views/ProductCreateView.vue";

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {path: '', component: ProductListView},
      {path: '/product/:id', component: ProductDetailView},
      {path: '/products/create', component: ProductCreateView},
    ]
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
