<template>
    <div class="additional-constraints">
        <InputText v-model="additionalConstraint" class="add-input" />
    </div>
</template>

<script setup lang="ts">

const { getAdditionalConstraint } = useAdditionalConstraints();

const additionalConstraint = getAdditionalConstraint();

</script>

<style scoped>
.add-input {
    width: 30rem;
}

.additional-constraints :deep(.p-autocomplete-token-label) {
    margin-right: 5px !important;
}

.additional-constraints {
    max-width: 50% !important;
}
</style>
