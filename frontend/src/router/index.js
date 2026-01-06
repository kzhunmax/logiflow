import {createRouter, createWebHistory} from "vue-router";
import MainLayout from "@/components/MainLayout.vue";
import ProductListView from "@/views/ProductListView.vue";
import ProductDetailView from "@/views/ProductDetailView.vue";
import ProductCreateView from "@/views/ProductCreateView.vue";
import LoginView from "@/views/LoginView.vue";

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresGuest: true }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {path: '', name: 'Dashboard', component: ProductListView},
      {path: '/product/:id', name: 'ProductDetail', component: ProductDetailView},
      {path: '/products/create', name: 'ProductCreate', component: ProductCreateView},
    ]
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  // With HTTP-only cookies, we can't access tokens directly
  // We use stored user info as a hint for UI routing
  // Actual auth is verified by the server on API calls
  const user = localStorage.getItem('logiflow_user');
  const isAuthenticated = !!user;

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Redirect to login if trying to access protected route without auth
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else if (to.meta.requiresGuest && isAuthenticated) {
    // Redirect to dashboard if trying to access login while authenticated
    next({ path: '/' });
  } else {
    next();
  }
});

