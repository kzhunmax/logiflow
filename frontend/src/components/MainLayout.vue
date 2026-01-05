<script setup>
import {useI18n} from 'vue-i18n'
import LogoIcon from "@/components/icons/LogoIcon.vue";

const {locale, t} = useI18n()

function switchLocale(newLocale) {
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <nav
      class="flex justify-between items-center bg-white border-b border-slate-200 px-6 py-4 sticky top-0 z-50">
      <div class="flex items-center gap-3">
        <div
          class="w-8 h-8 p-1 flex items-center justify-center bg-blue-600 rounded-lg text-white font-bold">
          <LogoIcon/>
        </div>
        <span class="text-xl font-bold tracking-tight text-slate-900">LogiFlow</span>
      </div>
      <div class="flex gap-6 items-center">
        <RouterLink to="/"
                    class="text-sm font-medium text-slate-500 hover:text-blue-600 transition-colors [&.router-link-exact-active]:text-blue-600">
          {{ t('nav.products') }}
        </RouterLink>

        <div class="flex items-center gap-1 ml-4 border-l border-slate-200 pl-4">
          <button
            @click="switchLocale('en')"
            :class="locale === 'en' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            class="px-2 py-1 text-xs font-medium rounded transition-colors"
          >
            EN
          </button>
          <button
            @click="switchLocale('uk')"
            :class="locale === 'uk' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            class="px-2 py-1 text-xs font-medium rounded transition-colors"
          >
            UK
          </button>
        </div>
      </div>
    </nav>
    <main class="flex-1">
      <RouterView/>
    </main>
    <footer class="bg-white border-t border-slate-200 py-6 px-4 text-center text-xs text-slate-500">
      <p>{{ t('footer.copyright') }}</p>
    </footer>
  </div>
</template>

<style scoped>

</style>
