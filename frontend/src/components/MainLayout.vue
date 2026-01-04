<script setup>

import LogoIcon from "@/components/icons/LogoIcon.vue";
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <nav class="flex justify-between items-center bg-white border-b border-slate-200 px-6 py-4 sticky top-0 z-50">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 p-1 flex items-center justify-center bg-blue-600 rounded-lg text-white font-bold">
          <LogoIcon/>
        </div>
        <span class="text-xl font-bold tracking-tight text-slate-900">LogiFlow</span>
      </div>
      <div class="flex gap-6">
        <RouterLink to="/" class="text-sm font-medium text-slate-500 hover:text-blue-600 transition-colors [&.router-link-exact-active]:text-blue-600">Products</RouterLink>
        <RouterLink to="/inventory" class="text-sm font-medium text-slate-500 hover:text-blue-600 transition-colors [&.router-link-exact-active]:text-blue-600">Inventory</RouterLink>
      </div>
    </nav>
    <main class="flex-1">
      <RouterView/>
    </main>
    <footer class="bg-white border-t border-slate-200 py-6 px-4 text-center text-xs text-slate-500">
      <p>&copy; 2026 LogiFlow. All rights reserved</p>
    </footer>
  </div>
</template>

<style scoped>

</style>
