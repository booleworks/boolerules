<template>
    <td class="property-name font-bold" style="padding-right: 2rem">
        {{ selection.propertyName }}
    </td>
    <td v-if="selection.propertyType == 'BOOLEAN'">
        <Dropdown v-model="selection.selectedValue.booleanValue" :options="selection.possibleValues.booleanValues"
            :placeholder="$t('slices.filter')" />
    </td>
    <td v-if="selection.propertyType == 'ENUM'">
        <div>
            <Dropdown v-model="selection.selectedValue.enumValue" :options="selection.possibleValues.enumValues?.toSorted()"
                :placeholder="$t('slices.filter')" />
        </div>
    </td>
    <td v-else-if="selection.propertyType == 'INT'">
        <Dropdown v-model="selection.selectedValue.intValue" :options="selection.possibleValues.intValues?.toSorted()"
            :placeholder="$t('slices.filter')" class="mr-3" />
    </td>
    <td v-else-if="selection.propertyType == 'DATE'">
        <Dropdown v-model="selection.selectedValue.dateValue" :options="selection.possibleValues.dateValues?.toSorted()"
            :placeholder="$t('slices.filter')" class="mr-3" />
    </td>
</template>

<script setup lang="ts">
import { type DetailSliceSelection } from "~/types/computations";

defineProps<{ selection: DetailSliceSelection }>();

</script>

<style scoped>
.property-name {
    font-family: monospace;
}

th,
td {
    padding-top: 10px;
    padding-right: 20px;
}
</style>
