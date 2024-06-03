<template>
    <span v-if="feature.type === 'BOOLEAN'">
        <span v-if="showTag" class="mr-2">
            <Tag severity="success" value="bool" />
        </span>
        <span class="important">{{ feature.code }}</span>
    </span>
    <span v-if="feature.type === 'ENUM'">
        <span v-if="showTag" class="mr-2">
            <Tag severity="warning" value="enum" />
        </span>
        <span class="unimportant">{{ feature.code }}=</span>
        <span class="important">{{ feature.enumValue }}</span>
    </span>
    <span v-if="feature.type === 'INT'" class="mr-1">
        <span v-if="showTag" class="mr-2">
            <Tag severity="info" value="int" />
        </span>
        <span class="unimportant">{{ feature.code }}=</span>
        <span class="important">{{ feature.intValue }}</span>
    </span>
    <span v-if="feature.type === 'VERSIONED_BOOLEAN'" class="mr-1">
        <span v-if="showTag" class="mr-2">
            <Tag severity="success" value="bool" />
        </span>
        <span class="unimportant">{{ feature.code }}=</span>
        <span class="important">{{ feature.version }}</span>
    </span>
</template>

<script setup lang="ts">
import { type Feature } from '~/types/rulefiles'

defineProps<{
    feature: Feature
    showTag: Boolean
}>()
</script>

<style scoped>
.unimportant {
    font-family: monospace;
    color: var(--text-color-secondary) !important;
}

.important {
    font-family: monospace;
    font-weight: bold;
}
</style>
