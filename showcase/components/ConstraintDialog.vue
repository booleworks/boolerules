<template>
    <div>
        <DataTable :value="getCustomConstraints()" size="small" showGridlines class="mb-3" scrollable
            scrollHeight="600px">
            <Column field="constraint" :header="$t('common.constraint')">
                <template #body="slotProps">
                    <div class="constraint">
                        {{ slotProps.data.constraint }}
                    </div>
                </template>
            </Column>
        </DataTable>
        <div class="flex flex-grow items-center">
            <div class="flex flex-grow"></div>
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

<style scoped>
.constraint {
    font-family: monospace;
}
</style>
