import { usePrimeVue } from "primevue/config";

export default () => {
    const PrimeVue = usePrimeVue();

    const THEME = 'brTheme'
    const THEME_LINK = 'theme-link'
    const DARK = 'lara-dark-green'
    const LIGHT = 'lara-light-green'
    const currentTheme = ref(useSessionStorage(THEME, LIGHT))

    const switchTheme = () => {
        document.documentElement.classList.toggle('dark');
        const ct = currentTheme.value
        const nt = currentTheme.value === DARK ? LIGHT : DARK
        PrimeVue.changeTheme(ct, nt, THEME_LINK, () => { });
        currentTheme.value = nt
    }

    const currentLogo = (): string => {
        return currentTheme.value === DARK ? "/br-logo-dm.png" : "/br-logo.png"
    }

    const currentIcon = (): string => {
        return currentTheme.value === DARK ? "pi pi-sun" : "pi pi-moon"
    }

    return { switchTheme, currentLogo, currentIcon }
}
