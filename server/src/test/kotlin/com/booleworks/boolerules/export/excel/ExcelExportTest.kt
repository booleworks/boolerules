package com.booleworks.boolerules.export.excel

import com.booleworks.boolerules.computations.backbone.BackboneComputation
import com.booleworks.boolerules.computations.backbone.BackboneRequest
import com.booleworks.boolerules.computations.backbone.BackboneType
import com.booleworks.boolerules.computations.consistency.ConsistencyComputation
import com.booleworks.boolerules.computations.consistency.ConsistencyRequest
import com.booleworks.boolerules.computations.generic.ComputationElement
import com.booleworks.boolerules.computations.generic.ComputationElementResult
import com.booleworks.boolerules.computations.generic.ComputationStatusBuilder
import com.booleworks.boolerules.computations.generic.ComputationVariant.SINGLE
import com.booleworks.boolerules.computations.generic.ListComputationResponse
import com.booleworks.boolerules.computations.generic.MergeResult
import com.booleworks.boolerules.computations.generic.OptimizationType
import com.booleworks.boolerules.computations.generic.PropertySelectionDO
import com.booleworks.boolerules.computations.generic.SingleComputationResponse
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SliceTypeDO
import com.booleworks.boolerules.computations.minmaxconfig.MinMaxConfigComputation
import com.booleworks.boolerules.computations.minmaxconfig.MinMaxConfigRequest
import com.booleworks.boolerules.computations.modelcount.ModelCountComputation
import com.booleworks.boolerules.computations.modelcount.ModelCountRequest
import com.booleworks.boolerules.computations.modelenumeration.ModelEnumerationComputation
import com.booleworks.boolerules.computations.modelenumeration.ModelEnumerationRequest
import com.booleworks.boolerules.computations.optimization.OptimizationComputation
import com.booleworks.boolerules.computations.optimization.OptimizationRequest
import com.booleworks.boolerules.computations.optimization.WeightPair
import com.booleworks.boolerules.export.excel.computations.BackboneExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ConsistencyExcelFormatter
import com.booleworks.boolerules.export.excel.computations.MinMaxConfigExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ModelCountExcelFormatter
import com.booleworks.boolerules.export.excel.computations.ModelEnumerationExcelFormatter
import com.booleworks.boolerules.export.excel.computations.OptimizationExcelFormatter
import com.booleworks.boolerules.rulefile.PropertyRangeDO
import com.booleworks.boolerules.rulefile.PropertyTypeDO
import com.booleworks.kjobs.data.Job
import com.booleworks.kjobs.data.JobStatus
import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.parseRuleFile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigInteger
import java.time.LocalDateTime

class ExcelExportTest {
    private val cc = ConsistencyComputation
    private val mc = ModelCountComputation
    private val bb = BackboneComputation
    private val me = ModelEnumerationComputation
    private val mm = MinMaxConfigComputation
    private val op = OptimizationComputation
    private val compiler = PrlCompiler()
    private val model = compiler.compile(parseRuleFile("../test-files/prl/transpiler/merge3.prl"))

    private val weightings = listOf(
        WeightPair("""[a in ["a1", "a2"]]""", 20), // always true
        WeightPair("""[b = "b1"]""", 10),          // always in version 1
        WeightPair("""[b = "b2"]""", 15),          // always in version 2
        WeightPair("""[b = "b3"]""", 1000),        // never true
        WeightPair("""[p = "px"]""", -1000),       // never true
        WeightPair("""[p = "p1"]""", -7),          // always in series 1
        WeightPair("""[p = "p2"]""", -9),          // always in series 2
        WeightPair("""[c = "c1"]""", 2),           // version 1 or 2
        WeightPair("""[c = "c2"]""", 4),           // version 1 or 2
        WeightPair("""[c = "c3"]""", 6),           // only in version 2
    )

