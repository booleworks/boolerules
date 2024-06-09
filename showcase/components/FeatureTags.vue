<template>
    <div>
        <Tag v-if="boolFeature" class="feature-tag ml-2 !bg-[#004f93]" value="bool" />
        <Tag v-if="enumFeature" class="feature-tag ml-2 !bg-[#006266]" value="enum" />
        <Tag v-if="intFeature" class="feature-tag ml-2 !bg-[#ffc612]" value="int" />
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
