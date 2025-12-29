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
  <div class="data-table-container">
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

  </div>
</template>

<style scoped>
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.data-table th {
  background: #f5f5f5;
  font-weight: 600;
}

.data-table tr.clickable:hover {
  background: #f9f9f9;
  cursor: pointer;
}

.loading,
.empty {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
}

.pagination-controls {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.pagination button {
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
