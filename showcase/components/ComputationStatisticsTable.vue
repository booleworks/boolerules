<template>
    <div class="flex">
        <div class="flex">
            <table class="text-600">
                <tr>
                    <th class="font-bold text-left">{{ $t('comp_stat.time') }}</th>
                    <th class="font-bold text-left">{{ $t('comp_stat.slices') }}</th>
                    <th class="font-bold text-left">{{ $t('comp_stat.computations') }}</th>
                    <th class="font-bold text-left">{{ $t('comp_stat.avg_time') }}</th>
                    <th class="font-bold text-left">{{ $t('comp_stat.job_id') }}</th>
                </tr>
                <tr>
                    <td>{{ statistics.computationTimeInMs }} ms</td>
                    <td>{{ statistics.numberOfSlices }}</td>
                    <td>{{ statistics.numberOfSliceComputations }}</td>
                    <td>{{ statistics.averageSliceComputationTimeInMs }} ms</td>
                    <td>{{ jobId }}</td>
                </tr>
            </table>
        </div>

        <div class="flex align-items-center justify-content-center">
            <Button class="ml-5" :label="$t('comp_stat.btn_download')" @click="download()" icon="pi pi-cloud-download" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { type ComputationStatistics } from '~/types/computations'

const appConfig = useAppConfig()

let props = defineProps<{
    jobId: string
    statistics: ComputationStatistics
}>()

async function download() {
    try {
        const response = await fetch(appConfig.excel + "/" + props.jobId, {
            method: "GET",
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const blob = await response.blob();
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = "result_" + props.jobId + ".xlsx";
        link.click();
        URL.revokeObjectURL(link.href);
    } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
    }
}
</script>

<style scoped>
table,
th,
td {
    border: 1px solid;
    border-collapse: collapse;
    border-color: var(--surface-300);
    padding: 5px;
}

th {
    background-color: var(--surface) !important;
}

td {
    text-align: center;
    vertical-align: middle;
}
</style>
