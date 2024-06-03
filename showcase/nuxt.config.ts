// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    css: [
        "primevue/resources/primevue.css",
        "primeicons/primeicons.css",
        'primeflex/primeflex.css'
    ],
    modules: [
        '@vueuse/nuxt',
        '@nuxtjs/i18n'
    ],
    i18n: {
        vueI18n: './i18n.config.ts'
    },
    app: {
        head: {
            link: [
                {
                    id: 'theme-link',
                    rel: 'stylesheet',
                    href: '/themes/bw-dark/theme.css'
                }
            ],
        },
    },
    build: {
        transpile: ["primevue"]
    }
})
