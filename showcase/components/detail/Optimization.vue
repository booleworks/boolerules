<template>
    <DetailHeader />
    <Button class="mt-3" :label="$t('details.btn_compute')" icon="pi pi-info-circle" @click="computeDetails()" />
    <div v-if="details.mainResult" class="mt-5">
        <div class="mt-5 mb-2 text-2xl font-bold">{{ $t('details.optimum') }}</div>
        <div> {{ details.mainResult.result }} </div>

        <div class="mt-5 mb-2 text-2xl font-bold">{{ $t('details.used_weights') }}</div>
        <DataTable size="small" :value="details.detail.detail.usedWeightings">
            <Column field="constraint" :header="$t('common.constraint')" sortable>
                <template #body="slotProps">
                    <div class="constraint">
                        {{ slotProps.data.constraint }}
                    </div>
                </template>
            </Column>
            <Column field="weight" :header="$t('algo.optimization.weighting')" sortable />
        </DataTable>

        <DetailExampleConfiguration :model="details.detail.detail.exampleConfiguration" />

    </div>
</template>

<script setup lang="ts">
import { type Slice, type FeatureModel, type DetailRequest, type WeightPair } from '~/types/computations'

const appConfig = useAppConfig()
const { getDetailRequest, getJobId } = useComputation()
const details = ref({} as OptimizationDetail)

type OptimizationDetail = {
    mainResult: { result: number }
    detail: {
        slice: Slice,
        detail: {
            exampleConfiguration?: FeatureModel
            usedWeightings: WeightPair[]
        }
    }
}

async function computeDetails() {
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.optimization_detail, {
        method: 'POST',
        body: request,
    }).then((res) => {
        details.value = res as OptimizationDetail
    })
}
</script>

<style scoped>
.constraint {
    font-family: monospace;
}
</style>
