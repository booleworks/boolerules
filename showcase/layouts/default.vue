<template>
    <div class="flex h-full">
        <div class="flex flex-none">
            <!-- The sidebar with the main navigation -->
            <MenuSidebar />
        </div>
        <div class="flex-grow-1">
            <div class="flex flow-grow-1 flex-column h-full">
                <!-- Rule File Status Bar -->
                <div class="flex flex-none">
                    <RuleFileBar />
                </div>

                <!-- Main Content -->
                <div class="flex flex-grow-1 surface-ground pl-3 lr-3 overflow-scroll">
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
    font-family: var(--font-family);
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
