import type { Configuration } from "~/types/computations";

const CONFIGURATION = "configuration";

const configuration = ref(useSessionStorage(CONFIGURATION, { features: [] } as Configuration));

export default () => {

    const getConfiguration = (): Configuration => {
        return configuration.value
    }

    const uploadCsv = (file: File) => {
        configuration.value.features = []
        const reader = new FileReader();

        reader.onload = (event) => {
            if (event.target?.result) {
                const content = event.target.result as string
                const lines = content.split('\n').slice(1)
                lines.forEach(feature => {
                    configuration.value.features.push({ feature: feature })
                });
            }
        };

        reader.readAsText(file)
    }

    return { getConfiguration, uploadCsv };
};
