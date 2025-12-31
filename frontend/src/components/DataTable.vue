<script setup>

const props = defineProps({
  columns: {type: Array, required: true},
  items: {type: Array, required: true},
  loading: {type: Boolean, default: false},
  currentPage: {type: Number, default: 1},
  totalPages: {type: Number, default: 1},
  totalItems: {type: Number, default: 0},
})

const emit = defineEmits(['page-change', 'row-click'])

function goToPage(page) {
  if (page >= 1 && page <= props.totalPages) {
    emit('page-change', page)
  }
}
</script>

<template>
  <main class="data-table-container">
    <table class="data-table">
      <thead>
      <tr>
        <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-if="loading">
        <td :colspan="columns.length" class="loading">Loading...</td>
      </tr>
      <tr v-else-if="items.length === 0">
        <td :colspan="columns.length" class="empty">No data available</td>
      </tr>
      <tr
        v-else
        v-for="item in items"
        :key="item.id"
        @click="emit('row-click', item)"
        class="clickable"
      >
        <td v-for="col in columns" :key="col.key">
          <slot :name="col.key" :item="item">
            {{ item[col.key] }}
          </slot>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="pagination">
      <span>Total: {{ totalItems }} items</span>
      <div class="pagination-controls">
        <button :disabled="currentPage === 1" @click="goToPage(1)">«</button>
        <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">‹</button>
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
        <button :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">›</button>
        <button :disabled="currentPage === totalPages" @click="goToPage(totalPages)">»</button>
      </div>
    </div>

  </main>
</template>

<style scoped>
.data-table-container {
  background-color: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 0.75rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
  text-align: left;
}

.data-table thead {
  background-color: var(--color-background);
  border-bottom: 1px solid #e2e8f0;
}

.data-table th {
  padding: 1rem 1.5rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
}

.data-table td {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  color: #0f172a;
  vertical-align: middle;
}

.data-table tr.clickable {
  transition: background-color 0.2s;
}

.data-table tr.clickable:hover {
  background-color: var(--color-background);
  cursor: pointer;
}

.loading,
.empty {
  text-align: start;
  padding: 3rem;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: var(--color-background);
  border-top: 1px solid var(--color-border);
  font-size: 0.75rem;
  color: #64748b;
}

.pagination-controls {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.pagination button {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--color-border);
  background-color: var(--color-white);
  border-radius: 0.25rem;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  background-color: var(--color-border);
  color: var(--color-text);
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: var(--color-background);
}
</style>
