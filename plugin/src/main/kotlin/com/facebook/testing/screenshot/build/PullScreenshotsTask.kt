/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.testing.screenshot.build

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import com.usefulness.testing.screenshot.build.ScreenshotTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class PullScreenshotsTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val layout: ProjectLayout,
    private val execOperations: ExecOperations,
) : ScreenshotTask(objectFactory = objectFactory) {
    companion object {
        fun taskName(variantName: String) = "pull${variantName.replaceFirstChar(Char::titlecase)}Screenshots"

        internal fun ProjectLayout.getReportDir(variantName: String): File =
            buildDirectory.file("screenshots${variantName.replaceFirstChar(Char::titlecase)}").get().asFile
    }

    private lateinit var apkPath: File

    @Input
    protected var verify = false

    @Input
    protected var record = false

    init {
        description = "Pull screenshots from your device"
        group = ScreenshotsPlugin.GROUP
    }

    override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
        super.init(variant, extension)
        val output = variant.outputs.find { it is ApkVariantOutput } as? ApkVariantOutput
            ?: throw IllegalArgumentException("Can't find APK output")
        val packageTask = variant.packageApplicationProvider.orNull
            ?: throw IllegalArgumentException("Can't find package application provider")

        apkPath = File(packageTask.outputDirectory.asFile.get(), output.outputFileName)
    }

    @TaskAction
    fun pullScreenshots() {
        val codeSource = ScreenshotsPlugin::class.java.protectionDomain.codeSource
        val jarFile = File(codeSource.location.toURI().path)
        val referenceDir = referenceDir.orNull?.let(::File)
        val outputDir = if (verify && referenceDir != null) {
            referenceDir
        } else {
            layout.getReportDir(variantName.get())
        }

        assert(if (verify) outputDir.exists() else !outputDir.exists())

        execOperations.exec {
            it.executable = pythonExecutable.get()
            it.environment("PYTHONPATH", jarFile)

            it.args = mutableListOf(
                "-m",
                "android_screenshot_tests.pull_screenshots",
                "--apk",
                apkPath.absolutePath,
                "--test-run-id",
                testRunId.get(),
                "--temp-dir",
                outputDir.absolutePath,
            )
                .apply {
                    if (verify) {
                        add("--verify")
                    } else if (record) {
                        add("--record")
                    }

                    if (verify || record) {
                        add(recordDir.get())
                    }

                    if (verify && failureDir.isPresent) {
                        add("--failure-dir")
                        add(failureDir.get())
                    }

                    if (multipleDevices.get()) {
                        add("--multiple-devices")
                        add("true")
                    }

                    if (verify) {
                        add("--no-pull")
                    }

                    if (bundleResults.get()) {
                        add("--bundle-results")
                    }
                }

            println(it.args)
        }
    }
}
