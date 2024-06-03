import {
    type Feature,
    type PropertyRange,
    type PropertySelection,
    type PropertyType,
    type SlicingProperty
} from '~/types/rulefiles'

// Specific datatypes
export type WeightPair = {
    constraint: string
    weight: number
}

export type Constraint = {
    constraint: string
}

export type Configuration = {
    features: { feature: string }[]
}

// Generic computation datatypes
export type SingleComputationResponse<MAIN> = {
    status: ComputationStatus
    results: SliceComputationResult<MAIN>[]
}

export type ListComputationResponse<MAIN, ELEMENT> = {
    status: ComputationStatus
    results: ComputationElementResult<MAIN, ELEMENT>[]
}

export type ComputationElementResult<MAIN, ELEMENT> = {
    element: ComputationElement<ELEMENT>
    results: SliceComputationResult<MAIN>[]
}

export type ComputationElement<ELEMENT> = {
    id: number
    content: ELEMENT
}

export type ComputationStatus = {
    success: boolean
    jobId: string
    ruleFileId: string
    statistics: ComputationStatistics
    errors: string[]
    warnings: string[]
    infos: string[]
}

export type ComputationStatistics = {
    computationTimeInMs: number
    numberOfSlices: number
    numberOfSliceComputations: number
    averageSliceComputationTimeInMs: number
}

export type SliceComputationResult<MAIN> = {
    result: MAIN
    slices: Slice[]
}

export type Slice = {
    content: SlicingProperty[]
}

export type DetailRequest = {
    jobId: string,
    sliceSelection: PropertySelection[]
}

export type DetailSliceSelection = {
    propertyName: string
    propertyType: PropertyType
    possibleValues: PropertyRange
    selectedValue: SingleRange
}

export type SingleRange = {
    booleanValue?: boolean
    enumValue?: string
    intValue?: number
    dateValue?: string
}

export type ListResultModel<MAIN, ELEMENT> = {
    element: ComputationElement<ELEMENT>,
    result: MAIN
    slice: Slice
}

export type ResultModel<MAIN> = {
    result: MAIN
    slice: Slice
}

export type FeatureModel = {
    features: Feature[]
}
