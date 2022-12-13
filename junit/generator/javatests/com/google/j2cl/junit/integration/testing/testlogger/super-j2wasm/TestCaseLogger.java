/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.junit.integration.testing.testlogger;

import static jsinterop.annotations.JsPackage.GLOBAL;

import jsinterop.annotations.JsMethod;

/** Calling stub for window.console. */
public class TestCaseLogger {

  @JsMethod(namespace = GLOBAL, name = "goog.testing.TestCase.saveMessage")
  public static native void saveMessage(String.NativeString message);

  public static void log(String message) {
    saveMessage(("[java_message_from_test] " + message).toJsString());
  }
}
