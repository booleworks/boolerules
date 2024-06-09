<template>
    <DetailHeader />
    <Button class="mt-3" :label="$t('details.btn_compute')" icon="pi pi-info-circle" @click="computeDetails()" />
    <Button class="mt-3 ml-3" :label="$t('details.btn_compute_graph')" icon="pi pi-chart-bar" @click="computeGraph()" />
    <div v-if="details.mainResult" class="mt-5">
        <DataTable size="small" :value="details.detail.detail.configurations">
            <Column field="coveredConstraints" :header="$t('details.covered_constraints')" style="vertical-align: top">
                <template #body="slotProps">
                    <div v-for="constraint of slotProps.data.coveredConstraints" class="font-mono font-bold mb-4">{{
                        constraint }}</div>
                </template>
            </Column>
            <Column field="configuration" :header="$t('details.example')" style="vertical-align: top">
                <template #body="slotProps">
                    <div class="feature-container">
                        <span v-for="feature in slotProps.data.configuration.features">
                            <span v-if="feature.booleanValue" class="mr-1">
                                <span class="font-mono font-bold feature-item">{{ feature.code }}</span>
                            </span>
                            <span v-if="feature.enumValue" class="mr-1">
                                <span class="font-mono text-gray-500 feature-item">{{ feature.code }}=</span>
                                <span class="font-mono font-bold feature-item">{{ feature.enumValue }}</span>
                            </span>
                            <span v-if="feature.intValue" class="mr-1">
                                <span class="font-mono text-gray-500 feature-item">{{ feature.code }}=</span>
                                <span class="font-mono font-bold feature-item">{{ feature.intValue }}</span>
                            </span>
                            <span v-if="feature.version" class="mr-1">
                                <span class="font-mono text-gray-500 feature-item">{{ feature.code }}=</span>
                                <span class="font-mono font-bold feature-item">{{ feature.version }}</span>
                            </span>
                        </span>
                    </div>
                </template>
            </Column>
        </DataTable>
    </div>
    <div v-if="graphData.labels" class="mt-10">
        <!--        <DataTable :value="graphData.coverableConstraints">-->
        <!--            <Column field="numberOfConfigurations" :header="'numberOfConfigurations'" style="vertical-align: top"></Column>-->
        <!--            <Column field="maxCoverableConstraints" :header="'maxCoverableConstraints'" style="vertical-align: top"></Column>-->
        <!--        </DataTable>-->
        <Chart type="bar" :data="graphData" :options="chartOptions()" />
    </div>
    <div v-if="graphData.labels" class="text-gray-600 mt-5 text-sm">
        {{ $t('details.constraint_cover_desc') }}
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
                    backgroundColor: ['rgba(255, 195, 18, 0.6)'],
                    borderColor: ['rgb(255, 195, 18)'],
                    borderWidth: 1
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
.feature-container {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
}

.feature-item {
    padding: 2px 2px;
    border-radius: 4px;
}
</style>
