<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, Wrench } from 'lucide-vue-next'
import AppSidebar from '@/components/AppSidebar.vue'
import ToastContainer from '@/components/ToastContainer.vue'

// No celular a sidebar vira um "drawer" que abre pelo botão de menu
const sidebarAberta = ref(false)
const route = useRoute()

// Fecha o menu mobile sempre que trocar de página
watch(() => route.fullPath, () => (sidebarAberta.value = false))
</script>

<template>
  <div class="layout">
    <AppSidebar :aberta="sidebarAberta" @fechar="sidebarAberta = false" />

    <div class="main">
      <!-- Barra superior só aparece em telas pequenas -->
      <header class="topbar">
        <button class="icon-btn" aria-label="Abrir menu" @click="sidebarAberta = true">
          <Menu :size="20" />
        </button>
        <div class="topbar-brand">
          <span class="topbar-logo"><Wrench :size="16" /></span>
          Oficina
        </div>
      </header>

      <main class="content">
        <!-- Transição suave entre as páginas -->
        <RouterView v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <ToastContainer />
  </div>
</template>

<style scoped>
.layout { display: flex; min-height: 100vh; }

.main {
  flex: 1;
  min-width: 0;
  margin-left: var(--sidebar-width);
  /* Fundo com textura de chapa xadrez, como piso de oficina */
  background: var(--textura), var(--bg);
}

.content { padding: 32px 36px 48px; max-width: 1400px; margin: 0 auto; }

.topbar { display: none; }

@media (max-width: 900px) {
  .main { margin-left: 0; }
  .content { padding: 20px 16px 40px; }
  .topbar {
    position: sticky;
    top: 0;
    z-index: 30;
    display: flex;
    align-items: center;
    gap: 12px;
    height: 56px;
    padding: 0 12px;
    background: var(--topbar-bg);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid var(--border);
  }
  .topbar-brand {
    display: flex; align-items: center; gap: 10px;
    font-family: var(--font-titulo); font-weight: 700; font-size: 18px;
    text-transform: uppercase; letter-spacing: 0.06em;
  }
  .topbar { border-top: 3px solid var(--hazard-a); }
  .topbar-logo {
    display: grid; place-items: center;
    width: 28px; height: 28px; border-radius: 8px;
    background: var(--steel); color: var(--on-primary);
  }
}
</style>
