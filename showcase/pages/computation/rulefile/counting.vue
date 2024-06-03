<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.counting')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.counting.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ALL']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-column">
                <ComputationParams additionalConstraints />
                <div class="flex">
                    <Button :label="$t('algo.counting.btn_compute')" @click="compute()" icon="pi pi-desktop"
                        :disabled="!buttonActive" />
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
                    <DataTable :value="result" resizableColumns showGridlines class="p-datatable-sm mt-3 pb-3">
                        <Column sortable field="result" :header="$t('result.header')" class="font-bold"
                            style="max-width: 8rem">
                            <template #body="bdy">
                                {{ bdy.data.result }}
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
</template>

<script setup lang="ts">
import {
    type SingleComputationResponse,
    type ComputationStatus,
    type ResultModel,
} from '~/types/computations'
import { type PropertySelection } from '~/types/rulefiles'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()

const buttonActive = computed(() => isPresent())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])

const result = ref({} as ModelCountResultModel[])
const status = ref({} as ComputationStatus)

// data types
type ModelCountResultModel = ResultModel<Number>

type ModelCountResponse = SingleComputationResponse<number>

type ModelCountRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
}

function compute() {
    const request: ModelCountRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList()
    }
    performComputation(request)
}

async function performComputation(request: ModelCountRequest) {
    $fetch(appConfig.counting, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const mcRes = res as ModelCountResponse
        openTopTabs.value = []
        openResultTabs.value = mcRes.status.success ? [1] : [0]
        status.value = mcRes.status
        result.value = flattenResult(mcRes.results, [-1]) as ModelCountResultModel[]
    })
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}

.feature-autocomplete :deep(.p-autocomplete-token-label) {
    margin-right: 5px !important;
}

.feature-autocomplete {
    max-width: 50% !important;
}
</style>
