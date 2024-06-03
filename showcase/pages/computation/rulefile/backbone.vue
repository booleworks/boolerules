<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.backbone')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.backbone.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ALL']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-column">
                <ComputationParams additionalConstraints autocompleteFeatures />
                <div class="flex">
                    <Button :label="$t('algo.backbone.btn_compute')" @click="compute()" icon="pi pi-desktop"
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
                    <DataTable :value="result" rowGroupMode="rowspan" groupRowsBy="element" resizableColumns
                        columnResizeMode="expand" scrollable scrollHeight="flex" showGridlines sortField="element"
                        :sortOrder="1" class="mt-3 pb-3 p-datatable-sm">
                        <Column field="element" header="Feature" sortable>
                            <template #body="slotProps">
                                <div class="flex align-items-center gap-2">
                                    <FeatureSpan :feature="slotProps.data.element.content" :showTag="true" />
                                </div>
                            </template>
                        </Column>
                        <Column field="result" :header="$t('result.header')" class="font-bold">
                            <template #body="bdy">
                                <div v-if="bdy.data.result === 'FORBIDDEN'" class="text-red-700">
                                    {{ $t("algo.backbone.forbidden") }}
                                </div>
                                <div v-else-if="bdy.data.result === 'MANDATORY'" class="text-green-700">
                                    {{ $t("algo.backbone.mandatory") }}
                                </div>
                                <div v-else>{{ $t("algo.backbone.optional") }}</div>
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
                <div v-else class="text-600 text">{{ $t("algo.nothing_computed") }}</div>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script setup lang="ts">
import { type Feature, type PropertySelection } from "~/types/rulefiles"
import {
    type ListComputationResponse,
    type ComputationStatus,
    type ListResultModel,
} from "~/types/computations"

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenListResult, splitPropsListResult } = useResult()
const { getSelectedFeatures } = useFeatureSelection()
const { getConstraintList } = useAdditionalConstraints()

const buttonActive = computed(() => isPresent())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])

const result = ref([] as BackboneResultModel[])
const status = ref({} as ComputationStatus)

// data types
type BackboneResultModel = ListResultModel<BackboneType, Feature>

type BackboneType = "MANDATORY" | "FORBIDDEN" | "OPTIONAL"

type BackboneResponse = ListComputationResponse<BackboneType, Feature
>

type BackboneRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: String[]
    features: String[]
}

function compute() {
    const request: BackboneRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList(),
        features: getSelectedFeatures().value,
    }
    performComputation(request)
}

async function performComputation(request: BackboneRequest) {
    $fetch(appConfig.backbone, {
        method: "POST",
        body: request,
    }).then((res) => {
        const bbRes = res as BackboneResponse
        openTopTabs.value = []
        openResultTabs.value = bbRes.status.success ? [1] : [0]
        status.value = bbRes.status
        result.value = flattenListResult(bbRes.results) as BackboneResultModel[]
    })
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}
</style>
