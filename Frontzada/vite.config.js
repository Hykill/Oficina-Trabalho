import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // Permite importar com "@/components/..." em vez de "../../components/..."
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    // PROXY: o navegador chama http://localhost:5173/api/clientes
    // e o Vite repassa para http://localhost:8080/clientes.
    // Como para o navegador é a "mesma origem", não há erro de CORS
    // e não foi preciso alterar nada no Spring Boot.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
