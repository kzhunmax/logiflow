<script setup>

</script>

<template>
  <div class="min-h-screen flex flex-col">
    <nav class="flex justify-between items-center bg-white border-b border-slate-200 px-6 py-4 sticky top-0 z-50">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 p-1 flex items-center justify-center bg-blue-600 rounded-lg text-white font-bold">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512"><path fill="currentColor" d="M560.3 237.2c10.4 11.8 28.3 14.4 41.8 5.5 14.7-9.8 18.7-29.7 8.9-44.4l-48-72c-2.8-4.2-6.6-7.7-11.1-10.2L351.4 4.7c-19.3-10.7-42.8-10.7-62.2 0L88.8 116c-5.4 3-9.7 7.4-12.6 12.8L27.7 218.7c-12.6 23.4-3.8 52.5 19.6 65.1l33 17.7 0 53.3c0 23 12.4 44.3 32.4 55.7l176 99.7c19.6 11.1 43.5 11.1 63.1 0l176-99.7c20.1-11.4 32.4-32.6 32.4-55.7l0-117.5zm-240-9.8L170.2 144 320.3 60.6 470.4 144 320.3 227.4zm-41.5 50.2l-21.3 46.2-165.8-88.8 25.4-47.2 161.7 89.8z"/></svg>
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
