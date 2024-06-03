<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.consistency')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.consistency.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ANY', 'ALL']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-column">
                <ComputationParams additionalConstraints />
                <div class="flex">
                    <Button class="mt-2" :label="$t('algo.consistency.btn_compute')" @click="compute()"
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
                    <DataTable :value="result" scrollable showGridlines class="p-datatable-sm mt-3 pb-3">
                        <template #header>
                            <div class="flex flex-wrap align-items-center justify-content-end">
                                <Button :label="$t('details.btn_show')" icon="pi pi-info-circle"
                                    @click="showDetails()" />
                            </div>
                        </template>
                        <Column field="result" sortable :header="$t('result.header')" class="font-bold"
                            style="width: 15rem">
                            <template #body="bdy">
                                <div v-if="bdy.data.result" class="text-green-700">
                                    {{ $t('algo.consistency.consistent') }}
                                </div>
                                <div v-else class="text-red-700">
                                    {{ $t('algo.consistency.inconsistent') }}
                                </div>
                            </template>
                        </Column>
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
        <DetailConsistency />
    </Sidebar>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import {
    type SingleComputationResponse,
    type ComputationStatus,
    type ResultModel,
} from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { setJobId, initDetailSelection, } = useComputation()

const buttonActive = computed(() => isPresent())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const detailView = ref(false)

const result = ref({} as ConsistencyResultModel[])
const status = ref({} as ComputationStatus)

// data types
type ConsistencyRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
}

type ConsistencyResponse = SingleComputationResponse<Boolean>
type ConsistencyResultModel = ResultModel<Boolean>

async function compute() {
    const request: ConsistencyRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList()
    }
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.consistency, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const cRes = res as ConsistencyResponse
        setJobId(cRes.status.jobId)
        openTopTabs.value = []
        openResultTabs.value = cRes.status.success ? [1] : [0]
        status.value = cRes.status
        result.value = flattenResult(cRes.results) as ConsistencyResultModel[]
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
