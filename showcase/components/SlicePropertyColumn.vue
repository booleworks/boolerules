<template>
    <span>
        <span class="slice-prop-value">{{ formatRange(property.range) }} </span>
    </span>
</template>

<script setup lang="ts">
import { type SlicingProperty, type PropertyRange } from '~/types/rulefiles'

defineProps<{ property: SlicingProperty }>()

function formatRange(range: PropertyRange) {
    if (range.booleanValues) {
        return range.booleanValues.join(', ')
    }
    if (range.enumValues) {
        return range.enumValues.join(', ')
    }
    if (range.intValues && range.intValues.length > 0) {
        return range.intValues.join(', ')
    }
    if (range.intMin) {
        return range.intMin == range.intMax
            ? range.intMin
            : range.intMin + ' - ' + range.intMax
    }
    if (range.dateValues && range.dateValues.length > 0) {
        return range.dateValues.join(', ')
    }
    if (range.dateMin) {
        return range.dateMin == range.dateMax
            ? range.dateMin
            : range.dateMin + ' - ' + range.dateMax
    }
}
</script>

<style scoped>
.slice-prop-name {
    font-family: monospace;
    font-weight: bold;
}

.slice-prop-value {
    font-family: monospace;
}

.slice-prop-divider {
    font-family: monospace;
}
</style>
