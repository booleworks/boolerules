<template>
    <div>
        <div v-if="getPositions().value.length > 0">
            <span class="font-bold">{{ $t('algo.bom.position') + ': ' }}</span>
            <span class="font-mono mr-5">{{ firstVal().positionId }}</span>
            <span class="font-bold">{{ $t('algo.bom.description') + ': ' }}</span>
            <span class="mr-5">{{ firstVal().description }}</span>
            <span v-if="firstVal().constraint" class="font-bold">{{ $t('algo.bom.constraint') + ': ' }}</span>
            <span v-if="firstVal().constraint" class="font-mono">{{ firstVal().constraint }}</span>
            <DataTable :value="getPositions().value[0].positionVariants" showGridlines size="small" class="mb-3 mt-5"
                scrollable scrollHeight="600px">
                <Column field="positionVariantId" :header="$t('algo.bom.id')" sortable>
                    <template #body="slotProps">
                        <div class="font-mono">
                            {{ slotProps.data.positionVariantId }}
                        </div>
                    </template>
                </Column>
                <Column field="description" :header="$t('algo.bom.description')" sortable />
                <Column field="constraint" :header="$t('algo.bom.constraint')" sortable>
                    <template #body="slotProps">
                        <div class="font-mono">
                            {{ slotProps.data.constraint }}
                        </div>
                    </template>
                </Column>
            </DataTable>
        </div>
        <div v-else>
            {{ $t('algo.bom.no_position') }}
        </div>
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

function firstVal() {
    return getPositions().value[0]
}
</script>
