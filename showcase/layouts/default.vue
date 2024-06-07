<template>
    <div class="flex h-full">
        <div class="flex flex-none">
            <!-- The sidebar with the main navigation -->
            <MenuSidebar />
        </div>
        <div class="flex-grow">
            <div class="flex flex-grow flex-col h-full">
                <!-- Rule File Status Bar -->
                <div class="flex flex-none">
                    <RuleFileBar />
                </div>

                <!-- Main Content -->
                <div class="flex flex-grow bg-gray-100 pl-3 px-3 overflow-auto">
                    <slot />
                </div>

                <!-- Application Log and Footer -->
                <!-- <div class="flex flex-none"> -->
                <!--   <Footer /> -->
                <!-- </div> -->
            </div>
        </div>
    </div>
</template>

<script setup lang="ts"></script>

<style>
body {
    font-family: Inter var, ui-sans-serif, system-ui, sans-serif, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;
}

html,
body,
#__nuxt,
#__nuxt>div {
    height: 100% !important;
    width: 100% !important;
    margin: 0;
    font-size: 14px;
}
</style>
