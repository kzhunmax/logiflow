import { createApp } from 'vue'
import {router} from "@/router/index.js";
import App from './App.vue'
import {pinia} from "@/stores/index.js";

createApp(App).use(router).use(pinia).mount('#app')
