<template>
    <div>
        <ClientOnly>
            <div v-if="isPresent() && getSummary().slicingProperties.length > 0">
                <table class="text-600">
                    <tr v-if="isPresent()">
                        <th class="font-bold text-left" style="padding-right: 2rem">{{ $t('slices.property').toUpperCase()
                        }}</th>
                        <th v-if="!onlySingleSlice" class="font-bold text-left">{{ $t('slices.type').toUpperCase() }}</th>
                        <th class="font-bold text-left">{{ $t('slices.slice').toUpperCase() }}</th>
                        <th class="font-bold text-left">{{ $t('slices.sel').toUpperCase() }}</th>
                    </tr>
                    <tr v-for="slice in getSummary().slicingProperties">
                        <SliceSelector :slice="slice" :selection="selectionForProperty(slice.name, defaultSliceType)"
                            :defaultSliceType="defaultSliceType" :allowedSliceTypes="allowedSliceTypes"
                            :only-single-slice="onlySingleSlice"/>
                    </tr>
                </table>
            </div>
            <div v-else-if="isPresent()" class="text-600 text">
                {{ $t('slices.no_props') }}
            </div>
            <div v-else class="text-600 text">
                {{ $t('slices.no_file') }}
            </div>
        </ClientOnly>
    </div>
</template>

<script setup lang="ts">
import { type SliceType } from '~/types/rulefiles';

const { isPresent, getSummary } = useCurrentRuleFile()
const { selectionForProperty } = useCurrentSliceSelection()

defineProps<{
    defaultSliceType: SliceType
    allowedSliceTypes: SliceType[]
    onlySingleSlice: boolean
}>()
</script>

<style scoped>
.divider-text :deep(.p-divider-content) {
    background-color: var(--surface-ground) !important;
}
</style>
