import { type UploadSummary } from '~/types/rulefiles'

const PRLFILE = 'prlFile'
const uploadSummary = ref(useSessionStorage(PRLFILE, {} as UploadSummary))

export default () => {

    const setSummary = (summary: UploadSummary) => {
        sessionStorage.setItem(PRLFILE, JSON.stringify(summary))
        uploadSummary.value = summary
    }

    const getSummary = () => {
        return uploadSummary.value
    }

    const getId = () => {
        return uploadSummary.value.id
    }

    const isPresent = () => {
        return uploadSummary.value.id != null
    }

    const getFeatures = (): string[] => {
        return uploadSummary.value.features
    }

    return { setSummary, getSummary, getId, isPresent, getFeatures }
}
