<template>
    <td class="property-name font-bold" style="padding-right: 2rem">
        {{ selection.property }}
    </td>
    <td>{{ format(slice.type) }}</td>
    <td v-if="!onlySingleSlice">
        <Dropdown v-model="selection.sliceType" :options="allowedSliceTypes" :placeholder="$t('slices.slice')"
            class="md:w-8rem" />
    </td>
    <td v-if="booleanValues && booleanValues.length > 0">
        <Dropdown v-if="onlySingleSlice" v-model="singleBooleanValue" :options="booleanValues" :placeholder="$t('slices.single_select')" />
        <MultiSelect v-else v-model="selection.range.booleanValues" :options="booleanValues" :placeholder="$t('slices.filter')" />
    </td>
    <td v-if="enumValues && enumValues.length > 0">
        <div>
            <Dropdown v-if="onlySingleSlice" v-model="singleEnumValue" :options="enumValues" :placeholder="$t('slices.single_select')" />
            <MultiSelect v-else v-model="selection.range.enumValues" :options="enumValues" :placeholder="$t('slices.filter')" />
        </div>
    </td>
    <td v-else-if="intValues && intValues.length > 0 && onlySingleSlice">
        <Dropdown v-model="singleIntValue" :options="intValues" :placeholder="$t('slices.single_select')" :disabled="selection.range.intMin != null || selection.range.intMax != null" />
    </td>
    <td v-else-if="intValues && intValues.length > 0">
        <MultiSelect v-model="selection.range.intValues" :options="intValues" :placeholder="$t('slices.filter')"
                     :disabled="selection.range.intMin != null || selection.range.intMax != null" class="mr-3" />
        <InputNumber v-model="selection.range.intMin" placeholder="From" mode="decimal" showButtons id="intStart"
            :min="intStart" :max="intEnd" :disabled="selection.range.intValues && selection.range.intValues.length > 0" class="mr-3" />
        <InputNumber v-model="selection.range.intMax" placeholder="To" mode="decimal" showButtons :min="intStart"
            :max="intEnd" :disabled="selection.range.intValues && selection.range.intValues.length > 0" />
    </td>
    <td v-else-if="dateValues && dateValues.length > 0 && onlySingleSlice">
        <Dropdown v-model="singleDateValue" :options="dateValues" :placeholder="$t('slices.single_select')"
                     :disabled="selection.range.dateMin != null || selection.range.dateMax != null" />
    </td>
    <td v-else-if="dateValues && dateValues.length > 0">
        <MultiSelect v-model="selection.range.dateValues" :options="dateValues" :placeholder="$t('slices.filter')"
            :disabled="selection.range.dateMin != null || selection.range.dateMax != null
                " class="mr-3" />
        <Calendar id="dateFrom" v-model="selection.range.dateMin" :placeholder="$t('slices.from')" showIcon showButtonBar
                  dateFormat="yy-mm-dd" :minDate="dateStart" :maxDate="dateEnd"
                  :disabled="selection.range.dateValues && selection.range.dateValues.length > 0" class="mr-3" />
        <Calendar id="dateTo" v-model="selection.range.dateMax" :placeholder="$t('slices.to')" dateFormat="yy-mm-dd"
                  :minDate="dateStart" :maxDate="dateEnd" showIcon showButtonBar
                  :disabled="selection.range.dateValues && selection.range.dateValues.length > 0" />
    </td>
</template>

<script setup lang="ts">
import {
    type SlicingProperty,
    type PropertySelection,
    type PropertyType,
    type SliceType,
} from "~/types/rulefiles";

const props = defineProps<{
    slice: SlicingProperty;
    selection: PropertySelection;
    defaultSliceType: SliceType;
    allowedSliceTypes: SliceType[];
    onlySingleSlice: boolean;
}>();

const booleanValues = computed(() =>
    props.slice.range?.booleanValues?.map((v) => v.toString())
);
const enumValues = computed(() => props.slice.range?.enumValues?.toSorted());
const intValues = computed(() => props.slice.range?.intValues?.toSorted());
const intStart = computed(() => props.slice.range?.intMin);
const intEnd = computed(() => props.slice.range?.intMax);
const dateValues = computed(() => props.slice.range?.dateValues?.toSorted());
const dateStart = computed(() =>
    props.slice.range?.dateMin == null
        ? new Date("1900-01-01")
        : new Date(props.slice.range?.dateMin)
);
const dateEnd = computed(() =>
    props.slice.range?.dateMax == null
        ? new Date("2099-12-31")
        : new Date(props.slice.range?.dateMax)
);
const singleBooleanValue = computed({
    get() { return props.selection.range.booleanValues && props.selection.range.booleanValues.length > 0 ? props.selection.range.booleanValues[0] : null },
    set(newValue: boolean) { props.selection.range.booleanValues = newValue ? [newValue] : []}
});
const singleIntValue = computed({
    get() { return props.selection.range.intValues && props.selection.range.intValues.length > 0 ? props.selection.range.intValues[0] : null },
    set(newValue: number) { props.selection.range.intValues = newValue ? [newValue] : []}
});
const singleEnumValue = computed({
    get() { return props.selection.range.enumValues && props.selection.range.enumValues.length > 0 ? props.selection.range.enumValues[0] : null },
    set(newValue: string) { props.selection.range.enumValues = newValue ? [newValue] : []}
});
const singleDateValue = computed({
    get() { return props.selection.range.dateValues && props.selection.range.dateValues.length > 0 ? props.selection.range.dateValues[0] : null },
    set(newValue: string) { props.selection.range.dateValues = newValue ? [newValue] : []}
});

function format(t: PropertyType) {
    if (t === "BOOLEAN") {
        return "Boolean";
    } else if (t === "INT") {
        return "Integer";
    } else if (t === "ENUM") {
        return "Enum";
    } else if (t === "DATE") {
        return "Date";
    }
}
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
