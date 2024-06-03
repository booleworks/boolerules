<template>
    <div id="br-topbar" class="flex flex-grow-1 p-2 surface-100 border-bottom-1 surface-border">
        <ClientOnly>
            <div v-if="!isPresent()" class="flex flex-grow-1 align-items-center">
                {{ $t('rulefilebar.upload_first') }}
            </div>
            <div v-else class="flex flex-grow-1 align-items-center">
                <span class="font-bold">{{ $t('rulefilebar.filename') }}</span>
                <span>: {{ summary.fileName }}</span>
                <span class="ml-3 font-bold"># {{ $t('rulefilebar.rules') }}</span>
                <span>: {{ summary.numberOfRules }}</span>
                <span class="ml-3 font-bold"># {{ $t('rulefilebar.features') }}</span>
                <span>: {{ summary.numberOfFeatures }}</span>
                <FeatureTags class="ml-4" :boolFeature="summary.hasBooleanFeatures"
                    :enumFeature="summary.hasEnumFeatures" :intFeature="summary.hasIntFeatures" />
            </div>
            <div class="flex flex-none">
                <div class="flex">
                    <Button :icon="currentIcon()" class="mr-3" rounded @click="switchTheme()" />
                    <Button :label="$t('rulefilebar.management')" icon="pi pi-file" class="mr-3"
                        @click="fetchRuleFiles()" />
                    <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :url="appConfig.rulefile" class="mr-3"
                        :auto="true" :multiple="false" accept=".prl" @upload="onUpload" @error="onError"
                        :chooseLabel="!isPresent() ? $t('rulefilebar.btn_upload') : $t('rulefilebar.btn_upload_new')" />
                </div>
            </div>
            <Dialog v-model:visible="showDialog" modal :header="$t('upload.summary')" :style="{ width: '50vw' }">
                <UploadDialog :summary="summary" />
            </Dialog>
            <Dialog v-model:visible="showFileManagement" modal :header="$t('rulefilebar.management')"
                :style="{ width: '80vw' }">
                <FileManagementDialog :initial-files="ruleFiles" />
            </Dialog>
        </ClientOnly>
    </div>
</template>

<script setup lang="ts">
import {
    type FileUploadErrorEvent,
    type FileUploadUploadEvent,
} from 'primevue/fileupload'
import { type UploadSummary } from '~/types/rulefiles'

const appConfig = useAppConfig()
const { getSummary, setSummary, isPresent } = useCurrentRuleFile()
const { initSliceSelection } = useCurrentSliceSelection()
const { clearSelectedFeatures } = useFeatureSelection()
const { switchTheme, currentIcon } = useTheme()

const showDialog = ref(false)
const showFileManagement = ref(false)
const ruleFiles = ref([] as UploadSummary[])

const onError = (event: FileUploadErrorEvent) => {
    console.error(event)
    if (event.xhr.response) {
        const summary: UploadSummary = JSON.parse(event.xhr.response)
        setSummary(summary)
    } else {
        setSummary({} as UploadSummary)
    }
    showDialog.value = true
}

const onUpload = (event: FileUploadUploadEvent) => {
    const summary: UploadSummary = JSON.parse(event.xhr.response)
    console.log('successfully uploaded PRL file')
    console.log(summary)
    initSliceSelection(summary)
    setSummary(summary)
    clearSelectedFeatures()
    showDialog.value = true
}

async function fetchRuleFiles() {
    $fetch(appConfig.rulefile, { method: 'GET' }).then((res) => {
        ruleFiles.value = res as UploadSummary[]
        showFileManagement.value = true
    })
}

const summary = computed(() => getSummary())
</script>
