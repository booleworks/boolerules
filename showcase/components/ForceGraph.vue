<template>
    <div ref="domElement"></div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import ForceGraph from 'force-graph';
import type {Graph} from "~/types/computations";

const domElement = ref(null as HTMLElement | null)

const createGraph = () => {
    const graphInstance = ForceGraph()(domElement.value!!)
            .graphData(props.graph || {nodes: [], links: []})
            .nodeId('id')
            .nodeLabel('name')
            .linkSource('source')
            .linkTarget('target')
            // TODO fix width and height
            .width(window.innerWidth)
            .height(window.innerHeight);

    window.addEventListener('resize', () => {
        graphInstance.width(window.innerWidth).height(window.innerHeight);
    });
};

onMounted(() => {
    createGraph();
});

const props = defineProps<{ graph?: Graph }>()
</script>
