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
        <div class="flex flex-grow items-center">
            <div class="flex flex-grow"></div>
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadConfiguration"
                :chooseLabel="$t('algo.reconfiguration.btn_upload_configuration')" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getConfiguration, uploadCsv } = useConfiguration()

const uploadConfiguration = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
