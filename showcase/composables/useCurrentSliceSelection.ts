import {
    type PropertyRange,
    type PropertySelection,
    type SliceType,
    type UploadSummary,
} from '~/types/rulefiles'

const SLICE_SELECTION = 'sliceSelection'

const sliceSelection = ref(deserializeMap(useSessionStorage(SLICE_SELECTION, "[]")))

function serializeMap(sliceMap: Map<string, PropertySelection>) {
    return JSON.stringify(Array.from(sliceMap))
}

function deserializeMap(json: Ref<string>): Map<string, PropertySelection> {
    const map = new Map<string, PropertySelection>()
    if (json.value) {
        JSON.parse(json.value).forEach((item: [string, PropertySelection]) => {
            map.set(item[0], item[1])
        })
    }
    return map
}

export default () => {
    const initSliceSelection = (summary: UploadSummary) => {
        let sliceMap = new Map<string, PropertySelection>()
        summary.slicingProperties.forEach((property) => {
            sliceMap.set(property.name, {
                property: property.name,
                propertyType: property.type,
                range: {} as PropertyRange,
            } as PropertySelection)
        })
        sliceSelection.value = sliceMap
        sessionStorage.setItem(SLICE_SELECTION, serializeMap(sliceMap))
    }

    const selectionForProperty = (property: string, preset: SliceType): PropertySelection => {
        const sel = sliceSelection.value.get(property)!
        if (!sel.sliceType) {
            sel.sliceType = preset
        }
        return sel
    }

    const currentSliceSelection = (): PropertySelection[] => {
        return Array.from(sliceSelection.value.values())
    }

    return { initSliceSelection, selectionForProperty, currentSliceSelection }
}
