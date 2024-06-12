import { type Position, type PositionVariant } from "~/types/computations";

const BOM_CHECK = "bomCheck";

const bomPositions = ref(useSessionStorage(BOM_CHECK, [] as Position[]));

export default () => {

    const getPositions = () => {
        return bomPositions
    }

    const uploadBomCsv = (file: File) => {
        bomPositions.value = []
        const reader = new FileReader();

        reader.onload = (event) => {
            if (event.target?.result) {
                const content = event.target.result as string
                const lines = content.split('\n').slice(1)
                lines.forEach(line => {
                    const parts = line.split(';')
                    let posId = parts[0]
                    let positions = bomPositions.value.filter(bp => bp.positionId == posId)

                    let pvId = parts[1]
                    if (!pvId) {
                        if (positions.length == 0) {
                            if (parts[2]) {
                                let position = {
                                    positionId: posId,
                                    description: parts[2],
                                    constraint: parts[3],
                                    positionVariants: []
                                } as Position
                                bomPositions.value.push(position)
                            }
                        }
                    } else {
                        if (positions.length == 1) {
                            positions[0].positionVariants.push({
                                positionVariantId: pvId,
                                description: parts[2],
                                constraint: parts[3]
                            } as PositionVariant)
                        } else if (positions.length < 1) {
                            // TODO no position error
                        } else {
                            // TODO duplicate position error
                        }

                    }
                });
            }
        };

        reader.readAsText(file)
    }

    return { getPositions, uploadBomCsv };
};
