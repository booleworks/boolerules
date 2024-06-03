<template>
    <NuxtLink class="no-underline" :to="item.to" :class="{
        'font-bold': item.to === $route.fullPath,
        'font-normal': item.to !== $route.fullPath,
    }">
        <div class="menu-item">{{ item.label }}</div>
    </NuxtLink>
</template>

<script setup lang="ts">
import { type MenuItem } from '~/types/menu'

defineProps<{ item: MenuItem }>()
</script>

<style scoped>
.menu-item {
    color: var(--text-color);
    padding-left: 2.5rem;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
}

.menu-item:hover {
    background-color: var(--surface-ground);
}
</style>
