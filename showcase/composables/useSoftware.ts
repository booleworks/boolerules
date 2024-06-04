import { type SoftwareElement } from "~/types/computations";

const CUSTOM_SOFTWARE = "customSoftware";

const customSoftware = ref(useSessionStorage(CUSTOM_SOFTWARE, [] as SoftwareElement[]));

export default () => {

    const getCustomSoftware = () => {
        return customSoftware
    }

    const uploadCsv = (file: File) => {
        customSoftware.value = []
        const reader = new FileReader();

        reader.onload = (event) => {
            if (event.target?.result) {
                const content = event.target.result as string
                const lines = content.split('\n').slice(1)
                lines.forEach(line => {
                    const parts = line.split(';')
                    if (parts[0] && parts[1]) {
                        const tokens = parts[0].split('[')
                        var p: string
                        var v: string
                        if (tokens.length == 2 && tokens[1].startsWith("=")) {
                            p = tokens[0]
                            v = tokens[1].substring(1, tokens[1].length - 1)
                        } else {
                            p = parts[0]
                            v = ""
                        }
                        customSoftware.value.push({
                            package: p,
                            version: v,
                            action: parts[1]
                        } as SoftwareElement)
                    }
                });
            }
        };

        reader.readAsText(file)
    }

    return { getCustomSoftware, uploadCsv };
};
