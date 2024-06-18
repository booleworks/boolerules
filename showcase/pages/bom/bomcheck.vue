<template>
    <div class="flex-col w-full">
        <AlgorithmHeader :header="$t('computation.pos_validity')" boolFeature enumFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.bom.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT', 'ANY', 'ALL']"
                    :only-single-slice="false" />
            </AccordionTab>
        </Accordion>

        <!-- Computation paramters & button -->
        <ClientOnly>
            <div class="flex-col">
                <div class="flex">
                    <Button :label="$t('algo.bom.btn_edit_bom')" icon="pi pi-table" severity="secondary"
                        class="mb-3 mr-3" @click="showBomDialog()" />
                    <div v-if="getPositions().value.length > 0" class="mb-3 content-center">
                        {{ $t('algo.bom.loaded_pvs') + ": " + getPositions().value[0].positionVariants.length }}
                    </div>
                    <div v-else class="mb-3 content-center">
                        {{ $t('algo.bom.no_position') }}
                    </div>
                </div>


                <ComputationParams additionalConstraints />
                <div class="flex">
                    <Button :label="$t('algo.bom.btn_compute_bom')" @click="compute()" icon="pi pi-desktop"
                        :disabled="!buttonActive" class="mt-3" />
                </div>
            </div>
        </ClientOnly>

        <Dialog v-model:visible="showBom" modal :header="$t('algo.bom.bom_position')" :style="{ width: '80vw' }"
            :dismissable-mask=true>
            <BomDialog v-on:close-dialog="closeDialog" />
        </Dialog>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" :enable-download="false" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <div class="flex flex-wrap items-center justify-end">
                        <Button :label="$t('details.btn_show')" icon="pi pi-info-circle" @click="showDetails()" />
                    </div>
                    <DataTable :value="result" scrollable showGridlines size="small" class="mt-3 pb-3">
                        <Column field="result.positionId" :header="$t('algo.bom.position')"></Column>
                        <Column field="result.description" :header="$t('algo.bom.description')"></Column>
                        <Column :header="$t('algo.bom.is_complete')">
                            <template #body="bdy">
                                <span v-if="bdy.data.result.isComplete" class="pi pi-check text-green-700"></span>
                                <span v-else class="pi pi-times text-red-700"></span>
                            </template>
                        </Column>
                        <Column :header="$t('algo.bom.has_non_unique_pvs')">
                            <template #body="bdy">
                                <span v-if="!bdy.data.result.hasNonUniquePVs" class="pi pi-check text-green-700"></span>
                                <span v-else class="pi pi-times text-red-700"></span>
                            </template>
                        </Column>
                        <Column :header="$t('algo.bom.has_dead_pvs')">
                            <template #body="bdy">
                                <span v-if="!bdy.data.result.hasDeadPvs" class="pi pi-check text-green-700"></span>
                                <span v-else class="pi pi-times text-red-700"></span>
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
                <div v-else class="text-gray-500 text">{{ $t('algo.nothing_computed') }}</div>
            </AccordionTab>
        </Accordion>
    </div>

    <Sidebar v-model:visible="detailView" position="right" style="width: 70rem;">
        <DetailPosVal />
    </Sidebar>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type ComputationStatus, type SingleComputationResponse, type ResultModel, type Position, } from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection } = useCurrentSliceSelection()
const { flattenResult, splitPropsSingleResult } = useResult()
const { getConstraintList } = useAdditionalConstraints()
const { getPositions } = useBom()
const { setJobId, initDetailSelection } = useComputation()

const buttonActive = computed(() => isPresent() && getPositions().value.length > 0)
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const showBom = ref(false)
const detailView = ref(false)

const result = ref({} as PosValResultModel[])
const status = ref({} as ComputationStatus)

// data types
type ComputationType = 'UNIQUENESS' | 'COMPLETENESS' | 'DEAD_PV'

type PosValRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
    computationTypes: ComputationType[]
    position: Position
}
type PosValResultModel = ResultModel<PosValResult>

type PosValResponse = SingleComputationResponse<PosValResult>

type PosValResult = {
    positionId: string
    description: string
    constraint: string
    isComplete: boolean
    hasNonUniquePVs: boolean
    hasDeadPvs: boolean
}

async function compute() {
    const request: PosValRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList(),
        computationTypes: ['UNIQUENESS', 'COMPLETENESS', 'DEAD_PV'],
        position: getPositions().value[0]
    }
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.posval, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const pvRes = res as PosValResponse
        openTopTabs.value = []
        openResultTabs.value = pvRes.status.success ? [1] : [0]
        setJobId(pvRes.status.jobId)
        status.value = pvRes.status
        result.value = flattenResult(pvRes.results) as PosValResultModel[]
    })
}

async function showBomDialog() {
    showBom.value = true
}

async function closeDialog() {
    showBom.value = false
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
