<template>
    <DetailHeader />
    <Button class="mt-3" :label="$t('details.btn_compute')" icon="pi pi-info-circle" @click="computeDetails()" />
    <div v-if="details.mainResult" class="mt-5">
        <h3>{{ $t('details.optimum') }}</h3>
        <div> {{ details.mainResult.result }} </div>
        <DetailExampleConfiguration :model="details.detail.detail.exampleConfiguration" />
    </div>
</template>

<script setup lang="ts">
import { type Slice, type FeatureModel, type DetailRequest } from '~/types/computations'

const appConfig = useAppConfig()
const { getDetailRequest, getJobId } = useComputation()
const details = ref({} as MinMaxDetail)

type MinMaxDetail = {
    mainResult: { result: number }
    detail: {
        slice: Slice,
        detail: {
            exampleConfiguration?: FeatureModel
        }
    }
}

async function computeDetails() {
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.minmax_detail, {
        method: 'POST',
        body: request,
    }).then((res) => {
        details.value = res as MinMaxDetail
    })
}
</script>
