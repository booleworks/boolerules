<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.coverage')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.coverage.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT']" :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation parameters & button -->
        <ClientOnly>
            <div class="flex-column">
                <div class="flex">
                    <Button :label="$t('algo.coverage.btn_edit_constraints')" icon="pi pi-table" severity="warning"
                        class="mb-3 mr-3" @click="showConstraintDialog()" />
                    <div v-if="getCustomConstraintsAsStrings().length > 0" class="mb-3 align-content-center">
                        {{ $t('algo.coverage.loaded_constraints') + ": " + getCustomConstraintsAsStrings().length }}
                    </div>
                    <div v-else class="mb-3 align-content-center">
                        {{ $t('algo.coverage.no_constraints') }}
                    </div>
                </div>
                <ComputationParams additionalConstraints />
                <div class="flex">
                    <Button class="mt-2" :label="$t('algo.coverage.btn_compute')" @click="compute()"
                        icon="pi pi-desktop" :disabled="!buttonActive" />
                </div>
            </div>
        </ClientOnly>

        <Dialog v-model:visible="showConstraints" modal :header="$t('algo.coverage.constraints')"
            :style="{ width: '80vw' }">
            <ConstraintDialog />
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
                        <Column sortable field="result.requiredConfigurations"
                            :header="$t('algo.coverage.header_required_configurations')" class="font-bold"
                            style="width: 10rem" />
                        <Column sortable field="result.uncoverableConstraints"
                            :header="$t('algo.coverage.header_uncoverable_constraints')" class="font-bold"
                            style="width: 10rem" />
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
        <DetailCoverage />
    </Sidebar>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type SingleComputationResponse, type ComputationStatus, type ResultModel, } from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { setJobId, initDetailSelection, } = useComputation()
const { getCustomConstraintsAsStrings } = useConstraints()

const buttonActive = computed(() => isPresent() && getCustomConstraintsAsStrings().length > 0)
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const detailView = ref(false)
const showConstraints = ref(false)

const result = ref({} as CoverageResultModel[])
const status = ref({} as ComputationStatus)

// data types
type CoverageRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
    constraintsToCover: string[]
    pairwiseCover: boolean
}

type CoverageMainResult = {
    requiredConfigurations: number,
    uncoverableConstraints: number,
}

type CoverageResponse = SingleComputationResponse<number>
type CoverageResultModel = ResultModel<CoverageMainResult>

async function showConstraintDialog() {
    showConstraints.value = true
}

async function compute() {
    const request: CoverageRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList(),
        constraintsToCover: getCustomConstraintsAsStrings(),
        pairwiseCover: false
    }
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.coverage, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const cRes = res as CoverageResponse
        setJobId(cRes.status.jobId)
        openTopTabs.value = []
        openResultTabs.value = cRes.status.success ? [1] : [0]
        status.value = cRes.status
        result.value = flattenResult(cRes.results) as CoverageResultModel[]
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
