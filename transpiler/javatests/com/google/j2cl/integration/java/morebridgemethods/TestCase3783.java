/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package morebridgemethods;

import static com.google.j2cl.integration.testing.Asserts.assertTrue;

import jsinterop.annotations.JsType;

public class TestCase3783 {
  static class B {
    @SuppressWarnings("unused")
    public String get(String value) {
      return "B get String";
    }
  }

  @JsType
  static interface CI1 {
    String get(String value);
  }

  static class C extends B implements CI1 {}

  @SuppressWarnings("unchecked")
  public static void test() {
    C c = new C();
    assertTrue(((B) c).get("").equals("B get String"));
    assertTrue(c.get("").equals("B get String"));
    assertTrue(((CI1) c).get("").equals("B get String"));
  }
}
