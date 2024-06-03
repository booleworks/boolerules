<template>
    <div class="border-right-1 surface-border overflow-scroll">
        <div class="m-3">
            <a href="/">
                <img :src="currentLogo()" alt="Logo" width="200" />
            </a>
        </div>
        <div class="menu-header"><i class="pi pi pi-fw pi-file"></i> {{ $t('computation.header_rulefile').toUpperCase()
            }}
        </div>
        <MenuItem v-for="algo in rulefileAlgos" :item="algo" />
        <div class="menu-header"><i class="pi pi pi-fw pi-box"></i>{{
            $t('computation.header_configuration').toUpperCase()
            }}
        </div>
        <MenuItem v-for="algo in configurationAlgos" :item="algo" />
        <div class="menu-header"><i class="pi pi pi-fw pi-sort-amount-down"></i>{{
            $t('computation.header_optimization').toUpperCase() }}
        </div>
        <MenuItem v-for="algo in optimizationAlgos" :item="algo" />
        <!-- <div class="menu-header"><i class="pi pi pi-fw pi-sitemap"></i>{{ $t('computation.header_bom').toUpperCase() }}</div> -->
        <!-- <MenuItem v-for="algo in bomAlgos" :item="algo" /> -->
        <!-- <div class="menu-header"><i class="pi pi pi-fw pi-desktop"></i>{{ $t('computation.header_software').toUpperCase() }} -->
        <!-- </div> -->
        <!-- <MenuItem v-for="algo in softwareAlgos" :item="algo" /> -->
        <!-- <div class="menu-header"><i class="pi pi pi-fw pi-share-alt"></i>{{ $t('computation.header_vis').toUpperCase() }} -->
        <!-- </div> -->
        <!-- <MenuItem v-for="algo in visualizationAlgos" :item="algo" /> -->
    </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { currentLogo } = useTheme()

const rulefileAlgos = ref([
    { label: t('computation.consistency'), to: '/computation/rulefile/consistency' },
    { label: t('computation.backbone'), to: '/computation/rulefile/backbone' },
    { label: t('computation.enumeration'), to: '/computation/rulefile/enumeration' },
    { label: t('computation.counting'), to: '/computation/rulefile/counting' },
])
const configurationAlgos = ref([
    { label: t('computation.minmax'), to: '/computation/configuration/minmax' },
    { label: t('computation.reconfiguration'), to: '/computation/configuration/reconfiguration' },
    // { label: t('computation.constraints'), to: '' },
    // { label: t('computation.bulk'), to: '' },
])
const optimizationAlgos = ref([
    { label: t('computation.weights'), to: '/computation/optimization/optimization' },
    { label: t('computation.coverage'), to: '/computation/optimization/coverage' },
])
// const bomAlgos = ref([
//   { label: t('computation.pos_uniqueness'), to: '' },
//   { label: t('computation.pos_dead'), to: '' },
// ])
// const softwareAlgos = ref([
//   { label: t('computation.pkg_buildability'), to: '' },
//   { label: t('computation.pkg_solving'), to: '' },
//   { label: t('computation.pkg_optimization'), to: '' },
// ])
// const visualizationAlgos = ref([
//   { label: t('computation.constraint_graph'), to: '' },
//   { label: t('computation.feature_cloud'), to: '' },
// ])

</script>

<style scoped>
.menu-header {
    font-weight: bold;
    font-size: 0.875rem;
    color: var(--primary-color);
    margin-left: 1rem;
    margin-top: 2rem;
    margin-bottom: 0.75rem;
}
</style>
