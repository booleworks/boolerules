<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.weights')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.optimization.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ANY', 'ALL']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-column">
                <div class="flex">
                    <Button :label="$t('algo.optimization.btn_edit_weights')" icon="pi pi-table" severity="warning"
                        class="mb-3 mr-3" @click="showWeightsDialog()" />
                    <div v-if="getCustomWeights().value.length > 0" class="mb-3 align-content-center">
                        {{ $t('algo.optimization.loaded_weights') + ": " + getCustomWeights().value.length }}
                    </div>
                    <div v-else class="mb-3 align-content-center">
                        {{ $t('algo.optimization.no_weights') }}
                    </div>
                </div>
                <ComputationParams weights additionalConstraints />
                <div class="flex">
                    <div class="flex align-items-center">
                        <Button :label="$t('algo.optimization.btn_compute_min')" @click="compute(false)"
                            icon="pi pi-desktop" :disabled="!buttonActive" />
                        <Button class="ml-5" :label="$t('algo.optimization.btn_compute_max')" @click="compute(true)"
                            icon="pi pi-desktop" :disabled="!buttonActive" />
                    </div>
                </div>
            </div>
        </ClientOnly>

        <Dialog v-model:visible="showWeights" modal :header="$t('algo.optimization.weightings')"
            :style="{ width: '80vw' }">
            <WeightsDialog />
        </Dialog>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <DataTable :value="result" resizableColumns showGridlines class="p-datatable-sm mt-3 pb-3"
                        sortField="result" :sortOrder="1">
                        <template #header>
                            <div class="flex flex-wrap align-items-center justify-content-end">
                                <Button :label="$t('details.btn_show')" icon="pi pi-info-circle"
                                    @click="showDetails()" />
                            </div>
                        </template>
                        <Column sortable field="result" :header="$t('result.header')" class="font-bold"
                            style="width: 15rem" />
                        <Column v-for="(col, index) in splitPropsSingleResult(result)" :key="col"
                            :header="$t('result.property') + ' ' + col">
                            <template #body="bdy">
                                <SlicePropertyColumn :property="bdy.data.slice.content[index]" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
                <div v-else class="text-600 text">{{ $t('algo.nothing_computed') }}</div>
            </AccordionTab>
        </Accordion>
    </div>

    <Sidebar v-model:visible="detailView" position="right" class="" style="width: 50rem;">
        <DetailOptimization />
    </Sidebar>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import {
    type SingleComputationResponse,
    type ComputationStatus,
    type ResultModel,
    type WeightPair,
} from '~/types/computations'
import useComputation from '~/composables/useComputation';

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { getCustomWeights } = useWeights()
const { setJobId, initDetailSelection, } = useComputation()

const buttonActive = computed(() => isPresent() && getCustomWeights().value.length > 0)
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const detailView = ref(false)
const showWeights = ref(false)

const result = ref({} as MinMaxResultModel[])
const status = ref({} as ComputationStatus)

// data types
type MinMaxType = 'MIN' | 'MAX'
type MinMaxResultModel = ResultModel<Number>

type MinMaxResponse = SingleComputationResponse<Number>

type MinMaxRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    computationType: MinMaxType
    weightings: WeightPair[]
    additionalConstraints: string[]
}

async function showWeightsDialog() {
    showWeights.value = true
}

async function compute(max: boolean) {
    let request: MinMaxRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        computationType: 'MIN',
        weightings: getCustomWeights().value,
        additionalConstraints: getConstraintList()
    }
    initDetailSelection(request.sliceSelection)
    request.computationType = max ? 'MAX' : 'MIN'
    $fetch(appConfig.optimization, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const mmRes = res as MinMaxResponse
        setJobId(mmRes.status.jobId)
        openTopTabs.value = []
        openResultTabs.value = mmRes.status.success ? [1] : [0]
        status.value = mmRes.status
        result.value = flattenResult(mmRes.results, [-1]) as MinMaxResultModel[]
    })
}

function showDetails() {
    detailView.value = true
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}
</style>
