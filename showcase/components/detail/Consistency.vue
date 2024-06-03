<template>
    <DetailHeader />
    <Button v-if="getDetailSelection().length > 0" class="mt-3" :label="$t('details.btn_compute')"
            icon="pi pi-info-circle" @click="computeDetails()" />
    <div v-if="error.length == 0">
        <div v-if="details.mainResult" class="mt-5">
            <h3>{{ $t('details.result') }}</h3>
            <div v-if="details.mainResult.result" class="text-green-700"> {{ $t('algo.consistency.consistent') }} </div>
            <div v-else class="text-red-700"> {{ $t('algo.consistency.inconsistent') }} </div>
            <DetailExampleConfiguration :model="details.detail.detail.exampleConfiguration" />
            <DetailConflictExplanation :explanation="details.detail.detail.explanation" />
        </div>
    </div>
    <div v-else class="mt-3">
        {{ error }}
    </div>
</template>

<script setup lang="ts">
import { type Slice, type FeatureModel, type DetailRequest } from '~/types/computations'
import { type Rule } from '~/types/rulefiles'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const appConfig = useAppConfig()
const { getDetailRequest, getJobId, getDetailSelection } = useComputation()
const details = ref({} as ConsistencyDetail)
const error = ref("")

onMounted(() => {
    if (getDetailSelection().length == 0) {
        computeDetails()
    }
})

type ConsistencyDetail = {
    mainResult: { result: Boolean }
    detail: {
        slice: Slice,
        detail: {
            exampleConfiguration?: FeatureModel
            explanation?: Rule[]
        }
    }
}

async function computeDetails() {
    error.value = ""
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.consistency_detail, {
        method: 'POST',
        body: request,
    }).then((res) => {
        details.value = res as ConsistencyDetail
    }).catch(() => {
        error.value = t('details.error')
    })
}
</script>
