<template>
    <div class="border-r border-gray-300 overflow-scroll">
        <div class="m-3">
            <a href="/">
                <img :src="currentLogo()" alt="Logo" width="220" />
            </a>
        </div>
        <div class="menu-header text-primary"><i class="pi pi-fw pi-eye"></i>{{ ' ' +
            $t('computation.header_insights').toUpperCase()
            }}
        </div>
        <MenuItem v-for="algo in insightAlgos" :item="algo" />
        <div class="menu-header text-primary"><i class="pi pi-fw pi-sliders-h"></i>{{
            ' ' + $t('computation.header_configuration').toUpperCase()
        }}
        </div>
        <MenuItem v-for="algo in configurationAlgos" :item="algo" />
        <div class="menu-header text-primary"><i class="pi pi-fw pi-sort-amount-down"></i>{{
            ' ' + $t('computation.header_optimization').toUpperCase() }}
        </div>
        <MenuItem v-for="algo in optimizationAlgos" :item="algo" />
        <div class="menu-header text-primary"><i class="pi pi-fw pi-sitemap"></i>{{ ' ' +
            $t('computation.header_bom').toUpperCase()
            }}
        </div>
        <MenuItem v-for="algo in bomAlgos" :item="algo" />
        <div class="menu-header text-primary"><i class="pi pi-fw pi-box"></i>{{
            ' ' + $t('computation.header_software').toUpperCase() }}
        </div>
        <MenuItem v-for="algo in softwareAlgos" :item="algo" />
    </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { currentLogo } = useTheme()

const insightAlgos = ref([
    { label: t('computation.counting'), to: '/computation/rulefile/counting' },
    { label: t('computation.constraint_graph'), to: '/computation/visualization/constraintgraph' },
])

const configurationAlgos = ref([
    { label: t('computation.consistency'), to: '/computation/rulefile/consistency' },
    { label: t('computation.reconfiguration'), to: '/computation/configuration/reconfiguration' },
    { label: t('computation.backbone'), to: '/computation/rulefile/backbone' },
    { label: t('computation.enumeration'), to: '/computation/rulefile/enumeration' },
])

const optimizationAlgos = ref([
    { label: t('computation.minmax'), to: '/computation/configuration/minmax' },
    { label: t('computation.coverage'), to: '/computation/optimization/coverage' },
    { label: t('computation.weights'), to: '/computation/optimization/optimization' },
])

const bomAlgos = ref([
    { label: t('computation.pos_validity'), to: '' },
])

const softwareAlgos = ref([
    { label: t('computation.packagesolving'), to: '/computation/software/packagesolving' },
])
</script>



<style scoped>
.menu-header {
    font-weight: bold;
    font-size: 0.875rem;
    margin-left: 1rem;
    margin-top: 2rem;
    margin-bottom: 0.1rem;
}
</style>
