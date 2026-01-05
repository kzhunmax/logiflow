<script setup>
import {useI18n} from "vue-i18n";

const { t } = useI18n()

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
  <main class="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
    <table class="w-full text-sm text-left border-collapse">
      <thead class="bg-slate-50 border-b border-slate-200">
      <tr>
        <th v-for="col in columns" :key="col.key" class="px-6 py-4 font-medium text-slate-500 uppercase text-xs tracking-wide">{{ col.label }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-if="loading">
        <td :colspan="columns.length" class="text-left px-6 py-4 text-slate-400 text-sm">{{ t('common.loading') }}</td>
      </tr>
      <tr v-else-if="items.length === 0">
        <td :colspan="columns.length" class="text-left px-6 py-4 text-slate-400 text-sm">{{ t('common.noData') }}</td>
      </tr>
      <tr
        v-else
        v-for="item in items"
        :key="item.id"
        @click="emit('row-click', item)"
        class="transition-colors hover:bg-slate-50 cursor-pointer"
      >
        <td v-for="col in columns" :key="col.key" class="px-6 py-4 border-b border-slate-200 text-slate-900 align-middle">
          <slot :name="col.key" :item="item">
            {{ item[col.key] }}
          </slot>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="flex justify-between items-center px-6 py-4 bg-slate-50 border-t border-slate-200 text-xs text-slate-500">
      <span>{{ t('common.total') }}: {{ totalItems }} {{ t('common.items') }}</span>
      <div class="flex gap-2 items-center">
        <button :disabled="currentPage === 1" @click="goToPage(1)" class="px-2 py-1 border border-slate-200 bg-white rounded text-slate-500 cursor-pointer transition-all hover:bg-slate-200 hover:text-slate-900 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-50">«</button>
        <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)" class="px-2 py-1 border border-slate-200 bg-white rounded text-slate-500 cursor-pointer transition-all hover:bg-slate-200 hover:text-slate-900 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-50">‹</button>
        <span>{{ t('common.page') }} {{ currentPage }} {{ t('common.of') }} {{ Math.max(totalPages, 1) }}</span>
        <button :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)" class="px-2 py-1 border border-slate-200 bg-white rounded text-slate-500 cursor-pointer transition-all hover:bg-slate-200 hover:text-slate-900 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-50">›</button>
        <button :disabled="currentPage === totalPages" @click="goToPage(totalPages)" class="px-2 py-1 border border-slate-200 bg-white rounded text-slate-500 cursor-pointer transition-all hover:bg-slate-200 hover:text-slate-900 disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-50">»</button>
      </div>
    </div>

  </main>
</template>

<style scoped>

</style>