    private val sliceSelection = mutableListOf(
        PropertySelectionDO(
            "series",
            PropertyTypeDO.ENUM,
            PropertyRangeDO(enumValues = setOf("S1", "S2")),
            SliceTypeDO.SPLIT
        ),
        PropertySelectionDO("version", PropertyTypeDO.INT, PropertyRangeDO(intMin = 1, intMax = 2), SliceTypeDO.SPLIT)
    )
    private val ccRequest = ConsistencyRequest("any", sliceSelection, listOf())
    private val ccResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genSingleResponse(cc.computeResponse(ccRequest, model, status).merge, status)
        }
    private val mcRequest = ModelCountRequest("any", sliceSelection, listOf())
    private val mcResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genSingleResponse(mc.computeResponse(mcRequest, model, status).merge, status)
        }

    private val bbRequest = BackboneRequest("any", sliceSelection, listOf(), listOf())
    private val bbResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genListResponse(bb.computeResponse(bbRequest, model, status), status)
        }

    private val meRequest = ModelEnumerationRequest("any", sliceSelection, listOf(), listOf())
    private val meResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genListResponse(me.computeResponse(meRequest, model, status), status)
        }

    private val mmRequest = MinMaxConfigRequest("any", sliceSelection, listOf(), OptimizationType.MAX, listOf())
    private val mmResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genSingleResponse(mm.computeResponse(mmRequest, model, status).merge, status)
        }

    private val opRequest = OptimizationRequest("any", sliceSelection, listOf(), OptimizationType.MAX, weightings)
    private val opResponse =
        ComputationStatusBuilder("62198eea-7fb4-4487-af2f-fba902b2b512", "", SINGLE).let { status ->
            genSingleResponse(op.computeResponse(opRequest, model, status).merge, status)
        }

    private val job = Job(
        "a5c55985-3741-4f5c-b15c-260b19a65613",
        "consistency",
        listOf("fast", "prio"),
        null,
        42,
        "br-node-1",
        LocalDateTime.now(),
        JobStatus.SUCCESS,
        startedAt = LocalDateTime.now().plusMinutes(1),
        executingInstance = "br-node-2",
        finishedAt = LocalDateTime.now().plusMinutes(2),
        null,
        0
    )

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelConsistency() {
        val excel = ExcelFormatter<Boolean>().generateSingleResult(ccResponse, job, ConsistencyExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/consistency.xlsx").outputStream())
    }

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelModelCounting() {
        val excel = ExcelFormatter<BigInteger>().generateSingleResult(mcResponse, job, ModelCountExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/counting.xlsx").outputStream())
    }

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelBackbone() {
        val excel = ExcelFormatter<BackboneType>().generateListResult(bbResponse, job, BackboneExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/backbone.xlsx").outputStream())
    }

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelModelEnumeration() {
        val excel = ExcelFormatter<Boolean>().generateListResult(meResponse, job, ModelEnumerationExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/enumeration.xlsx").outputStream())
    }

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelMinMaxConfiguration() {
        val excel = ExcelFormatter<Int>().generateSingleResult(mmResponse, job, MinMaxConfigExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/maxconfig.xlsx").outputStream())
    }

    @Test
    @Disabled("only generates the Excel file")
    fun testExcelOptimization() {
        val excel = ExcelFormatter<Int>().generateSingleResult(opResponse, job, OptimizationExcelFormatter())
        excel.write(File("src/test/resources/excel_exports/optimization.xlsx").outputStream())
    }
}

private fun <MAIN> genSingleResponse(
    res: List<SliceComputationResult<MAIN>>,
    status: ComputationStatusBuilder
): SingleComputationResponse<MAIN> =
    SingleComputationResponse(status.build(), res)

private fun <MAIN, ELEMENT> genListResponse(
    res: Map<ComputationElement<ELEMENT>, MergeResult<MAIN, *>>,
    status: ComputationStatusBuilder
):
        ListComputationResponse<MAIN, ELEMENT> =
    ListComputationResponse(status.build(), res.map { (e, r) -> ComputationElementResult(e, r.merge) })
