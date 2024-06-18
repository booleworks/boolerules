<template>
    <div>
        <div v-if="getCustomSoftware().value.length > 0">
            <DataTable :value="getCustomSoftware().value" size="small" showGridlines class="mb-3" scrollable
                scrollHeight="600px">
                <Column field="package" :header="$t('algo.packagesolving.package')" sortable>
                    <template #body="slotProps">
                        <div class="font-mono"> {{ slotProps.data.package }} </div>
                    </template>
                </Column>
                <Column field="version" :header="$t('algo.packagesolving.version')" sortable />
                <Column field="action" :header="$t('algo.packagesolving.action')" sortable />
            </DataTable>
        </div>
        <div v-else>
            {{ $t('algo.packagesolving.no_packages') }}
        </div>
        <div class="flex flex-grow items-center mt-5">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadSoftware"
                :chooseLabel="$t('algo.packagesolving.btn_upload_software')" />
            <div class="flex flex-grow"></div>
            <Button :label="$t('common.use_data')" icon="pi pi-check-circle" class="mr-2"
                :disabled="getCustomSoftware().value.length == 0" @click="closeDialog" severity="secondary" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomSoftware, uploadCsv } = useSoftware()
const emit = defineEmits()

function closeDialog() {
    emit('close-dialog')
}

const uploadSoftware = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
