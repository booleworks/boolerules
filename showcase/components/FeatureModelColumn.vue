<template>
    <div v-if="model && model?.features.length > 0" class="flex flex-wrap">
        <span v-for="feature of model.features" class="mr-1 feature-span">
            <span v-if="feature.booleanValue" class="mr-1">
                <span class="font-bold font-mono">{{ feature.code }}</span>
            </span>
            <span v-if="feature.enumValue" class="mr-1">
                <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                <span class="font-bold font-mono">{{ feature.enumValue }}</span>
            </span>
            <span v-if="feature.intValue" class="mr-1">
                <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                <span class="font-bold font-mono">{{ feature.intValue
                    }}</span>
            </span>
            <span v-if="feature.version" class="mr-1">
                <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                <span class="font-bold font-mono">{{ feature.version }}</span>
            </span>
        </span>
    </div>
    <div v-else>{{ $t('result.empty_config') }}</div>
</template>

<script setup lang="ts">
import { type FeatureModel } from '~/types/computations'

defineProps<{ model?: FeatureModel }>()
</script>
