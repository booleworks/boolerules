<template>
    <div class="flex justify-center w-full">
        <div class="flex flex-col items-center">
            <div class="text-3xl font-bold mt-10 mb-10">{{ $t('start.welcome') }}</div>
            <img :src="currentLogo()" alt="Logo" width="400" />
            <ol class="list-decimal mt-10">
                <li>{{ $t('start.first') }}</li>
                <li>{{ $t('start.second') }}</li>
                <li>{{ $t('start.third') }}</li>
            </ol>
        </div>
    </div>
</template>

<script setup lang="ts">
const { currentLogo } = useTheme()
</script>
