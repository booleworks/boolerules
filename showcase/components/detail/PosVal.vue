kj<template>
    <DetailHeader />
    <Button v-if="getDetailSelection().length > 0" class="mt-5" :label="$t('details.btn_compute')"
        icon="pi pi-info-circle" @click="computeDetails()" />
    <div class="mt-10" v-if="error.length == 0">
        <div v-if="details.mainResult" class="mt-5">
            <div v-if="details.mainResult.result.hasDeadPvs">
                <Panel class="mb-10" :header="$t('algo.bom.has_dead_pvs')" toggleable>
                    <div class="mb-3">
                        {{ t('algo.bom.detail_dead') }}
                    </div>
                    <DataTable size="small" :value="details.detail.detail.deadPVs">
                        <Column :header="$t('algo.bom.pv')" sortable>
                            <template #body="slotProps">
                                <div class="font-mono font-bold">
                                    {{ slotProps.data.positionVariantId }}
                                </div>
                            </template>
                        </Column>
                        <Column field="description" :header="$t('common.desc')" sortable />
                        <Column :header="$t('common.constraint')" sortable>
                            <template #body="slotProps">
                                <div class="font-mono">
                                    {{ slotProps.data.constraint }}
                                </div>
                            </template>
                        </Column>
                    </DataTable>
                </Panel>
            </div>
            <div v-if="!details.mainResult.result.isComplete">
                <Panel class="mb-10" :header="$t('algo.bom.is_complete')" toggleable>
                    <div class="mb-3">
                        {{ t('algo.bom.detail_complete') }}
                    </div>
                    <DetailExampleConfiguration :model="details.detail.detail.nonComplete" :omit-header="true" />
                </Panel>
            </div>
            <div v-if="details.mainResult.result.hasNonUniquePVs">
                <Panel :header="$t('algo.bom.has_non_unique_pvs')" toggleable>
                    <div v-for="(col, index) in details.detail.detail.nonUniquePvs">
                        <div class="mt-5 mb-2 text-xl font-bold"> {{ $t('algo.bom.has_non_unique_pvs') + ' ' +
                            (index + 1) }}
                        </div>
                        <DataTable size="small" :value="[col.firstPositionVariant, col.secondPositionVariant]">
                            <Column :header="$t('algo.bom.pv')" sortable>
                                <template #body="slotProps">
                                    <div class="font-mono font-bold">
                                        {{ slotProps.data.positionVariantId }}
                                    </div>
                                </template>
                            </Column>
                            <Column field="description" :header="$t('common.desc')" sortable />
                            <Column :header="$t('common.constraint')" sortable>
                                <template #body="slotProps">
                                    <div class="font-mono">
                                        {{ slotProps.data.constraint }}
                                    </div>
                                </template>
                            </Column>
                        </DataTable>
                        <div class="mt-5 mb-3">
                            {{ t('algo.bom.detail_unique') }}
                        </div>
                        <DetailExampleConfiguration :model="col.exampleConfiguration" :omit-header="true" />
                    </div>
                </Panel>
            </div>
        </div>
    </div>
    <div v-else class="mt-3">
        {{ error }}
    </div>
</template>

<script setup lang="ts">
import { type Slice, type FeatureModel, type DetailRequest } from '~/types/computations'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const appConfig = useAppConfig()
const { getDetailRequest, getJobId, getDetailSelection } = useComputation()
const details = ref({} as PosValDetail)
const error = ref("")

onMounted(() => {
    if (getDetailSelection().length == 0) {
        computeDetails()
    }
})

type PosValDetail = {
    mainResult: {
        id: number,
        result: {
            positionId: string
            description: string
            constraint: string
            isComplete: boolean
            hasNonUniquePVs: boolean
            hasDeadPvs: boolean
        }
    }
    detail: {
        slice: Slice,
        detail: {
            deadPVs: PositionVariant[]
            nonUniquePvs: NonUniquePvsDetail[]
            nonComplete?: FeatureModel
        }
    }
}

type PositionVariant = {
    positionVariantId: string
    description: string
    constraint: string
}
type NonUniquePvsDetail = {
    firstPositionVariant: PositionVariant,
    secondPositionVariant: PositionVariant,
    exampleConfiguration: FeatureModel
}

async function computeDetails() {
    error.value = ""
    const request: DetailRequest = {
        jobId: getJobId(),
        sliceSelection: getDetailRequest()
    }
    $fetch(appConfig.posval_detail, {
        method: 'POST',
        body: request,
    }).then((res) => {
        details.value = res as PosValDetail
    }).catch(() => {
        error.value = t('details.error')
    })
}
</script>
