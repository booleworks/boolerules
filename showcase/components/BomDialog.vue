<template>
    <div>
        <DataTable :value="getPositions().value" showGridlines size="small" class="mb-3" scrollable
            scrollHeight="600px">
            <Column field="positionId" :header="$t('algo.bom.id')" sortable />
            <Column field="description" :header="$t('algo.bom.description')" sortable />
            <Column field="constraint" :header="$t('algo.bom.constraint')" sortable />
        </DataTable>
        <div class="flex flex-grow items-center">
            <div class="flex flex-grow"></div>
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="positions[]"
                accept=".csv" customUpload @uploader="uploadBom" :chooseLabel="$t('algo.bom.btn_upload_bom')" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getPositions, uploadBomCsv } = useBom()

const uploadBom = async (event: any) => {
    uploadBomCsv(event.files[0] as File)
}
</script>
