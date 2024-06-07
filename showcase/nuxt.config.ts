import path from 'path'

export default defineNuxtConfig({
    css: [
        "public/br-theme.css",
        "primeicons/primeicons.css",
    ],
    modules: [
        '@nuxtjs/tailwindcss',
        '@vueuse/nuxt',
        '@nuxtjs/i18n',
        'nuxt-primevue',
    ],
    i18n: {
        vueI18n: './i18n.config.ts'
    },
    primevue: {
        options: {
            unstyled: true
        },
        importPT: { as: 'Lara', from: path.resolve(__dirname, './presets/lara/') }
    },
})
