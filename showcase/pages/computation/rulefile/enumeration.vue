<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.enumeration')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.enumeration.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ANY', 'ALL']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-column">
                <ComputationParams additionalConstraints autocompleteFeatures />
                <div class="flex">
                    <Button :label="$t('algo.enumeration.btn_compute')" @click="compute()" icon="pi pi-desktop"
                        :disabled="!buttonActive" />
                </div>
            </div>
        </ClientOnly>

        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <DataTable :value="result" rowGroupMode="rowspan" groupRowsBy="element" resizableColumns
                        columnResizeMode="expand" showGridlines sortField="element" :sortOrder="1"
                        class="p-datatable-sm mt-3 pb-3">
                        <Column field="element" :header="$t('algo.enumeration.combination')">
                            <template #body="slotProps">
                                <div class="flex align-items-center gap-2">
                                    <FeatureModelColumn :model="slotProps.data.element.content" />
                                </div>
                            </template>
                        </Column>
                        <Column v-for="(col, index) in splitPropsListResult(result)" :key="col"
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
import { type PropertySelection } from '~/types/rulefiles'
import {
    type ListComputationResponse,
    type ComputationStatus,
    type ListResultModel,
    type FeatureModel,
} from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenListResult, splitPropsListResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { getSelectedFeatures } = useFeatureSelection()

const buttonActive = computed(() => isPresent() && getSelectedFeatures().value.length > 0)
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])

const result = ref({} as EnumerationResultModel[])
const status = ref({} as ComputationStatus)

// data types
type EnumerationResultModel = ListResultModel<Boolean, FeatureModel>

type EnumerationResponse = ListComputationResponse<Boolean, FeatureModel>

type EnumerationRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    features: String[]
    additionalConstraints: string[]
}

function compute() {
    const request: EnumerationRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        features: getSelectedFeatures().value,
        additionalConstraints: getConstraintList()
    }
    performComputation(request)
}

async function performComputation(request: EnumerationRequest) {
    $fetch(appConfig.enumeration, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const meRes = res as EnumerationResponse
        openTopTabs.value = []
        openResultTabs.value = meRes.status.success ? [1] : [0]
        status.value = meRes.status
        result.value = flattenListResult(meRes.results).filter(
            (item) => item.result,
        ) as EnumerationResultModel[]
    })
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}
</style>
