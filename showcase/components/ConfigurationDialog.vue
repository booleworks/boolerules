<template>
    <div>
        <DataTable :value="getConfiguration().features" showGridlines class="p-datatable-sm mb-3"
                   scrollable scrollHeight="600px">
            <Column field="feature" sortable :header="$t('common.features')" />
        </DataTable>
        <div class="flex flex-grow-1 align-items-center">
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
