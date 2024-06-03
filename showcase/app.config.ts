const hostname = 'http://localhost:7070'

export default defineAppConfig({
    rulefile: hostname + "/rulefile",
    excel: hostname + "/export/excel",

    minmax: hostname + "/computation/minmaxconfig/synchronous",
    minmax_detail: hostname + "/computation/minmaxconfig/details",
    optimization: hostname + "/computation/optimization/synchronous",
    optimization_detail: hostname + "/computation/optimization/details",
    backbone: hostname + "/computation/backbone/synchronous",
    consistency: hostname + "/computation/consistency/synchronous",
    consistency_detail: hostname + "/computation/consistency/details",
    counting: hostname + "/computation/modelcount/synchronous",
    enumeration: hostname + "/computation/modelenumeration/synchronous",
    coverage: hostname + "/computation/coverage/synchronous",
    coverage_detail: hostname + "/computation/coverage/details",
    coverage_graph: hostname + "/computation/coverage/graph",
    reconfiguration: hostname + "/computation/reconfiguration/synchronous",
})
