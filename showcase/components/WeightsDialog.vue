<template>
    <div>
        <div v-if="getCustomWeights().value.length > 0">
            <DataTable :value="getCustomWeights().value" size="small" showGridlines class="mb-3" scrollable
                scrollHeight="600px">
                <Column field="constraint" :header="$t('common.constraint')" sortable>
                    <template #body="slotProps">
                        <div class="font-mono"> {{ slotProps.data.constraint }} </div>
                    </template>
                </Column>
                <Column field="weight" :header="$t('algo.optimization.weighting')" sortable />
            </DataTable>
        </div>
        <div v-else>
            {{ $t('algo.optimization.no_weights') }}
        </div>
        <div class="flex flex-grow items-center mt-5">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadWeights"
                :chooseLabel="$t('algo.optimization.btn_upload_weights')" />
            <div class="flex flex-grow"></div>
            <Button :label="$t('common.use_data')" icon="pi pi-check-circle" class="mr-2"
                :disabled="getCustomWeights().value.length == 0" @click="closeDialog" severity="secondary" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomWeights, uploadCsv } = useWeights()
const emit = defineEmits()

function closeDialog() {
    emit('close-dialog')
}

const uploadWeights = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
