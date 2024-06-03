<template>
    <DetailHeader />
    <Button class="mt-3" :label="$t('details.btn_compute')" icon="pi pi-info-circle" @click="computeDetails()" />
    <div v-if="details.mainResult" class="mt-5">
        <h3>{{ $t('details.optimum') }}</h3>
        <div> {{ details.mainResult.result }} </div>

        <h3>{{ $t('details.used_weights') }}</h3>
        <DataTable :value="details.detail.detail.usedWeightings">
            <Column field="constraint" :header="$t('common.constraint')" sortable />
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
