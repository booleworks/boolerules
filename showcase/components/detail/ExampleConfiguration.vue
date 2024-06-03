<template>
    <div v-if="model">
        <h3>{{ $t('details.example') }}</h3>
        <div v-if="model?.features.length > 0">
            <div v-for="feature of model.features" class="mr-1 feature-span">
                <div v-if="feature.booleanValue" class="mr-1">
                    <span class="important-info">{{ feature.code }}</span>
                </div>
                <div v-if="feature.enumValue" class="mr-1">
                    <span class="unimportant-info">{{ feature.code }}=</span>
                    <span class="important-info">{{ feature.enumValue }}</span>
                </div>
                <div v-if="feature.intValue" class="mr-1">
                    <span class="unimportant-info">{{ feature.code }}=</span>
                    <span class="important-info">{{ feature.intValue
                    }}</span>
                </div>
                <div v-if="feature.version" class="mr-1">
                    <span class="unimportant-info">{{ feature.code }}=</span>
                    <span class="important-info">{{ feature.version }}</span>
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

<style scoped>
.unimportant-info {
    font-family: monospace;
    color: var(--text-color-secondary);
}

.important-info {
    font-family: monospace;
    font-weight: bold;
}
</style>

