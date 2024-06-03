<template>
    <div class="mb-3">
        <table>
            <tr v-if="additionalConstraints">
                <td style="padding-right: 20px">{{ $t('common.add_const') }}</td>
                <td>
                    <AdditionalConstraints />
                </td>
            </tr>
            <tr v-if="autocompleteFeatures">
                <td>{{ $t('common.features') }}</td>
                <td>
                    <FeatureAutoComplete />
                </td>
            </tr>
        </table>
    </div>
</template>

<script setup lang="ts">
defineProps(
    {
        additionalConstraints: Boolean,
        autocompleteFeatures: Boolean,
    }
)
</script>
