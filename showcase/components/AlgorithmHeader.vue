<template>
    <div class="flex w-full mt-3">
        <div class="text-3xl font-bold mr-3">{{ header }}</div>
        <FeatureTags :boolFeature="boolFeature" :enumFeature="enumFeature" :intFeature="intFeature" />
    </div>
</template>

<script setup lang="ts">
defineProps(
    {
        header: String,
        boolFeature: Boolean,
        enumFeature: Boolean,
        intFeature: Boolean
    })
</script>
