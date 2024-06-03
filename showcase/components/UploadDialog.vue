<template>
    <div>
        <div class="text-red-500" v-if="!summary.fileName">
            {{ $t('upload.error') }}
        </div>
        <div v-else>
            <div class="grid">
                <div class="col-2"><b>{{ $t('upload.status') }}</b></div>
                <div class="col-10" :class="{
                    'text-green-500':
                        summary.errors.length == 0 && summary.warnings.length == 0,
                    'text-orange-500':
                        summary.errors.length == 0 && summary.warnings.length > 0,
                    'text-red-500': summary.errors.length > 0,
                }">
                    <i class="pi" :class="{
                        'pi-check-circle':
                            summary.errors.length == 0 && summary.warnings.length == 0,
                        'pi-exclamation-circle':
                            summary.errors.length == 0 && summary.warnings.length > 0,
                        'pi-times-circle': summary.errors.length > 0,
                    }" style="font-size: 1rem"></i>
                    {{ status }}
                </div>
            </div>
            <div class="grid">
                <div class="col-2"><b>{{ $t('rulefilemgmt.filename') }}</b></div>
                <div class="col-10">{{ summary.fileName }}</div>
            </div>
            <div class="grid">
                <div class="col-2"><b>{{ $t('rulefilemgmt.size') }}</b></div>
                <div class="col-10">{{ formatBytes(summary.size) }}</div>
            </div>
            <div class="grid">
                <div class="col-2"><b>{{ $t('rulefilemgmt.uuid') }}</b></div>
                <div class="col-10">{{ summary.id }}</div>
            </div>
            <div class="grid">
                <div class="col-2"><b>{{ $t('rulefilemgmt.features') }}</b></div>
                <div class="col-10">{{ summary.numberOfFeatures }}</div>
            </div>
            <div class="grid">
                <div class="col-2"><b>{{ $t('rulefilemgmt.rules') }}</b></div>
                <div class="col-10">{{ summary.numberOfRules }}</div>
            </div>
            <div v-if="summary.errors?.length > 0">
                <div class="font-bold text-red-500 mt-2 mb-1">{{ $t('upload.errors') }}</div>
                <div class="text-red-500" v-for="error in summary.errors">
                    {{ error }}
                </div>
            </div>
            <div v-if="summary.warnings?.length > 0">
                <div class="font-bold text-orange-500 mt-2 mb-1">{{ $t('upload.warnings') }}</div>
                <div class="text-orange-500" v-for="warning in summary.warnings">
                    {{ warning }}
                </div>
            </div>
            <div v-if="summary.infos?.length > 0">
                <div class="font-bold mt-2 mb-1">{{ $t('upload.infos') }}</div>
                <div v-for="info in summary.infos">
                    {{ info }}
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { type UploadSummary } from '~/types/rulefiles'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const props = defineProps<{ summary: UploadSummary }>()

const status = computed(() => {
    if (!props.summary.id || props.summary.errors.length > 0) {
        return t('upload.no_upload_errors')
    } else if (props.summary.warnings.length > 0) {
        return t('upload.upload_warnings')
    } else {
        return t('upload.upload_success')
    }
})

function formatBytes(bytes: number, decimals = 2) {
    if (!+bytes) return '0 Bytes'
    const k = 1024
    const dm = decimals < 0 ? 0 : decimals
    const sizes = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`
}
</script>
