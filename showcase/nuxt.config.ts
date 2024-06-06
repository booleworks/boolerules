// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    css: [
        'primeflex/primeflex.css',
        "primeicons/primeicons.css",
    ],
    modules: [
        '@vueuse/nuxt',
        '@nuxtjs/i18n',
        'nuxt-primevue',
    ],
    i18n: {
        vueI18n: './i18n.config.ts'
    },
    app: {
        baseURL: "/",
        head: {
            link: [
                {
                    id: 'theme-link',
                    rel: 'stylesheet',
                    href: '/themes/aura-light-cyan/theme.css'
                }
            ],
        },
    },
    build: {
        //transpile: ["primevue"]
    }
})
