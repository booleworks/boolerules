import {
    type ComputationElementResult,
    type ListResultModel,
    type ResultModel,
    type SliceComputationResult,
} from '~/types/computations'

export default <MAIN, ELEMENT>() => {
    const flattenResult = (
        response: SliceComputationResult<MAIN>[],
        ignores: MAIN[] = []
    ): ResultModel<MAIN>[] => {
        return response.flatMap((mainRes) =>
            mainRes.slices.flatMap((slice) =>
                ({
                    result: mainRes.result,
                    slice: slice,
                }) as ResultModel<MAIN>,
            ),
        ).filter((res) => !ignores.includes(res.result))
    }

    const flattenListResult = (
        response: ComputationElementResult<MAIN, ELEMENT>[],
    ): ListResultModel<MAIN, ELEMENT>[] => {
        return response.flatMap((elemRes) =>
            elemRes.results.flatMap((mainRes) =>
                mainRes.slices.flatMap(
                    (slice) =>
                        ({
                            element: elemRes.element,
                            result: mainRes.result,
                            slice: slice,
                        }) as ListResultModel<MAIN, ELEMENT>,
                ),
            ),
        )
    }

    const splitPropsSingleResult = (
        response: ResultModel<MAIN>[],
    ): string[] => {
        return response.length == 0
            ? []
            : response[0].slice.content.map((prop) => prop.name)
    }

    const splitPropsListResult = (
        response: ListResultModel<MAIN, ELEMENT>[],
    ): string[] => {
        return response.length == 0
            ? []
            : response[0].slice.content.map((prop) => prop.name)
    }

    return {
        flattenResult,
        flattenListResult,
        splitPropsSingleResult,
        splitPropsListResult,
    }
}
