<template>
    <div class="flex-column w-full">
        <AlgorithmHeader :header="$t('computation.packagesolving')" boolFeature enumFeature intFeature />

        <!-- Top panels -->
        <Accordion :multiple="true" :activeIndex="openTopTabs" class="mt-5 mr-3 mb-5">
            <AccordionTab :header="$t('common.algdesc')">
                <p style="width: 60%;" v-html="$t('algo.packagesolving.desc')" />
            </AccordionTab>

            <AccordionTab :header="$t('slices.selection')">
                <SliceSelection defaultSliceType="SPLIT" :allowedSliceTypes="['SPLIT']" :only-single-slice="true" />
            </AccordionTab>
        </Accordion>

        <!-- Computation parameters & button -->
        <ClientOnly>
            <div class="flex-column">
                <div class="flex">
                    <Button :label="$t('algo.packagesolving.btn_edit_software')" icon="pi pi-table" severity="secondary"
                        class="mb-3 mr-3" @click="showSoftwareDialog()" />
                    <div v-if="getCustomSoftware().value.length > 0" class="mb-3 align-content-center">
                        {{
                            $t('algo.packagesolving.loaded_packages') + ": " + getCustomSoftware().value.length
                        }}
                    </div>
                    <div v-else class="mb-3 align-content-center">
                        {{ $t('algo.packagesolving.no_packages') }}
                    </div>
                </div>
                <ComputationParams additionalConstraints />
                <div class="flex">
                    <Button class="mt-2" :label="$t('algo.packagesolving.btn_addremove')" @click="compute(false)"
                        icon="pi pi-desktop" :disabled="!buttonActive" />
                    <Button class="ml-5 mt-2" :label="$t('algo.packagesolving.btn_upgrade')" @click="compute(true)"
                        icon="pi pi-desktop" :disabled="!buttonActive" />
                </div>
            </div>
        </ClientOnly>

        <Dialog v-model:visible="showSoftware" modal :header="$t('algo.packagesolving.packages')"
            :style="{ width: '50vw' }" :dismissableMask="true">
            <SoftwareDialog />
        </Dialog>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" :enable-download="false" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <div>
                        <span class="features-to-add mr-1">{{ result.newFeatures.length }} </span>
                        <span class="mr-4">{{ $t('algo.packagesolving.add') }}</span>
                        <span class="features-to-upgrade mr-1">{{ upgraded() }} </span>
                        <span class="mr-4">{{ $t('algo.packagesolving.upgraded') }}</span>
                        <span class="features-to-downgrade mr-1">{{ downgraded() }} </span>
                        <span class="mr-3">{{ $t('algo.packagesolving.downgraded') }}</span>
                        <span class="features-to-remove mr-1">{{ result.removedFeatures.length }} </span>
                        <span class="mr-3">{{ $t('algo.packagesolving.remove') }}</span>
                    </div>
                    <div class="flex mt-2">
                        <DataTable v-if="result.newFeatures.length > 0" :value="result.newFeatures" showGridlines
                            class="p-datatable-sm mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.add')" class="font-bold"
                                style="width: 15rem">
                                <template #body="slotProps">
                                    <div class="features-to-add">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.version')" class="font-bold"
                                style="width: 5rem">
                                <template #body="slotProps">
                                    <div class="features-to-add">
                                        {{ slotProps.data.versionNew }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                        <DataTable v-if="result.changedFeatures.length > 0" :value="result.changedFeatures"
                            showGridlines class="p-datatable-sm mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.changed')" class="font-bold"
                                style="width: 15rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'features-to-upgrade': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'features-to-downgrade': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.old')" class="font-bold"
                                style="width: 5rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'features-to-upgrade': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'features-to-downgrade': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.versionOld }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.new')" class="font-bold"
                                style="width: 5rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'features-to-upgrade': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'features-to-downgrade': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.versionNew }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                        <DataTable v-if="result.removedFeatures.length > 0" :value="result.removedFeatures"
                            showGridlines class="p-datatable-sm mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.remove')" class="font-bold"
                                style="width: 15rem">
                                <template #body="slotProps">
                                    <div class="text-red-700">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.version')" class="font-bold"
                                style="width: 5rem">
                                <template #body="slotProps">
                                    <div class="text-red-700">
                                        {{ slotProps.data.versionOld }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </div>
                <div v-else class="text-600 text">{{ $t('algo.nothing_computed') }}</div>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script setup lang="ts">
import { type PropertySelection } from '~/types/rulefiles'
import { type SingleComputationResponse, type ComputationStatus, type SoftwareElement, } from '~/types/computations'

const appConfig = useAppConfig()
const { isPresent, getId } = useCurrentRuleFile()
const { currentSliceSelection, allSlicesSelected } = useCurrentSliceSelection()
const { getConstraintList } = useAdditionalConstraints()
const { setJobId, initDetailSelection, } = useComputation()
const { getCustomSoftware, uploadCsv } = useSoftware()

const buttonActive = computed(() => isPresent() && getCustomSoftware().value.length > 0 && allSlicesSelected())
const openTopTabs = ref([1])
const openResultTabs = ref([] as number[])
const showSoftware = ref(false)

const result = ref({} as PackageSolvingResult)
const status = ref({} as ComputationStatus)

// data types
type PackageSolvingRequest = {
    ruleFileId: string
    sliceSelection: PropertySelection[]
    additionalConstraints: string[]
    currentInstallation: string[]
    install: string[]
    remove: string[]
    update: boolean,
}

type PackageSolvingResult = {
    removedFeatures: VersionedFeature[]
    newFeatures: VersionedFeature[]
    changedFeatures: VersionedFeature[]
}

type VersionedFeature = {
    feature: string
    versionOld: Number
    versionNew: Number
}

type PackageSolvingResponse = SingleComputationResponse<PackageSolvingResult>

async function showSoftwareDialog() {
    showSoftware.value = true
}

async function compute(update: boolean) {
    const request: PackageSolvingRequest = {
        ruleFileId: getId(),
        sliceSelection: currentSliceSelection(),
        additionalConstraints: getConstraintList(),
        currentInstallation: [],
        install: [],
        remove: [],
        update: update
    }
    parseCurrentInstall(request)
    initDetailSelection(request.sliceSelection)
    $fetch(appConfig.packagesolving, {
        method: 'POST',
        body: request,
    }).then((res) => {
        const cRes = res as PackageSolvingResponse
        setJobId(cRes.status.jobId)
        openTopTabs.value = []
        openResultTabs.value = cRes.status.success ? [1] : [0]
        status.value = cRes.status
        result.value = cRes.results[0].result
    })
}

function parseCurrentInstall(req: PackageSolvingRequest) {
    const inst = getCustomSoftware().value
    inst.forEach(i => {
        switch (i.action) {
            case "INSTALLED":
                req.currentInstallation.push(parsePackage(i))
                break
            case "ADD":
                req.install.push(parsePackage(i))
                break
            case "REMOVE":
                req.remove.push(parsePackage(i))
                break
        }
    })
}

function parsePackage(elem: SoftwareElement): string {
    if (elem.version == "") {
        return elem.package
    } else {
        return elem.package + "[=" + elem.version + "]"
    }
}

function upgraded(): Number {
    return result.value.changedFeatures.filter(it => { return it.versionOld < it.versionNew }).length
}
function downgraded(): Number {
    return result.value.changedFeatures.filter(it => { return it.versionOld > it.versionNew }).length
}
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}

.features-to-add {
    font-weight: 700 !important;
    color: var(--green-700) !important;
}

.features-to-remove {
    font-weight: 700 !important;
    color: var(--red-700) !important;
}

.features-to-upgrade {
    font-weight: 700 !important;
    color: var(--blue-700) !important;
}

.features-to-downgrade {
    font-weight: 700 !important;
    color: var(--orange-700) !important;
}
</style>
