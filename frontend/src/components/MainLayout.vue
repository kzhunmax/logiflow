<script setup>

</script>

<template>
  <div>
    <nav>
      <RouterLink to="/">Products</RouterLink>
      <RouterLink to="/inventory">Inventory</RouterLink>
    </nav>
    <main>
      <RouterView/>
    </main>
  </div>
</template>

<style scoped>
nav {
  display: flex;
  gap: 1rem;
  background-color: #f0f0f0;
  padding: 1rem;
}

nav a {
  text-decoration: none;
  color: #333;
}

main {
  padding: 1rem;
}
</style>
