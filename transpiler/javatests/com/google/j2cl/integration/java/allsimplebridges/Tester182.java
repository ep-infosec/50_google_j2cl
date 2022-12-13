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
package allsimplebridges;

import static com.google.j2cl.integration.testing.Asserts.assertTrue;

public class Tester182 {
  static interface I1 {
    default String get(String value) {
      return "I1.get";
    }
  }

  static class C1<T> {
    C1() {}
    public String get(T value) {
      return "C1.get";
    }
  }

  @SuppressWarnings("unchecked")
  static class C2 extends C1 implements I1 {
    C2() {}
    @SuppressWarnings("MissingOverride")
    public String get(Object value) {
      return "C2.get";
    }
  }

  @SuppressWarnings("unchecked")
  public static void test() {
    C2 s = new C2();
    assertTrue(s.get(new Object()).equals("C2.get"));
    assertTrue(((C1) s).get("").equals("C2.get"));
    assertTrue(((I1) s).get("").equals("I1.get"));
  }
}
