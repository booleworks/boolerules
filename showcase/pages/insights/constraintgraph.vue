<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.constraint_graph')" boolFeature enumFeature intFeature />

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
                <ComputationStatusTab :status="status" :enable-download="false" />
            </AccordionTab>
        </Accordion>

        <Dialog id="GraphDialog" v-model:visible="showGraph" modal :dismissable-mask=true :show-header="false"
            :style="{ width: '80vw', height: '80vh' }">
            <ForceGraph :graph="resultGraph" :relWidth=.8 :relHeight=.8 />
        </Dialog>
    </div>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type SingleComputationResponse, type ComputationStatus, type Graph, } from '~/types/computations'
import ForceGraph from "~/components/ForceGraph.vue";

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { setJobId, initDetailSelection, } = useComputation()
const { currentSliceSelection, allSlicesSelected } = useCurrentSliceSelection()

const buttonActive = computed(() => isPresent() && allSlicesSelected())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])

const resultGraph = ref({} as Graph)
const status = ref({} as ComputationStatus)

const showGraph = ref(false)

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
        // openTopTabs.value = []
        openResultTabs.value = [0]
        status.value = cRes.status
        if (cRes.status.success) {
            resultGraph.value = cRes.results[0].result
            showGraph.value = true
        }
    })
}
</script>

<style>
#GraphDialog .p-dialog-content {
    padding: 0 !important;
    margin: 0 !important;
    overflow: hidden !important;
}
</style>
