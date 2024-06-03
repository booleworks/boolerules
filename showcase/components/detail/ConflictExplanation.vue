<template>
    <div v-if="explanation">
        <h3>{{ $t('details.explanation') }}</h3>
        <DataTable :value="explanation">
            <Column field="rule" :header="$t('details.rule')" />
            <Column field="description" :header="$t('details.description')" />
        </DataTable>
    </div>
</template>

<script setup lang="ts">
import { type Rule } from '~/types/rulefiles'
defineProps<{ explanation?: Rule[] }>()
</script>
