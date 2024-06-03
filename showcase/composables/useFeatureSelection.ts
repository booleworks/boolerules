const FEATURE_SELECTION = 'featureSelection'

const selectedFeatures = ref(useSessionStorage(FEATURE_SELECTION, [] as string[]))

export default () => {

    const getSelectedFeatures = () => {
        return selectedFeatures
    }

    const clearSelectedFeatures = () => {
        sessionStorage.setItem(FEATURE_SELECTION, '[]')
        selectedFeatures.value = []
    }

    return { getSelectedFeatures, clearSelectedFeatures }
}

