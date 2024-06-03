<template>
    <div class="flex justify-content-center w-full">
        <div class="flex flex-column align-items-center">
            <h2>{{ $t('start.welcome') }}</h2>
            <img :src="currentLogo()" alt="Logo" width="400" />
            <ol>
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
