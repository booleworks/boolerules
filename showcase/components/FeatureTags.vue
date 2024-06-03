<template>
    <div>
        <Tag v-if="boolFeature" severity="success" class="feature-tag ml-2" value="bool" />
        <Tag v-if="enumFeature" severity="warning" class="feature-tag ml-2" value="enum" />
        <Tag v-if="intFeature" severity="info" class="feature-tag ml-2" value="int" />
    </div>
</template>

<script setup lang="ts">
defineProps<{
    boolFeature: boolean
    enumFeature: boolean
    intFeature: boolean
}>()
</script>

<style scoped>
.feature-tag :deep(.p-tag-value) {
    font-size: 0.7rem;
}
</style>
