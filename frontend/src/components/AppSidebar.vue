<script setup>
import { Users, Car, Settings, Wrench, X, ClipboardList, Package, CircleCheck } from 'lucide-vue-next'

defineProps({ aberta: { type: Boolean, default: false } })
defineEmits(['fechar'])

const menu = [
  { to: '/clientes', label: 'Clientes', icon: Users },
  { to: '/veiculos', label: 'Veículos', icon: Car },
  { to: '/pecas', label: 'Peças', icon: Package },
  { to: '/servicos', label: 'Serviços', icon: ClipboardList },
  { to: '/prontos', label: 'Prontos', icon: CircleCheck }
]
</script>

<template>
  <!-- Fundo escuro atrás da sidebar no mobile -->
  <Transition name="fade">
    <div v-if="aberta" class="backdrop" @click="$emit('fechar')" />
  </Transition>

  <aside class="sidebar" :class="{ aberta }">
    <!-- Faixa de sinalização no topo, como em parede de oficina -->
    <div class="hazard topo" />
    <div class="brand">
      <span class="logo"><Wrench :size="20" /></span>
      <div>
        <div class="brand-name">Oficina</div>
        <div class="brand-sub">Mecânica &amp; Serviços</div>
      </div>
      <button class="icon-btn close" aria-label="Fechar menu" @click="$emit('fechar')">
        <X :size="18" />
      </button>
    </div>

    <nav class="nav">
      <span class="nav-section">Menu</span>
      <!-- RouterLink troca de página sem recarregar o navegador -->
      <RouterLink v-for="item in menu" :key="item.to" :to="item.to" class="nav-link" active-class="ativo">
        <component :is="item.icon" :size="18" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>

    <div class="nav-bottom">
      <span class="nav-section">Sistema</span>
      <RouterLink to="/configuracoes" class="nav-link" active-class="ativo">
        <Settings :size="18" />
        <span>Configurações</span>
      </RouterLink>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 50;
  width: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  padding: 0 14px 20px;
  /* Aço escovado: linhas horizontais finas sobre o grafite */
  background:
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.015) 0 1px, transparent 1px 3px),
    linear-gradient(180deg, var(--bg-elevated), var(--bg));
  border-right: 1px solid var(--border);
  transition: transform var(--transition);
}

.hazard.topo { margin: 0 -14px 20px; }
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 20px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
/* Logo: placa de aço com parafusos nos cantos */
.logo {
  position: relative;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 6px;
  background: var(--steel);
  color: var(--on-primary);
  border: 1px solid rgba(0, 0, 0, 0.4);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4), 0 4px 12px rgba(0, 0, 0, 0.35);
}
.logo::before,
.logo::after {
  content: '';
  position: absolute;
  top: 4px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
}
.logo::before { left: 4px; }
.logo::after { right: 4px; }
.brand-name { font-weight: 700; font-size: 22px; line-height: 1; text-transform: uppercase; letter-spacing: 0.08em; }
.brand-sub { font-size: 11px; color: var(--text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-top: 3px; }
.close { display: none; margin-left: auto; }

.nav { display: flex; flex-direction: column; gap: 2px; }
.nav-bottom { margin-top: auto; display: flex; flex-direction: column; gap: 2px; padding-top: 16px; border-top: 1px solid var(--border); }
.nav-section {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-faint);
  padding: 0 12px 8px;
}

.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 40px;
  padding: 0 12px;
  border-radius: var(--radius);
  color: var(--text-muted);
  font-weight: 500;
  transition: background var(--transition), color var(--transition);
}
.nav-link:hover { background: var(--surface-2); color: var(--text); }
.nav-link.ativo { background: var(--primary-soft); color: var(--text); }
.nav-link.ativo svg { color: var(--primary-text); }
.nav-link { font-family: var(--font-titulo); font-size: 16px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.nav-link.ativo::before {
  content: '';
  position: absolute;
  left: -14px;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 0 2px 2px 0;
  background: var(--hazard-a);
}

.backdrop { display: none; }

@media (max-width: 900px) {
  .sidebar { transform: translateX(-100%); box-shadow: var(--shadow-lg); }
  .sidebar.aberta { transform: translateX(0); }
  .close { display: inline-grid; }
  .backdrop { display: block; position: fixed; inset: 0; z-index: 40; background: rgba(0, 0, 0, 0.55); }
}

.fade-enter-active, .fade-leave-active { transition: opacity 180ms ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
