<template>
    <div class="flex-col w-full">
        <AlgorithmHeader :header="$t('computation.minmax')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.minmax.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ANY', 'ALL']"
                    :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-col">
                <ComputationParams additionalConstraints />
                <div class="flex">
                    <div class="flex items-center">
                        <Button class="mt-2" :label="$t('algo.minmax.btn_compute_min')" @click="compute(false)"
                            icon="pi pi-desktop" :disabled="!buttonActive" />
                        <Button class="mt-2 ml-5" :label="$t('algo.minmax.btn_compute_max')" @click="compute(true)"
                            icon="pi pi-desktop" :disabled="!buttonActive" />
                    </div>
                </div>
            </div>
        </ClientOnly>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" :enable-download="true" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <div class="flex flex-wrap items-center justify-end">
                        <Button :label="$t('details.btn_show')" icon="pi pi-info-circle" @click="showDetails()" />
                    </div>
                    <DataTable :value="result" resizableColumns showGridlines size="small" class="mt-3 pb-3"
                        sortField="result" :sortOrder="1">
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
        <DetailMinMax />
    </Sidebar>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import {
    type SingleComputationResponse,
    type ComputationStatus,
    type ResultModel,
} from '~/types/computations'
import useComputation from '~/composables/useComputation';

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { initDetailSelection, setJobId } = useComputation()

const buttonActive = computed(() => isPresent())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const detailView = ref(false)

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
    features: string[]
    additionalConstraints: string[]
}

async function compute(max: boolean) {
    const request: MinMaxRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        computationType: 'MIN',
        features: [],
        additionalConstraints: getConstraintList()
    }

    initDetailSelection(request.sliceSelection)
    request.computationType = max ? 'MAX' : 'MIN'
    $fetch(appConfig.minmax, {
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
