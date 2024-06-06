<template>
    <div>
        <DataTable :value="getCustomSoftware().value" showGridlines class="p-datatable-sm mb-3" scrollable
            scrollHeight="600px">
            <Column field="package" :header="$t('algo.packagesolving.package')" sortable>
                <template #body="slotProps">
                    <div class="constraint">
                        {{ slotProps.data.package }}
                    </div>
                </template>
            </Column>
            <Column field="version" :header="$t('algo.packagesolving.version')" sortable />
            <Column field="action" :header="$t('algo.packagesolving.action')" sortable />
        </DataTable>
        <div class="flex flex-grow-1 align-items-center">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadSoftware"
                :chooseLabel="$t('algo.packagesolving.btn_upload_software')" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomSoftware, uploadCsv } = useSoftware()

const uploadSoftware = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>

<style scoped>
.constraint {
    font-family: monospace;
}
</style>
