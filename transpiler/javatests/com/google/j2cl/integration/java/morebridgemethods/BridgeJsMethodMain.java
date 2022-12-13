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

import jsinterop.annotations.JsMethod;

public class BridgeJsMethodMain {
  public static class A<T> {
    @JsMethod
    public T fun(T t) {
      return t;
    }
  }

  public static class B extends A<String> {
    // It is a JsMethod itself and also overrides a JsMethod.
    @Override
    @JsMethod
    public String fun(String s) {
      return s + "abc";
    }
  }

  public static Object callFunByA(A a, Object o) {
    return a.fun(o);
  }

  public static void test(String... args) {
    // TODO(b/150876433): enable when fixed.
    // Asserts.assertThrowsClassCastException(() -> callFunByA(new B(), 1));
  }
}
