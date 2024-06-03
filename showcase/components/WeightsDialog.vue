<template>
    <div>
        <DataTable :value="getCustomWeights().value" showGridlines class="p-datatable-sm mb-3" scrollable
            scrollHeight="600px">
            <Column field="constraint" :header="$t('common.constraint')" sortable />
            <Column field="weight" :header="$t('algo.optimization.weighting')" sortable />
        </DataTable>
        <div class="flex flex-grow-1 align-items-center">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadWeights"
                :chooseLabel="$t('algo.optimization.btn_upload_weights')" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomWeights, uploadCsv } = useWeights()

const uploadWeights = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
