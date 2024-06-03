import { usePrimeVue } from "primevue/config";

export default () => {
    const PrimeVue = usePrimeVue();

    const THEME = 'brTheme'
    const THEME_LINK = 'theme-link'
    const DARK = 'bw-dark'
    const LIGHT = 'bw-light'
    const currentTheme = ref(useSessionStorage(THEME, DARK))

    const switchTheme = () => {
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
