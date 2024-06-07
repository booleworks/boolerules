<template>
    <div id="br-topbar" class="flex flex-grow p-2 bg-white/50 backdrop-blur-sm border-b border-gray-200">
        <ClientOnly>
            <div v-if="!isPresent()" class="flex flex-grow items-center">
                {{ $t('rulefilebar.upload_first') }}
            </div>
            <div v-else class="flex flex-grow items-center">
                <span class="font-bold">{{ $t('rulefilebar.filename') }}</span>
                <span>: {{ summary.fileName }}</span>
                <span class="ml-3 font-bold"># {{ $t('rulefilebar.features') }}</span>
                <span>: {{ summary.numberOfFeatures }}</span>
                <FeatureTags class="ml-4" :boolFeature="summary.hasBooleanFeatures"
                    :enumFeature="summary.hasEnumFeatures" :intFeature="summary.hasIntFeatures" />
            </div>
            <div class="flex flex-none">
                <div class="flex content-center">
                    <Button :icon="currentIcon()" class="mr-3" rounded @click="switchTheme()" />
                    <Button :label="$t('rulefilebar.management')" icon="pi pi-file" class="mr-3"
                        @click="fetchRuleFiles()" />
                </div>
            </div>
            <Dialog v-model:visible="showFileManagement" modal :header="$t('rulefilebar.management')"
                :style="{ width: '80vw' }" :dismissableMask="true">
                <FileManagementDialog :initial-files="ruleFiles" />
            </Dialog>
        </ClientOnly>
    </div>
</template>

<script setup lang="ts">
import { type UploadSummary } from '~/types/rulefiles'

const appConfig = useAppConfig()
const { getSummary, isPresent } = useCurrentRuleFile()
const { switchTheme, currentIcon } = useTheme()

const showFileManagement = ref(false)
const ruleFiles = ref([] as UploadSummary[])

async function fetchRuleFiles() {
    $fetch(appConfig.rulefile, { method: 'GET' }).then((res) => {
        ruleFiles.value = res as UploadSummary[]
        showFileManagement.value = true
    })
}

const summary = computed(() => getSummary())
</script>
