<script setup>

</script>

<template>
  <div class="app-container">
    <nav>
      <div class="logo">
        <div class="logo-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512"><path fill="currentColor" d="M560.3 237.2c10.4 11.8 28.3 14.4 41.8 5.5 14.7-9.8 18.7-29.7 8.9-44.4l-48-72c-2.8-4.2-6.6-7.7-11.1-10.2L351.4 4.7c-19.3-10.7-42.8-10.7-62.2 0L88.8 116c-5.4 3-9.7 7.4-12.6 12.8L27.7 218.7c-12.6 23.4-3.8 52.5 19.6 65.1l33 17.7 0 53.3c0 23 12.4 44.3 32.4 55.7l176 99.7c19.6 11.1 43.5 11.1 63.1 0l176-99.7c20.1-11.4 32.4-32.6 32.4-55.7l0-117.5zm-240-9.8L170.2 144 320.3 60.6 470.4 144 320.3 227.4zm-41.5 50.2l-21.3 46.2-165.8-88.8 25.4-47.2 161.7 89.8z"/></svg>
        </div>
        <span class="logo-text">LogiFlow</span>
      </div>
      <div class="nav-links">
        <RouterLink to="/">Products</RouterLink>
        <RouterLink to="/inventory">Inventory</RouterLink>
      </div>
    </nav>
    <main>
      <RouterView/>
    </main>
    <footer>
      <p>&copy; 2026 LogiFlow. All rights reserved</p>
    </footer>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

main {
  flex: 1;
}

nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-white);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
  position: sticky;
  top: 0;
  z-index: 50;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  width: 2rem;
  height: 2rem;
  padding: 0.25rem;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: var(--color-primary);
  border-radius: 0.5rem;
  color: white;
  font-weight: 700;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.015em;
  color: var(--color-text);
}

.nav-links {
  display: flex;
  gap: 1.5rem;
}

.nav-links a {
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-muted);
  transition: color 0.15s ease;
}

.nav-links a:hover,
.nav-links a.active {
  color: var(--color-primary);
}

footer {
  background-color: var(--color-white);
  border-top: 1px solid var(--color-border);

  padding: 1.5rem 1rem;
  text-align: center;

  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
