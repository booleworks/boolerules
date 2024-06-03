import { type WeightPair } from "~/types/computations";

const CUSTOM_WEIGHTS = "customWeights";

const customWeights = ref(useSessionStorage(CUSTOM_WEIGHTS, [] as WeightPair[]));

export default () => {

    const getCustomWeights = () => {
        return customWeights
    }

    const uploadCsv = (file: File) => {
        customWeights.value = []
        const reader = new FileReader();

        reader.onload = (event) => {
            if (event.target?.result) {
                const content = event.target.result as string
                const lines = content.split('\n').slice(1)
                lines.forEach(line => {
                    const parts = line.split(';')
                    if (parts[0] && parts[1]) {
                        customWeights.value.push({
                            constraint: parts[0],
                            weight: parseInt(parts[1])
                        } as WeightPair)
                    }
                });
            }
        };

        reader.readAsText(file)
    }

    return { getCustomWeights, uploadCsv };
};
