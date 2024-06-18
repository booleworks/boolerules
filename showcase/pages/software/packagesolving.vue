<template>
    <div class="flex-col w-full">
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
            <div class="flex-col">
                <div class="flex">
                    <Button :label="$t('algo.packagesolving.btn_edit_software')" icon="pi pi-table" severity="secondary"
                        class="mb-3 mr-3" @click="showSoftwareDialog()" />
                    <div v-if="getCustomSoftware().value.length > 0" class="mb-3 content-center">
                        {{
                            $t('algo.packagesolving.loaded_packages') + ": " + getCustomSoftware().value.length
                        }}
                    </div>
                    <div v-else class="mb-3 content-center">
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
            <SoftwareDialog v-on:close-dialog="closeDialog" />
        </Dialog>

        <!-- Result panels -->
        <Accordion :multiple="true" :activeIndex="openResultTabs" class="mt-5 mr-3">
            <AccordionTab :header="$t('common.result_status')">
                <ComputationStatusTab :status="status" :enable-download="false" />
            </AccordionTab>

            <AccordionTab :header="$t('result.header')">
                <div v-if="status.success">
                    <div>
                        <span class="font-bold text-green-700 mr-1">{{ result.newFeatures.length }} </span>
                        <span class="mr-4">{{ $t('algo.packagesolving.add') }}</span>
                        <span class="font-bold text-blue-700 mr-1">{{ upgraded() }} </span>
                        <span class="mr-4">{{ $t('algo.packagesolving.upgraded') }}</span>
                        <span class="font-bold text-orange-700 mr-1">{{ downgraded() }} </span>
                        <span class="mr-3">{{ $t('algo.packagesolving.downgraded') }}</span>
                        <span class="font-bold text-red-700 mr-1">{{ result.removedFeatures.length }} </span>
                        <span class="mr-3">{{ $t('algo.packagesolving.remove') }}</span>
                    </div>
                    <div class="flex mt-2">
                        <DataTable v-if="result.newFeatures.length > 0" :value="result.newFeatures" showGridlines
                            size="small" class="mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.add')" style="width: 15rem">
                                <template #body="slotProps">
                                    <div class="font-mono text-green-700">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.version')" style="width: 5rem">
                                <template #body="slotProps">
                                    <div class="text-green-700">
                                        {{ slotProps.data.versionNew }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                        <DataTable v-if="result.changedFeatures.length > 0" :value="result.changedFeatures" size="small"
                            showGridlines class="mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.changed')" style="width: 15rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'font-mono text-blue-700': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'font-mono text-orange-700': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.old')" style="width: 5rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'text-blue-700': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'text-orange-700': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.versionOld }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.new')" style="width: 5rem">
                                <template #body="slotProps">
                                    <div :class="{
                                        'text-blue-700': slotProps.data.versionNew > slotProps.data.versionOld,
                                        'text-orange-700': slotProps.data.versionNew < slotProps.data.versionOld,
                                    }">
                                        {{ slotProps.data.versionNew }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                        <DataTable v-if="result.removedFeatures.length > 0" :value="result.removedFeatures"
                            showGridlines class="p-datatable-sm mt-3 pb-3 mr-3" sortField="result" :sortOrder="1">
                            <Column sortable :header="$t('algo.packagesolving.remove')" style="width: 15rem">
                                <template #body="slotProps">
                                    <div class="text-red-700">
                                        {{ slotProps.data.feature }}
                                    </div>
                                </template>
                            </Column>
                            <Column sortable :header="$t('algo.packagesolving.version')" style="width: 5rem">
                                <template #body="slotProps">
                                    <div class="text-red-700">
                                        {{ slotProps.data.versionOld }}
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

async function closeDialog() {
    showSoftware.value = false
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
