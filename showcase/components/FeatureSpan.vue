<template>
    <span v-if="feature.type === 'BOOLEAN'">
        <span v-if="showTag" class="mr-2">
            <Tag severity="success" value="bool" />
        </span>
        <span class="font-mono font-bold">{{ feature.code }}</span>
    </span>
    <span v-if="feature.type === 'ENUM'">
        <span v-if="showTag" class="mr-2">
            <Tag severity="warning" value="enum" />
        </span>
        <span class="font-mono text-gray-400">{{ feature.code }}=</span>
        <span class="font-mono font-bold">{{ feature.enumValue }}</span>
    </span>
    <span v-if="feature.type === 'INT'" class="mr-1">
        <span v-if="showTag" class="mr-2">
            <Tag severity="info" value="int" />
        </span>
        <span class="font-mono text-gray-400">{{ feature.code }}=</span>
        <span class="font-mono font-bold">{{ feature.intValue }}</span>
    </span>
    <span v-if="feature.type === 'VERSIONED_BOOLEAN'" class="mr-1">
        <span v-if="showTag" class="mr-2">
            <Tag severity="success" value="bool" />
        </span>
        <span class="font-mono text-gray-400">{{ feature.code }}=</span>
        <span class="font-mono font-bold">{{ feature.version }}</span>
    </span>
</template>

<script setup lang="ts">
import { type Feature } from '~/types/rulefiles'

defineProps<{
    feature: Feature
    showTag: Boolean
}>()
</script>
