<template>
    <div>
        <div v-if="getCustomConstraints().length > 0">
            <DataTable :value="getCustomConstraints()" size="small" showGridlines class="mb-3" scrollable
                scrollHeight="600px">
                <Column field="constraint" :header="$t('common.constraint')">
                    <template #body="slotProps">
                        <div class="font-mono"> {{ slotProps.data.constraint }} </div>
                    </template>
                </Column>
            </DataTable>
        </div>
        <div v-else>
            {{ $t('algo.coverage.no_constraints') }}
        </div>
        <div class="flex flex-grow items-center mt-5">
            <FileUpload mode="basic" uploadIcon="pi pi-cloud-upload" :auto="true" :multiple="false" name="weights[]"
                accept=".csv" customUpload @uploader="uploadConstraints"
                :chooseLabel="$t('algo.coverage.btn_upload_constraints')" />
            <div class="flex flex-grow"></div>
            <Button :label="$t('common.use_data')" icon="pi pi-check-circle" class="mr-2"
                :disabled="getCustomConstraints().length == 0" @click="closeDialog" severity="secondary" />
        </div>
    </div>
</template>

<script setup lang="ts">
const { getCustomConstraints, uploadCsv } = useConstraints()
const emit = defineEmits()

function closeDialog() {
    emit('close-dialog')
}

const uploadConstraints = async (event: any) => {
    uploadCsv(event.files[0] as File)
}
</script>
