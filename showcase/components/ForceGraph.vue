<template>
    <div ref="domElement" class="content"></div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import ForceGraph, {type ForceGraphInstance} from 'force-graph';
import type {Graph} from "~/types/computations";

const domElement = ref(null as HTMLElement | null)
const currentGraph = ref(null as ForceGraphInstance | null)

const createGraph = () => {
    currentGraph.value?._destructor();
    currentGraph.value = ForceGraph()(domElement.value!!)
            .graphData(props.graph || {nodes: [], links: []})
            .nodeId('id')
            .nodeLabel('name')
            .linkSource('source')
            .linkTarget('target')
            .width(props.relWidth * window.innerWidth)
            .height(props.relHeight * window.innerHeight);
};

onMounted(() => {
    createGraph();
    window.addEventListener('resize', () => {
        createGraph()
    });
});

const props = defineProps<{ graph?: Graph, relWidth: number, relHeight: number }>()
</script>

<style scoped>
.content {
    overflow: hidden !important;
    padding: 0 !important;
    margin: 0 !important;
}
</style>
