<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.constraintgraph')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.constraintgraph.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT']" :only-single-slice="true" />
            </AccordionTab>
        </Accordion>

        <!-- Computation parameters & button -->
        <ClientOnly>
            <div class="flex-column">
                <div class="flex">
                    <Button class="mt-2" :label="$t('algo.constraintgraph.btn_compute')" @click="compute()"
                            icon="pi pi-desktop" :disabled="!buttonActive" />
                </div>
            </div>
        </ClientOnly>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <ForceGraph :graph="resultGraph"/>
                </div>
                <div v-else class="text-600 text">{{ $t('algo.nothing_computed') }}</div>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type SingleComputationResponse, type ComputationStatus, type Graph, } from '~/types/computations'
import ForceGraph from "~/components/ForceGraph.vue";

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { setJobId, initDetailSelection, } = useComputation()
const { currentSliceSelection } = useCurrentSliceSelection()

const buttonActive = computed(() => isPresent())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])

const resultGraph = ref({} as Graph)
const status = ref({} as ComputationStatus)

// data types
type VisualizationRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
}

type VisualizationResponse = SingleComputationResponse<Graph>

async function compute() {
    const request: VisualizationRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
    }
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.constraintgraph, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const cRes = res as VisualizationResponse
        setJobId(cRes.status.jobId)
        openTopTabs.value = []
        openResultTabs.value = cRes.status.success ? [1] : [0]
        status.value = cRes.status
        resultGraph.value = cRes.results[0].result
    })
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}

.features-to-add {
    font-weight: 700 !important;
    color: var(--green-700) !important;
}

.features-to-remove {
    font-weight: 700 !important;
    color: var(--red-700) !important;
}
</style>
