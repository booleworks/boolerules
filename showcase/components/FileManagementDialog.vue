<template>
    <div>
        <DataTable :value="ruleFiles" v-model:selection="selectedFile" showGridlines class="p-datatable-sm mb-3"
            scrollable scrollHeight="600px" selectionMode="single" sortField="timestamp" :sortOrder="-1">
            <Column field="fileName" :header="$t('rulefilemgmt.filename')" sortable />
            <Column field="timestamp" :header="$t('rulefilemgmt.uploaded')" sortable>
                <template #body="bdy">
                    {{ formatTimestamp(bdy.data.timestamp) }}
                </template>
            </Column>
            <Column field="numberOfFeatures" :header="$t('rulefilemgmt.features')" sortable />
            <Column field="numberOfRules" :header="$t('rulefilemgmt.rules')" sortable />
            <Column field="hasBooleanFeatures" :header="$t('rulefilemgmt.bool_features')" sortable style="width: 8rem">
                <template #body="bdy">
                    <span v-if="bdy.data.hasBooleanFeatures" class="pi pi-check text-green-500"></span>
                    <span v-else class="pi pi-times text-red-500"></span>
                </template>
            </Column>
            <Column field="hasEnumFeatures" :header="$t('rulefilemgmt.enum_features')" sortable style="width: 8rem">
                <template #body="bdy">
                    <span v-if="bdy.data.hasEnumFeatures" class="pi pi-check text-green-500"></span>
                    <span v-else class="pi pi-times text-red-500"></span>
                </template>
            </Column>
            <Column field="hasIntFeatures" :header="$t('rulefilemgmt.int_features')" sortable style="width: 8rem">
                <template #body="bdy">
                    <span v-if="bdy.data.hasIntFeatures" class="pi pi-check text-green-500"></span>
                    <span v-else class="pi pi-times text-red-500"></span>
                </template>
            </Column>
            <Column field="id" :header="$t('rulefilemgmt.id')" sortable />
            <template #footer> # {{ $t('rulefilemgmt.stored') }}: {{ ruleFiles.length }} </template>
        </DataTable>

        <div class="flex flex-grow-1 align-items-center">
            <div class="flex flex-grow-1"></div>
            <Button :label="$t('rulefilemgmt.btn_load')" icon="pi pi-cloud-download" severity="success" class="mr-2"
                :disabled="!selectedFile.id" @click="selectFile" />
            <Button :label="$t('rulefilemgmt.btn_delete')" icon="pi pi-trash" severity="danger"
                :disabled="!selectedFile.id" @click="deleteRuleFile" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { type UploadSummary } from '~/types/rulefiles'

const appConfig = useAppConfig()
const { setSummary } = useCurrentRuleFile()
const { initSliceSelection } = useCurrentSliceSelection()
const { clearSelectedFeatures } = useFeatureSelection()

const props = defineProps<{ initialFiles: UploadSummary[] }>()
const ruleFiles = ref(props.initialFiles)

const selectedFile = ref({} as UploadSummary)

function formatTimestamp(timestamp: string): string {
    return new Date(timestamp).toLocaleString('de-DE')
}

function selectFile() {
    initSliceSelection(selectedFile.value)
    setSummary(selectedFile.value)
    clearSelectedFeatures()
}

async function fetchRuleFiles() {
    $fetch(appConfig.rulefile, { method: 'GET' }).then((res) => {
        ruleFiles.value = res as UploadSummary[]
    })
}

async function deleteRuleFile() {
    $fetch(appConfig.rulefile + "/" + selectedFile.value.id, {
        method: 'DELETE',
    }).then(() => {
        console.log('Deleted file with ID ' + selectedFile.value.id)
        selectedFile.value = {} as UploadSummary
        fetchRuleFiles()
    })
}
</script>
