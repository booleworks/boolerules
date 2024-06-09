<template>
    <DetailHeader />
    <Button v-if="getDetailSelection().length > 0" class="mt-5" :label="$t('details.btn_compute')"
        icon="pi pi-info-circle" @click="computeDetails()" />
    <div v-if="details.mainResult" class="mt-5">
        <div class="mt-5 mb-3 text-2xl font-bold">{{ $t('details.optimum') }}</div>
        <div> {{ details.mainResult.result }} </div>
        <DetailExampleConfiguration :model="details.detail.detail.exampleConfiguration" />
    </div>
</template>

<script setup lang="ts">
import { type Slice, type FeatureModel, type DetailRequest } from '~/types/computations'

const appConfig = useAppConfig()
const { getDetailRequest, getJobId, getDetailSelection } = useComputation()
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

onMounted(() => {
    if (getDetailSelection().length == 0) {
        computeDetails()
    }
})

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
