import { type DetailSliceSelection, type SingleRange } from "~/types/computations";
import { type PropertyRange, type PropertySelection } from "~/types/rulefiles";

const { getSummary } = useCurrentRuleFile()

const JOB_ID = "jobId";
const DETAIL_SLICE_SELECTION = "detailSliceSelection";
const ANY_OR_ALL_SLICES = "anyOrAllSlices";

const currentJobId = ref(useSessionStorage(JOB_ID, ""));
const currentSliceSelection = ref(useSessionStorage(DETAIL_SLICE_SELECTION, [] as DetailSliceSelection[]))
const anyOrAllSlices = ref(useSessionStorage(ANY_OR_ALL_SLICES, false))

export default () => {

    const setJobId = (jobId: string) => {
        currentJobId.value = jobId
    }

    const getJobId = (): string => {
        return currentJobId.value
    }

    const containsAnyOrAllSplits = (): boolean => {
        return anyOrAllSlices.value
    }

    const initDetailSelection = (props: PropertySelection[]) => {
        currentSliceSelection.value = []
        anyOrAllSlices.value = false
        props.forEach((prop) => {
            if (prop.sliceType == 'SPLIT') {
                let possibleValues = hasValue(prop.range)
                    ? prop.range
                    : getSummary().slicingProperties.find((p) => p.name == prop.property)?.range
                currentSliceSelection.value.push(({
                    propertyName: prop.property,
                    propertyType: prop.propertyType,
                    possibleValues: possibleValues,
                    selectedValue: firstValue(possibleValues)
                } as DetailSliceSelection))
            } else {
                anyOrAllSlices.value = true
            }
        })
    }

    const getDetailRequest = (): PropertySelection[] => {
        let sel = [] as PropertySelection[]
        currentSliceSelection.value.forEach((s) => {
            sel.push({
                property: s.propertyName,
                propertyType: s.propertyType,
                sliceType: 'SPLIT',
                range: transformRange(s.selectedValue)
            } as PropertySelection)
        })
        return sel
    }

    function hasValue(range: PropertyRange): boolean {
        return range.booleanValues != null && range.booleanValues.length > 0 ||
            range.enumValues != null && range.enumValues.length > 0 ||
            range.intValues != null && range.intValues.length > 0 ||
            range.dateValues != null && range.dateValues.length > 0
    }

    function transformRange(range: SingleRange): PropertyRange {
        let prop = {} as PropertyRange
        console.log(prop)
        if (range.booleanValue != null) {
            prop.booleanValues = [range.booleanValue]
        }
        if (range.enumValue != null) {
            prop.enumValues = [range.enumValue]
        }
        if (range.intValue != null) {
            prop.intValues = [range.intValue]
        }
        if (range.dateValue != null) {
            prop.dateValues = [range.dateValue]
        }
        return prop
    }

    const getDetailSelection = (): DetailSliceSelection[] => {
        return currentSliceSelection.value
    }

    function firstValue(possibleValues: PropertyRange | undefined): SingleRange {
        if (possibleValues === undefined) {
            return {}
        }
        if (possibleValues.intMin) {
            return { intValue: possibleValues.intMin } as SingleRange
        }
        if (possibleValues.intValues) {
            return { intValue: possibleValues.intValues[0] } as SingleRange
        }
        if (possibleValues.dateMin) {
            return { dateValue: possibleValues.dateMin } as SingleRange
        }
        if (possibleValues.dateValues) {
            return { dateValue: possibleValues.dateValues[0] } as SingleRange
        }
        if (possibleValues.enumValues) {
            return { enumValue: possibleValues.enumValues[0] } as SingleRange
        }
        if (possibleValues.booleanValues) {
            return { booleanValue: possibleValues.booleanValues[0] } as SingleRange
        }
        return {}
    }


    return { setJobId, getJobId, initDetailSelection, getDetailSelection, getDetailRequest, containsAnyOrAllSplits };
};
