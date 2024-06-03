<template>
    <div>
        <DataTable :value="getCustomConstraints()" showGridlines class="p-datatable-sm mb-3" scrollable
            scrollHeight="600px">
            <Column field="constraint" :header="$t('common.constraint')" />
        </DataTable>
        <div class="flex flex-grow-1 align-items-center">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadConstraints"
                :chooseLabel="$t('algo.coverage.btn_upload_constraints')" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomConstraints, uploadCsv } = useConstraints()

const uploadConstraints = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
