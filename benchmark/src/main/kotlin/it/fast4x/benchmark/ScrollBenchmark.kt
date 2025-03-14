package it.fast4x.benchmark


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
 *      Copyright 2023 The Android Open Source Project
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import it.fast4x.benchmark.util.DEFAULT_ITERATIONS
import it.fast4x.benchmark.util.TARGET_PACKAGE
import it.fast4x.benchmark.util.FrameMetric
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun noCompilation() = scrollBenchmark(CompilationMode.None())

    @Test
    fun defaultCompilation() = scrollBenchmark(CompilationMode.DEFAULT)

    @Test
    fun full() = scrollBenchmark(CompilationMode.Full())

    private fun scrollBenchmark(compilationMode: CompilationMode) {
        var firstStart = true
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(
                TraceSectionMetric("ClickTrace"),
                StartupTimingMetric(),
                FrameMetric()
            ),
            compilationMode = compilationMode,
            startupMode = null,
            iterations = DEFAULT_ITERATIONS,
            setupBlock = {
                if (firstStart) {
//                    val intent = Intent("$packageName.MainActivity")
//                    startActivityAndWait(intent)
                    startActivityAndWait()
                    firstStart = false
                }
            }
        ) {
            device.wait(Until.hasObject(By.scrollable(true)), 5_000)

            val scrollableObject = device.findObject(By.scrollable(true))
            if (scrollableObject == null) {
                TestCase.fail("No scrollable view found in hierarchy")
            }
            scrollableObject.setGestureMargin(device.displayWidth / 10)
            scrollableObject?.apply {
                repeat(2) {
                    scroll(Direction.DOWN, 0.3f)
//                    fling(Direction.DOWN)
                }
                repeat(2) {
                    scroll(Direction.UP, 0.3f)
//                    fling(Direction.UP)
                }
            }
        }
    }
}
