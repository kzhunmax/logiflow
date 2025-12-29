import {createRouter, createWebHistory} from "vue-router";
import MainLayout from "@/components/MainLayout.vue";
import ProductListView from "@/views/ProductListView.vue";
import ProductDetailView from "@/views/ProductDetailView.vue";
import InventoryView from "@/views/InventoryView.vue";

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {path: '', component: ProductListView},
      {path: '/catalog', component: ProductDetailView},
      {path: '/inventory', component: InventoryView},
    ]
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
