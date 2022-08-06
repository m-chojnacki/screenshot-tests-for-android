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

package com.facebook.testing.screenshot.internal;

import static org.junit.Assert.assertEquals;

import androidx.test.annotation.UiThreadTest;
import org.junit.Test;

/** Tests {@link TestNameDetector} */
public class TestNameDetectorTest {
  @Test
  @UiThreadTest
  public void testTestNameIsDetected() throws Throwable {
    assertEquals("testTestNameIsDetected", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }

  @Test
  public void testTestNameIsDetectedOnNonUiThread() throws Throwable {
    assertEquals("testTestNameIsDetectedOnNonUiThread", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }

  @Test
  @UiThreadTest
  public void testTestNameIsDetectedThroughExtraMethod() throws Throwable {
    extraLayerMethod();
  }

  private void extraLayerMethod() {
    assertEquals("testTestNameIsDetectedThroughExtraMethod", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }
}
