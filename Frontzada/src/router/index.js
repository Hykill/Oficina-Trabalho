import { createRouter, createWebHistory } from 'vue-router'

// Lazy loading: cada página só é baixada quando o usuário a acessa.
const routes = [
  { path: '/', redirect: '/clientes' }, // página inicial = Clientes
  { path: '/clientes', name: 'clientes', component: () => import('@/views/ClientesView.vue'), meta: { titulo: 'Clientes' } },
  { path: '/veiculos', name: 'veiculos', component: () => import('@/views/VeiculosView.vue'), meta: { titulo: 'Veículos' } },
  { path: '/servicos', name: 'servicos', component: () => import('@/views/ServicosView.vue'), meta: { titulo: 'Serviços' } },
  { path: '/prontos', name: 'prontos', component: () => import('@/views/ProntosView.vue'), meta: { titulo: 'Prontos' } },
  { path: '/pecas', name: 'pecas', component: () => import('@/views/PecasView.vue'), meta: { titulo: 'Peças' } },
  { path: '/configuracoes', name: 'configuracoes', component: () => import('@/views/ConfiguracoesView.vue'), meta: { titulo: 'Configurações' } },
  { path: '/:pathMatch(.*)*', redirect: '/clientes' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.afterEach((to) => {
  document.title = `${to.meta.titulo ?? 'Oficina'} · Oficina`
})

export default router
