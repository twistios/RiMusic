package it.fast4x.benchmark.util


/*
 * Copyright (c) 2025 twistios
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html#license-text.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *      CC BY SA Kevin Ding https://stackoverflow.com/users/6935264/kevin-ding
 *
 *      https://stackoverflow.com/a/77248079/7380827
 */

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.TraceMetric
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.benchmark.perfetto.PerfettoTraceProcessor
import androidx.benchmark.perfetto.Row

// Slice类型
enum class FrameSliceType(val value:String) {
    Expected("Expected Timeline"),
    Actual("Actual Timeline"),
    UiThread("UIThread"),
    RenderThread("RenderThread")
}

// 用来保存Slice
data class Slice(
    val type:String,
    val name:String,
    val start:Long,
    val duration:Long
) {
    val end:Long = start + duration

    val frameId:Int? = when(type) {
        FrameSliceType.Actual.value -> name.toIntOrNull()
        FrameSliceType.Expected.value  -> name.toIntOrNull()
        FrameSliceType.UiThread.value -> name.split(" ")[1].toIntOrNull()
        FrameSliceType.RenderThread.value -> name.substringAfter(" ").toIntOrNull()
        else -> { throw IllegalArgumentException("unexpected slice type") }
    }

    fun contains(targetTs:Long):Boolean {

        return targetTs in start..end
    }
}

// 用来将Row快速转换为Slice
@OptIn(ExperimentalPerfettoTraceProcessorApi::class)
fun Sequence<Row>.toSlices():List<Slice> {

    // 转成List<Slice>对象
    val list = map {

        Slice(it.string("type"), it.string("name"), it.long("ts"), it.long("dur"))

    }.toList()

    // 将ts调整为offset模式
    return list.map {

        it.copy(start = (it.start - list.first().start))

    }.filter {

        // 过滤掉没有结束的Slice
        it.duration != -1L
    }
}

enum class SubMetric {
    FrameDurationCpuMs,
    FrameDurationUiMs,
    FrameOverrunMs;
}

// 帧数据
class FrameData (
    private val expected:Slice,
    private val actual:Slice,
    private val uiThread:Slice,
    private val renderThread:Slice
) {

    fun get(subMetric:SubMetric):Long {
        return when(subMetric) {
            SubMetric.FrameDurationCpuMs -> renderThread.end - uiThread.start
            SubMetric.FrameDurationUiMs -> uiThread.duration
            SubMetric.FrameOverrunMs -> maxOf(actual.end, renderThread.end) - expected.end
        }
    }
}

// 使用FrameId查找对应的Slice，找不到就返回Null
fun List<Slice>.binarySearchByFrameId(frameId:Int):Slice? {

    val index = binarySearch { target ->

        val targetFrameId = target.frameId ?: run {
            val prefix = "Choreographer#doFrame - resynced to "
            if (target.name.startsWith(prefix, 0)) {
                target.name.substringAfter(prefix, "").split(" ")[0].toIntOrNull()
            } else {
                0
            }
        }!!

        targetFrameId - frameId }

    return if(index >= 0) {
        // get slice
        get(index)
    } else {
        // null
        null
    }
}




/**
 * get Frame metrics
 */
@OptIn(ExperimentalMetricApi::class)
class FrameMetric:TraceMetric() {

    @OptIn(ExperimentalPerfettoTraceProcessorApi::class)
    override fun getResult(
        captureInfo: CaptureInfo,
        traceSession: PerfettoTraceProcessor.Session
    ): List<Measurement> {

        val rows = traceSession.query(query = FrameMetricQuery.getFullQuery(captureInfo.targetPackageName))
        var slices = rows.toSlices()

        // 对数据分组
        val groupedData = slices.groupBy { it.type }
        var expectedSlices = groupedData.getOrElse(FrameSliceType.Expected.value) { listOf() }
        var actualSlices = groupedData.getOrElse(FrameSliceType.Actual.value) { listOf() }
        var uiThreadSlices = groupedData.getOrElse(FrameSliceType.UiThread.value) { listOf() }
        var renderThreadSlices = groupedData.getOrElse(FrameSliceType.RenderThread.value) { listOf() }

        val frameDataList = renderThreadSlices.mapNotNull { rtSlice ->
            val uiSlice = uiThreadSlices.binarySearchByFrameId(rtSlice.frameId!!)
            if(uiSlice != null) {

                val actualSlice = actualSlices.binarySearchByFrameId(uiSlice.frameId!!)

                if(actualSlice != null) {

                    val expectedSlice = expectedSlices.binarySearchByFrameId(actualSlice.frameId!!)

                    if(expectedSlice != null) {
                        FrameData(
                            expectedSlice,
                            actualSlice,
                            uiSlice,
                            rtSlice
                        )
                    } else { null }
                } else { null }
            } else { null }
        }

        // 先将FrameData数据按照SubMetrics分为几类，然后将得到的Map转换成指标
        return frameDataList.getSubMetrics().map {
            Measurement(it.key.name, it.value.map { v -> v.toDouble() / 1000 / 1000 })
        }
    }
}


// 将FrameData转换为Metric
fun List<FrameData>.getSubMetrics():Map<SubMetric, List<Long>> {

    // associatedWith 将值映射为k,v的Map
    return SubMetric.values().associateWith {
        // 产生对应的List<Long>指标
        map { frameData -> frameData.get(it) }
    }
}


class FrameMetricQuery {

    companion object {
        fun getFullQuery(packageName:String): String {

            return """
                select ts, dur, type, name
                from (
                    --- 查询Expected Timeline与Actual Timeline
                    select process_track.name as type, slice.name as name, slice.ts as ts, slice.dur as dur
                    from slice
                    left join process_track on slice.track_id = process_track.id
                    left join process using(upid)
                    where
                        --- 限定track类型
                        process_track.name in ("Expected Timeline", "Actual Timeline")
                        --- 限定指定的包
                        and (process.name = "$packageName" or process.name like "$packageName:%")
                union
                    --- 查询UIThread中的事件
                    select  "UIThread" as type, slice.name, slice.ts, slice.dur
                    from slice
                    left join thread_track on slice.track_id = thread_track.id
                    left join thread using(utid)
                    left join process using(upid)
                    where
                        --- 限定事件类型
                        (slice.name like "Choreographer#doFrame%" )
                        --- 限定指定的包
                        and (process.name = "$packageName" or process.name like "$packageName:%")
    
                union
                    --- 查询RenderThread中的事件
                    select  "RenderThread" as type, slice.name, slice.ts, slice.dur
                    from slice
                    left join thread_track on slice.track_id = thread_track.id
                    left join thread using(utid)
                    left join process using(upid)
                    where
                        --- 限定事件类型
                        (thread.name like "RenderThread%" and slice.name like "drawFrames%")
                        --- 限定指定的包
                        and (process.name = "$packageName" or process.name like "$packageName:%")
                ) order by ts asc  --- 默认按照ts升序排序
            """.trimIndent()
        }
    }
}