<template>
    <div v-if="model">
        <div class="mt-5 mb-3 text-2xl font-bold">{{ $t('details.example') }}</div>
        <div v-if="model?.features.length > 0">
            <div v-for="feature of model.features" class="mr-1 feature-span">
                <div v-if="feature.booleanValue" class="mr-1">
                    <span class="font-mono font-bold">{{ feature.code }}</span>
                </div>
                <div v-if="feature.enumValue" class="mr-1">
                    <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                    <span class="font-mono font-bold">{{ feature.enumValue }}</span>
                </div>
                <div v-if="feature.intValue" class="mr-1">
                    <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                    <span class="font-mono font-bold">{{ feature.intValue
                        }}</span>
                </div>
                <div v-if="feature.version" class="mr-1">
                    <span class="font-mono text-gray-500">{{ feature.code }}=</span>
                    <span class="font-mono font-bold">{{ feature.version }}</span>
                </div>
            </div>
        </div>
        <div v-else>{{ $t('result.empty_config') }}</div>
    </div>
</template>

<script setup lang="ts">
import { type FeatureModel } from '~/types/computations';

defineProps<{ model?: FeatureModel }>()
</script>
