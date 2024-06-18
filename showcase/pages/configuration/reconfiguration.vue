<template>
    <div class="flex-col w-full">
        <AlgorithmHeader :header="$t('computation.reconfiguration')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.reconfiguration.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT']" :only-single-slice="true" />
            </AccordionTab>
        </Accordion>

        <!-- Computation parameters & button -->
        <ClientOnly>
            <div class="flex-col">
                <div class="flex">
                    <Button :label="$t('algo.reconfiguration.btn_edit_configuration')" icon="pi pi-table"
                        severity="secondary" class="mb-3 mr-3" @click="showConfigurationDialog()" />
                    <div v-if="getConfiguration().features.length > 0" class="mb-3 content-center">
                        {{
                            $t('algo.reconfiguration.loaded_configuration') + ": " + getConfiguration().features.length
                        }}
                    </div>
                    <div v-else class="mb-3 content-center">
                        {{ $t('algo.reconfiguration.no_configuration') }}
                    </div>
                </div>
                <ComputationParams additionalConstraints />
                <table class="flex">
                    <tr>
                        <td style="padding-right: 20px">{{ $t('algo.reconfiguration.algorithm') }}</td>
                        <td>
                            <Dropdown v-model="algorithm" :options="['MAX_COV', 'MIN_DIFF']" />
                        </td>
                    </tr>
                </table>
                <div class="flex">
                    <Button class="mt-3" :label="$t('algo.reconfiguration.btn_compute')" @click="compute()"
                        icon="pi pi-desktop" :disabled="!buttonActive" />
                </div>
            </div>
        </ClientOnly>

        <Dialog v-model:visible="showConfiguration" modal :header="$t('algo.reconfiguration.configuration')"
            :style="{ width: '50vw' }" :dismissableMask="true">
            <ConfigurationDialog v-on:close-dialog="closeDialog" />
        </Dialog>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" :enable-download="false" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <div>
                        <span class="font-bold text-green-700 mr-1">{{ result.featuresToAdd.length }} </span>
                        <span class="mr-3">{{ $t('algo.reconfiguration.features_to_add') }}</span>
                        <span class="font-bold text-red-700 mr-1">{{ result.featuresToRemove.length }} </span>
                        <span>{{ $t('algo.reconfiguration.features_to_remove') }}</span>
                    </div>
                    <div class="flex mt-2">
                        <DataTable :value="result.featuresToAdd" size="small" showGridlines class="mt-3 pb-3 mr-3"
                            sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.reconfiguration.features_to_add')" style="width: 20rem">
                                <template #body="slotProps">
                                    <div class="font-mono text-green-700"> {{ slotProps.data }} </div>
                                </template>
                            </Column>
                        </DataTable>
                        <DataTable :value="result.featuresToRemove" size="small" showGridlines class="mt-3 pb-3"
                            sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.reconfiguration.features_to_remove')"
                                style="width: 20rem">
                                <template #body="slotProps">
                                    <div class="font-mono text-red-700"> {{ slotProps.data }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </div>
                <div v-else class="text-gray-500 text">{{ $t('algo.nothing_computed') }}</div>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type SingleComputationResponse, type ComputationStatus, } from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection, allSlicesSelected } = useCurrentSliceSelection()
const { getConstraintList } = useAdditionalConstraints()
const { setJobId, initDetailSelection, } = useComputation()
const { getConfiguration } = useConfiguration()

const buttonActive = computed(() => isPresent() && getConfiguration().features && allSlicesSelected())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const showConfiguration = ref(false)
const algorithm = ref('MAX_COV' as ReconfigurationAlgorithm)

const result = ref({} as ReconfigurationResult)
const status = ref({} as ComputationStatus)

// data types
type ReconfigurationRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
    configuration: string[]
    algorithm: ReconfigurationAlgorithm
}

type ReconfigurationAlgorithm = 'MAX_COV' | 'MIN_DIFF'

type ReconfigurationResult = {
    featuresToRemove: string[],
    featuresToAdd: string[],
}

type ReconfigurationResponse = SingleComputationResponse<ReconfigurationResult>

async function showConfigurationDialog() {
    showConfiguration.value = true
}

async function closeDialog() {
    showConfiguration.value = false
}

async function compute() {
    const request: ReconfigurationRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList(),
        configuration: getConfiguration().features.map(f => f.feature),
        algorithm: algorithm.value
    }
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.reconfiguration, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const cRes = res as ReconfigurationResponse
        setJobId(cRes.status.jobId)
        openResultTabs.value = cRes.status.success ? [1] : [0]
        status.value = cRes.status
        result.value = cRes.results[0].result
    })
}
</script>
