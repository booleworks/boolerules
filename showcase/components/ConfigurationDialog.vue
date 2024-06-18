<template>
    <div>
        <div v-if="getConfiguration().features.length > 0">
            <DataTable :value="getConfiguration().features" showGridlines size="small" class="mb-3" scrollable
                scrollHeight="600px">
                <Column field="feature" sortable :header="$t('common.features')">
                    <template #body="slotProps">
                        <div class="font-mono"> {{ slotProps.data.feature }} </div>
                    </template>
                </Column>
            </DataTable>
        </div>
        <div v-else>
            {{ $t('algo.reconfiguration.no_configuration') }}
        </div>
        <div class="flex flex-grow items-center mt-5">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadConfiguration"
                :chooseLabel="$t('algo.reconfiguration.btn_upload_configuration')" />
            <div class="flex flex-grow"></div>
            <Button :label="$t('common.use_data')" icon="pi pi-check-circle" class="mr-2"
                :disabled="getConfiguration().features.length == 0" @click="closeDialog" severity="secondary" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getConfiguration, uploadCsv } = useConfiguration()
const emit = defineEmits()

function closeDialog() {
    emit('close-dialog')
}

async function uploadConfiguration(event: any) {
    uploadCsv(event.files[0] as File)
}
</script>
