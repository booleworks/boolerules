import type { Constraint } from "~/types/computations";

const CUSTOM_CONSTRAINTS = "customConstraints";

const customConstraints = ref(useSessionStorage(CUSTOM_CONSTRAINTS, [] as Constraint[]));

export default () => {

    const getCustomConstraintsAsStrings = (): string[] => {
        if (customConstraints.value) {
            return customConstraints.value.map(it => it.constraint)
        } else {
            return []
        }
    }

    const getCustomConstraints = (): Constraint[] => {
        return customConstraints.value
    }

    const uploadCsv = (file: File) => {
        customConstraints.value = []
        const reader = new FileReader();

        reader.onload = (event) => {
            if (event.target?.result) {
                const content = event.target.result as string
                const lines = content.split('\n').slice(1)
                lines.forEach(line => {
                    customConstraints.value.push({ constraint: line } as Constraint)
                });
            }
        };

        reader.readAsText(file)
    }

    return { getCustomConstraints, getCustomConstraintsAsStrings, uploadCsv };
};
