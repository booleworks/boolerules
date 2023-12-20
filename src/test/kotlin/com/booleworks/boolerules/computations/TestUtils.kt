package com.booleworks.boolerules.computations

import com.booleworks.boolerules.computations.generic.ComputationDetail
import com.booleworks.boolerules.computations.generic.SliceComputationResult
import com.booleworks.boolerules.computations.generic.SliceDO
import com.booleworks.boolerules.computations.generic.SplitComputationDetail

fun <DETAIL : ComputationDetail> detailsForSlice(slice: SliceDO, details: List<SplitComputationDetail<DETAIL>>?) = details!!.first { it.slice == slice }

fun <MAIN> resForMain(main: MAIN, results: List<SliceComputationResult<MAIN>>) = results.first { it.result == main }
