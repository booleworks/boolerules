<template>
    <span>
        <span class="font-mono">{{ formatRange(property.range) }} </span>
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
