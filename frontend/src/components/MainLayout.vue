<script setup>
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {useAuthStore} from '@/stores/authStore.js'
import LogoIcon from "@/components/icons/LogoIcon.vue";

const {locale, t} = useI18n()
const router = useRouter()
const authStore = useAuthStore()

function switchLocale(newLocale) {
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
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

        <!-- User Menu -->
        <div class="flex items-center gap-3 ml-4 border-l border-slate-200 pl-4">
          <div class="text-right">
            <p class="text-sm font-medium text-slate-700">{{ authStore.username }}</p>
            <p class="text-xs text-slate-500">{{ authStore.userRole }}</p>
          </div>
          <button
            @click="handleLogout"
            class="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            :title="t('nav.logout')"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
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
