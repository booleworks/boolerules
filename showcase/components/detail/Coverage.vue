<template>
    <DetailHeader />
    <Button class="mt-3" :label="$t('details.btn_compute')" icon="pi pi-info-circle" @click="computeDetails()" />
    <Button class="mt-3 ml-3" :label="$t('details.btn_compute_graph')" icon="pi pi-info-circle"
        @click="computeGraph()" />
    <div v-if="details.mainResult" class="mt-5">
        <DataTable :value="details.detail.detail.configurations">
            <Column field="coveredConstraints" :header="$t('details.covered_constraints')" style="vertical-align: top">
                <template #body="slotProps">
                    <div v-for="constraint of slotProps.data.coveredConstraints" class="covered-constraints mb-4">{{
                        constraint }}</div>
                </template>
            </Column>
            <Column field="configuration" :header="$t('details.example')" style="vertical-align: top">
                <template #body="slotProps">
                    <!--                    <DetailExampleConfiguration :model="slotProps.data.configuration"/>-->
                    <div class="feature-container">
                        <span v-for="feature in slotProps.data.configuration.features">
                            <span v-if="feature.booleanValue" class="mr-1">
                                <span class="important-info feature-item">{{ feature.code }}</span>
                            </span>
                            <span v-if="feature.enumValue" class="mr-1">
                                <span class="unimportant-info feature-item">{{ feature.code }}=</span>
                                <span class="important-info feature-item">{{ feature.enumValue }}</span>
                            </span>
                            <span v-if="feature.intValue" class="mr-1">
                                <span class="unimportant-info feature-item">{{ feature.code }}=</span>
                                <span class="important-info feature-item">{{ feature.intValue }}</span>
                            </span>
                            <span v-if="feature.version" class="mr-1">
                                <span class="unimportant-info feature-item">{{ feature.code }}=</span>
                                <span class="important-info feature-item">{{ feature.version }}</span>
                            </span>
                        </span>
                    </div>
                </template>
            </Column>
        </DataTable>
    </div>
    <div v-if="graphData.labels" class="mt-5">
        <!--        <DataTable :value="graphData.coverableConstraints">-->
        <!--            <Column field="numberOfConfigurations" :header="'numberOfConfigurations'" style="vertical-align: top"></Column>-->
        <!--            <Column field="maxCoverableConstraints" :header="'maxCoverableConstraints'" style="vertical-align: top"></Column>-->
        <!--        </DataTable>-->
        <Chart type="bar" :data="graphData" :options="chartOptions()" />
    </div>
</template>

<script setup lang="ts">
import Chart from 'primevue/chart';
import { type DetailRequest, type FeatureModel, type Slice } from '~/types/computations'

const { t } = useI18n();
const appConfig = useAppConfig()
const { getDetailRequest, getJobId } = useComputation()
const details = ref({} as CoverageDetail)
const graphData = ref({})

type CoverageDetail = {
    mainResult: { result: number }
    detail: {
        slice: Slice,
        detail: {
            configurations: CoveringConfiguration[]
        }
    }
}

type CoverageGraph = {
    coverableConstraints: [
        {
            numberOfConfigurations: number,
            maxCoverableConstraints: number,
        }
    ]
}

type CoveringConfiguration = {
    configuration: FeatureModel
    coveredConstraints: string[]
}

async function computeDetails() {
    graphData.value = {}
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.coverage_detail, {
        method: 'POST',
        body: request,
    }).then((res) => {
        details.value = res as CoverageDetail
    })
}

async function computeGraph() {
    details.value = {} as CoverageDetail
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.coverage_graph, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const graph = res as CoverageGraph
        graphData.value = {
            labels: graph.coverableConstraints.map(c => c.numberOfConfigurations),
            datasets: [
                {
                    label: t('algo.coverage.graph_coverable_constraints'),
                    data: graph.coverableConstraints.map(c => c.maxCoverableConstraints),
                    // backgroundColor: ['rgba(249, 115, 22, 0.2)', 'rgba(6, 182, 212, 0.2)', 'rgb(107, 114, 128, 0.2)'],
                    // borderColor: ['rgb(249, 115, 22)', 'rgb(6, 182, 212)', 'rgb(107, 114, 128)'],
                    // borderWidth: 1
                }
            ]
        }
    })
}

const chartOptions = () => {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    return {
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                enabled: true
            }
        },
        scales: {
            x: {
                ticks: {
                    color: textColorSecondary
                },
                grid: {
                    color: surfaceBorder
                },
                title: {
                    display: true,
                    text: t('algo.coverage.configurations_axis'),
                    color: textColor
                }
            },
            y: {
                beginAtZero: true,
                ticks: {
                    color: textColorSecondary
                },
                grid: {
                    color: surfaceBorder
                },
                title: {
                    display: true,
                    text: t('algo.coverage.coverable_constraints_axis'),
                    color: textColor
                }
            }
        }
    };
}
</script>

<style scoped>
.covered-constraints {
    font-family: monospace;
    font-weight: bold;
}

.feature-container {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
}

.feature-item {
    padding: 2px 2px;
    border-radius: 4px;
}

.unimportant-info {
    font-family: monospace;
    color: var(--text-color-secondary);
}

.important-info {
    font-family: monospace;
    font-weight: bold;
}
</style>
