<template>
    <div class="text-3xl font-bold">{{ $t('details.header') }}</div>
    <div v-if="getDetailSelection().length > 0">
        <div class="mt-3 mb-3"> {{ $t('details.desc') }} </div>
        <table class="text-gray-600">
            <tr>
                <th class="font-bold text-left" style="padding-right: 2rem">{{ $t('slices.property').toUpperCase() }}
                </th>
                <th class="font-bold text-left">{{ $t('slices.sel').toUpperCase() }}</th>
            </tr>
            <tr v-for="detailSelection in getDetailSelection()">
                <SingleSliceSelector :selection="detailSelection" />
            </tr>
        </table>
    </div>
</template>

<script setup lang="ts">
const { getDetailSelection } = useComputation()
</script>
