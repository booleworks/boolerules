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
        <div class="flex flex-grow items-center">
            <div class="flex flex-grow"></div>
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
