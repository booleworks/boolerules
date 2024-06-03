<template>
    <div>
        <div v-if="status.success">
            <ComputationStatisticsTable :job-id="status.jobId" :statistics="status.statistics" />
        </div>

        <div v-if="status.errors?.length > 0">
            <div class="text-lg font-bold text-red-500 mt-2 mb-1">{{ $t('upload.errors') }}</div>
            <div class="text-red-500" v-for="error in status.errors">
                {{ error }}
            </div>
        </div>
        <div v-if="status.warnings?.length > 0">
            <div class="text-lg font-bold text-orange-500 mt-2 mb-1">{{ $t('upload.warnings') }}</div>
            <div class="text-orange-500" v-for="warning in status.warnings">
                {{ warning }}
            </div>
        </div>
        <div v-if="!status.jobId" class="text-600 text">{{ $t('algo.nothing_computed') }}</div>
    </div>
</template>

<script setup lang="ts">
import { type ComputationStatus } from '~/types/computations'

defineProps<{
    status: ComputationStatus
}>()
</script>

