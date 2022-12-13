/*
 * Copyright 2021 Google Inc.
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
package wasm;

import static com.google.j2cl.integration.testing.Asserts.assertEquals;
import static com.google.j2cl.integration.testing.Asserts.assertFalse;
import static com.google.j2cl.integration.testing.Asserts.assertNull;
import static com.google.j2cl.integration.testing.Asserts.assertTrue;
import static com.google.j2cl.integration.testing.Asserts.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javaemul.internal.ArrayHelper;
import javaemul.internal.annotations.Wasm;
import jsinterop.annotations.JsMethod;

/**
 * Incrementally tests wasm features as they are being added.
 *
 * <p>This test will be removed when all WASM features are implemented and all integration tests are
 * enabled for WASM.
 */
public class Main {

  public static void main(String... args) throws Exception {
    testDynamicClassMethodDispatch();
    testSwitch();
    testWasmAnnotation();
    testClassLiterals();
    testTry();
    testArrayInstanceOf();
    testArrayGetClass();
    testWasmArrayApis();
    testArrayList();
    testHashMap();
    testLinkedHashMap();
    testSystemArrayCopy();
    testString();
    testParse();
    testLambda();
    testEnumMap_get_put();
    testEnumMap_remove();
    testEnumMap_containsKey();
  }

  static class A {
    public int m() {
      return 1;
    }
  }

  static class B extends A {
    @Override
    public int m() {
      return 2;
    }
  }

  private static void testDynamicClassMethodDispatch() {
    A a = new A();
    B b = new B();
    assertEquals(1, a.m());
    assertEquals(2, b.m());
    a = b;
    assertEquals(2, b.m());
  }

  enum MyEnum {
    A,
    B;
  }

  private static int next = 0;

  private static int returnsNext() {
    return next++;
  }

  private static void testSwitch() {
    // returnsNext = 0;
    switch (returnsNext()) {
      case 1:
        fail();
      case 0:
        break;
      default:
        fail();
    }

    // returnsNext() = 1
    switch (returnsNext()) {
      case 2:
        fail();
      default:
        break;
      case 0:
        fail();
    }

    MyEnum a = MyEnum.A;
    switch (a) {
      case B:
        fail();
      case A:
        break;
      default:
        fail();
    }
  }

  private static void testWasmAnnotation() {
    assertTrue(42 == multiply(6, 7));
  }

  @JsMethod // Exist to keep to test running under closure output
  @Wasm("i32.mul")
  private static native int multiply(int x, int y);

  private static class SomeClass {}

  private interface SomeInterface {}

  private enum SomeEnum {
    A
  }

  private static void testClassLiterals() {
    assertEquals("wasm.Main$SomeClass", SomeClass.class.getName());
    assertEquals(Object.class, SomeClass.class.getSuperclass());
    assertFalse(SomeClass.class.isEnum());
    assertFalse(SomeClass.class.isInterface());
    assertFalse(SomeClass.class.isArray());
    assertFalse(SomeClass.class.isPrimitive());
    assertEquals(
        "wasm.Main$SomeInterface", SomeInterface.class.getName());
    assertEquals(null, SomeInterface.class.getSuperclass());
    assertFalse(SomeInterface.class.isEnum());
    assertTrue(SomeInterface.class.isInterface());
    assertFalse(SomeInterface.class.isArray());
    assertFalse(SomeInterface.class.isPrimitive());
    assertEquals("wasm.Main$SomeEnum", SomeEnum.class.getName());
    assertEquals(Enum.class, SomeEnum.class.getSuperclass());
    assertTrue(SomeEnum.class.isEnum());
    assertFalse(SomeEnum.class.isInterface());
    assertFalse(SomeEnum.class.isArray());
    assertFalse(SomeEnum.class.isPrimitive());
    assertEquals("int", int.class.getName());
    assertEquals(null, int.class.getSuperclass());
    assertFalse(int.class.isEnum());
    assertFalse(int.class.isInterface());
    assertFalse(int.class.isArray());
    assertTrue(int.class.isPrimitive());
    assertEquals(int.class, int[].class.getComponentType());
    assertFalse(int[].class.isEnum());
    assertFalse(int[].class.isInterface());
    assertTrue(int[].class.isArray());
    assertFalse(int[].class.isPrimitive());
  }

  private static void testTry() {
    int i = 1;
    try {
      i += 10;
    } finally {
      i += 3;
    }
    assertEquals(14, i);
  }

  private static void testArrayInstanceOf() {
    Object intArray = new int[0];
    assertTrue(intArray instanceof int[]);
    assertFalse(intArray instanceof long[]);
    assertFalse(intArray instanceof Object[]);
    assertFalse(intArray instanceof SomeInterface[]);

    Object multiDimIntArray = new int[0][0];
    assertFalse(multiDimIntArray instanceof int[]);
    assertFalse(multiDimIntArray instanceof long[]);
    assertTrue(multiDimIntArray instanceof Object[]);
    assertTrue(multiDimIntArray instanceof int[][]);
    // TODO(b/184675805): enable when this is fixed.
    // assertFalse(multiDimIntArray instanceof long[][]);
    // assertFalse(multiDimIntArray instanceof SomeInterface[]);

    Object objectArray = new Object[0];
    assertFalse(objectArray instanceof int[]);
    assertFalse(objectArray instanceof long[]);
    assertTrue(objectArray instanceof Object[]);
    // TODO(b/184675805): enable when this is fixed.
    // assertFalse(objectArray instanceof int[][]);
    // assertFalse(objectArray instanceof long[][]);
    // assertFalse(objectArray instanceof SomeInterface[]);

    Object multiDimObjectArray = new Object[0][0];
    assertFalse(multiDimObjectArray instanceof int[]);
    assertFalse(multiDimObjectArray instanceof long[]);
    assertTrue(multiDimObjectArray instanceof Object[]);
    assertTrue(multiDimObjectArray instanceof Object[][]);
    // TODO(b/184675805): enable when this is fixed.
    // assertFalse(multiDimObjectArray instanceof int[][]);
    // assertFalse(multiDimObjectArray instanceof long[][]);
    // assertFalse(multiDimObjectArray instanceof SomeInterface[]);

    Object referencetArray = new SomeInterface[0];
    assertFalse(referencetArray instanceof int[]);
    assertFalse(referencetArray instanceof long[]);
    assertTrue(referencetArray instanceof Object[]);
    assertTrue(referencetArray instanceof SomeInterface[]);
    // TODO(b/184675805): enable when this is fixed.
    // assertFalse(referencetArray instanceof Object[][]);
    // assertFalse(referencetArray instanceof int[][]);
    // assertFalse(referencetArray instanceof long[][]);
  }

  private static void testArrayGetClass() {
    Object intArray = new int[0];
    assertEquals(int[].class, intArray.getClass());
    assertEquals(int.class, intArray.getClass().getComponentType());

    Object objectArray = new Object[0];
    assertEquals(Object[].class, objectArray.getClass());
    assertEquals(Object.class, objectArray.getClass().getComponentType());
  }

  private static void testWasmArrayApis() {
    Object[] array = new Object[0];
    ArrayHelper.push(array, "c");
    ArrayHelper.push(array, "d");
    assertEquals(array, new Object[] {"c", "d"});

    ArrayHelper.insertTo(array, 0, "a");
    assertEquals(array, new Object[] {"a", "c", "d"});
    ArrayHelper.insertTo(array, 1, "b");
    assertEquals(array, new Object[] {"a", "b", "c", "d"});

    ArrayHelper.removeFrom(array, 3, 1);
    ArrayHelper.removeFrom(array, 0, 1);
    assertEquals(array, new Object[] {"b", "c"});

    ArrayHelper.setLength(array, 5);
    assertEquals(array, new Object[] {"b", "c", null, null, null});
    ArrayHelper.setLength(array, 1);
    assertEquals(array, new Object[] {"b"});

    Object[] clone = ArrayHelper.clone(new Object[] {"a", "b"}, 0, 4);
    assertEquals(clone, new Object[] {"a", "b", null, null});
  }

  private static void testArrayList() {
    ArrayList<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");
    list.add("d");
    list.add("e");
    list.add(2, "c");
    assertEquals(list.toArray(), new Object[] {"a", "b", "c", "d", "e"});

    list.remove(4);
    list.remove(0);
    assertEquals(list.toArray(), new Object[] {"b", "c", "d"});

    list.clear();
    assertEquals(list.toArray(), new Object[] {});
    list.add("z");
    assertEquals(list.toArray(), new Object[] {"z"});
  }

  private static void testHashMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put("a", "A");
    map.put("b", "B");
    map.put("c", "C");

    String value =
        map.computeIfAbsent(
            "a",
            k -> {
              fail();
              return null;
            });
    assertEquals("A", value);
    assertTrue(map.containsKey("a"));
    assertEquals("A", map.get("a"));

    map.remove("a");
    value = map.computeIfAbsent("a", String::toUpperCase);
    assertEquals("A", value);
    assertTrue(map.containsKey("a"));
    assertEquals("A", map.get("a"));

    map.remove("a");
    value = map.computeIfAbsent("a", k -> null);
    assertNull(value);
    assertFalse(map.containsKey("a"));
  }

  private static void testLinkedHashMap() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("a", "1");
    map.put("b", "2");
    assertEquals(2, map.keySet().size());
    assertTrue(map.keySet().contains("a"));
    assertTrue(map.keySet().contains("b"));

    map.remove("a");
    assertEquals(1, map.keySet().size());
    assertTrue(map.keySet().contains("b"));
  }

  static class Foo {}

  static class Bar extends Foo {}

  private static void testSystemArrayCopy() {
    Foo[] fooArray = new Foo[4];
    Bar[] barArray = new Bar[4];
    Object[] src = new Object[] {new Bar(), new Bar(), new Foo(), new Bar()};
    System.arraycopy(src, 0, fooArray, 0, src.length);
    for (int i = 0; i < src.length; ++i) {
      assertEquals(src[i], fooArray[i]);
    }

    String[] strArray = new String[] {"0", "1", "2", "3"};

    System.arraycopy(strArray, 0, strArray, 1, strArray.length - 1);
    String[] strArrayExpected1 = new String[] {"0", "0", "1", "2"};
    for (int i = 0; i < strArray.length; ++i) {
      assertEquals("rev str copy index " + i, strArrayExpected1[i], strArray[i]);
    }

    System.arraycopy(strArray, 1, strArray, 0, strArray.length - 1);
    String[] strArrayExpected2 = new String[] {"0", "1", "2", "2"};
    for (int i = 0; i < strArray.length; ++i) {
      assertEquals("rev str copy index " + i, strArrayExpected2[i], strArray[i]);
    }
  }

  private static void testString() throws Exception {
    assertTrue(Arrays.equals(new byte[] {70, 111, 111}, "Foo".getBytes("UTF-8")));

    StringBuilder builder = new StringBuilder();
    builder.append("x").append("y").append(5).append(true);
    assertEquals("xy5true", builder.toString());

    assertEquals("5", String.valueOf(5));

    assertEquals("5.5", String.valueOf(5.5f));
    assertTrue(String.valueOf(123.459980f).startsWith("123.4599"));

    assertDoubleToStringEquals("5.5", 5.5d);
    assertDoubleToStringEquals("NaN", Double.NaN);
    assertDoubleToStringEquals("Infinity", Double.POSITIVE_INFINITY);
    assertDoubleToStringEquals("-Infinity", Double.NEGATIVE_INFINITY);
    assertDoubleToStringEquals("1.7976931348623157E308", Double.MAX_VALUE);
    // Double min value is different for Closure and Java.
    // assertDoubleToStringEquals("4.9E-324", Double.MIN_VALUE);

    // Ported from j2objc
    double d;
    d = Double.longBitsToDouble(0x470fffffffffffffL);
    assertDoubleToStringEquals("2.0769187434139308E34", d);
    d = Double.longBitsToDouble(0x4710000000000000L);
    assertDoubleToStringEquals("2.076918743413931E34", d);

    d = Double.longBitsToDouble(0x470000000000000aL);
    assertDoubleToStringEquals("1.0384593717069678E34", d);
    d = Double.longBitsToDouble(0x470000000000000bL);
    assertDoubleToStringEquals("1.038459371706968E34", d);

    d = Double.longBitsToDouble(0x4700000000000017L);
    assertDoubleToStringEquals("1.0384593717069708E34", d);
    d = Double.longBitsToDouble(0x4700000000000018L);
    assertDoubleToStringEquals("1.038459371706971E34", d);

    d = Double.longBitsToDouble(0x4700000000000024L);
    assertDoubleToStringEquals("1.0384593717069738E34", d);
    d = Double.longBitsToDouble(0x4700000000000025L);
    assertDoubleToStringEquals("1.038459371706974E34", d);

    d = Double.longBitsToDouble(0x4700000000000031L);
    assertDoubleToStringEquals("1.0384593717069768E34", d);
    d = Double.longBitsToDouble(0x4700000000000032L);
    assertDoubleToStringEquals("1.038459371706977E34", d);

    d = Double.longBitsToDouble(0x470000000000003eL);
    assertDoubleToStringEquals("1.0384593717069798E34", d);
    d = Double.longBitsToDouble(0x470000000000003fL);
    assertDoubleToStringEquals("1.03845937170698E34", d);

    d = Double.longBitsToDouble(0x7e00000000000003L);
    assertDoubleToStringEquals("8.371160993642719E298", d);
    d = Double.longBitsToDouble(0x7e00000000000004L);
    assertDoubleToStringEquals("8.37116099364272E298", d);

    d = Double.longBitsToDouble(0x7e00000000000008L);
    assertDoubleToStringEquals("8.371160993642728E298", d);
    d = Double.longBitsToDouble(0x7e00000000000009L);
    assertDoubleToStringEquals("8.37116099364273E298", d);

    d = Double.longBitsToDouble(0x7e00000000000013L);
    assertDoubleToStringEquals("8.371160993642749E298", d);
    d = Double.longBitsToDouble(0x7e00000000000014L);
    assertDoubleToStringEquals("8.37116099364275E298", d);

    d = Double.longBitsToDouble(0x7e00000000000023L);
    assertDoubleToStringEquals("8.371160993642779E298", d);
    d = Double.longBitsToDouble(0x7e00000000000024L);
    assertDoubleToStringEquals("8.37116099364278E298", d);

    d = Double.longBitsToDouble(0x7e0000000000002eL);
    assertDoubleToStringEquals("8.371160993642799E298", d);
    d = Double.longBitsToDouble(0x7e0000000000002fL);
    assertDoubleToStringEquals("8.3711609936428E298", d);

    d = Double.longBitsToDouble(0xda00000000000001L);
    assertDoubleToStringEquals("-3.3846065602060736E125", d);
    d = Double.longBitsToDouble(0xda00000000000002L);
    assertDoubleToStringEquals("-3.384606560206074E125", d);

    d = Double.longBitsToDouble(0xda00000000000005L);
    assertDoubleToStringEquals("-3.3846065602060766E125", d);
    d = Double.longBitsToDouble(0xda00000000000006L);
    assertDoubleToStringEquals("-3.384606560206077E125", d);

    d = Double.longBitsToDouble(0xda00000000000009L);
    assertDoubleToStringEquals("-3.3846065602060796E125", d);
    d = Double.longBitsToDouble(0xda0000000000000aL);
    assertDoubleToStringEquals("-3.38460656020608E125", d);

    d = Double.longBitsToDouble(0xda0000000000000dL);
    assertDoubleToStringEquals("-3.3846065602060826E125", d);
    d = Double.longBitsToDouble(0xda0000000000000eL);
    assertDoubleToStringEquals("-3.384606560206083E125", d);
  }

  private static void assertDoubleToStringEquals(String expected, double d) {
    // Closure version uses 'e+' for exponential so normalize it back to Java style.
    assertEquals(expected, String.valueOf(d).replace("e+", "E"));
  }

  private static void testParse() {
    assertEquals((byte) 123, Byte.parseByte("123"));
    assertEquals((byte) 123, Byte.parseByte("+123"));
    assertEquals((byte) -123, Byte.parseByte("-123"));
    assertEquals((byte) 0, Byte.parseByte("0"));
    assertEquals((byte) Byte.MAX_VALUE, Byte.parseByte(String.valueOf(Byte.MAX_VALUE)));
    assertEquals((byte) Byte.MIN_VALUE, Byte.parseByte(String.valueOf(Byte.MIN_VALUE)));
    assertEquals((byte) 51, Byte.parseByte("123", 6));

    assertEquals(12345, Integer.parseInt("12345"));
    assertEquals(12345, Integer.parseInt("+12345"));
    assertEquals(-12345, Integer.parseInt("-12345"));
    assertEquals(0, Integer.parseInt("0"));
    assertEquals(Integer.MAX_VALUE, Integer.parseInt(String.valueOf(Integer.MAX_VALUE)));
    assertEquals(Integer.MIN_VALUE, Integer.parseInt(String.valueOf(Integer.MIN_VALUE)));
    assertEquals(1865, Integer.parseInt("12345", 6));

    assertEquals(0L, Long.parseLong("0"));
    assertEquals(100000000000L, Long.parseLong("100000000000"));
    assertEquals(100000000000L, Long.parseLong("+100000000000"));
    assertEquals(-100000000000L, Long.parseLong("-100000000000"));
    assertEquals(10L, Long.parseLong("010"));
    assertEquals(Long.MAX_VALUE, Long.parseLong(String.valueOf(Long.MAX_VALUE)));
    assertEquals(Long.MIN_VALUE, Long.parseLong(String.valueOf(Long.MIN_VALUE)));

    assertEquals(1865, Integer.parseInt("12345", 6));
    assertEquals(0xdeadbeefdeadL, Long.parseLong("deadbeefdead", 16));
    assertEquals(-0xdeadbeefdeadL, Long.parseLong("-deadbeefdead", 16));

    assertEquals(0.0, Double.parseDouble("0"));
    assertEquals(100.0, Double.parseDouble("1e2"));
    assertEquals(-100.0, Double.parseDouble("-1e2"));
    assertEquals(-1.5, Double.parseDouble("-1.5"));
    assertEquals(3.0, Double.parseDouble("3."));
    assertEquals(0.5, Double.parseDouble(".5"));
    assertEquals(2.98e8, Double.parseDouble("2.98e8"));
    assertEquals(-2.98e-8, Double.parseDouble("-2.98e-8"));
    assertEquals(+2.98E+8, Double.parseDouble("+2.98E+8"));
    assertEquals(2.56789e1, Double.parseDouble("2.56789e1"));
    assertEquals(2.56789e1, Double.parseDouble("  2.56789E+1"));
    assertEquals(2.56789e1, Double.parseDouble("2.56789e1   "));
    assertEquals(1.0d, Double.parseDouble("1.0f"));
    assertEquals(1.0d, Double.parseDouble("1.0F"));
    assertEquals(1.0d, Double.parseDouble("1.0d"));
    assertEquals(1.0d, Double.parseDouble("1.0D"));
    assertEquals(Double.NaN, Double.parseDouble("+NaN"));
    assertEquals(Double.NaN, Double.parseDouble("NaN"));
    assertEquals(Double.NaN, Double.parseDouble("-NaN"));
    assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("+Infinity"));
    assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("Infinity"));
    assertEquals(Double.NEGATIVE_INFINITY, Double.parseDouble("-Infinity"));
  }

  interface Function {
    int apply();
  }

  private static void testLambda() {
    boolean b = true;
    Function f =
        () -> {
          if (!b) {
            return 1;
          } else {
            return 2;
          }
        };
    assertEquals(2, f.apply());
  }

  private static void testEnumMap_get_put() {
    EnumMap<MyEnum, Integer> enumMap = new EnumMap<>(MyEnum.class);
    assertEquals(0, enumMap.size());

    enumMap.put(MyEnum.B, 5);
    assertEquals(1, enumMap.size());
    assertEquals(null, enumMap.get(MyEnum.A));
    assertEquals(Integer.valueOf(5), enumMap.get(MyEnum.B));

    enumMap.put(MyEnum.B, 4);
    assertEquals(1, enumMap.size());
    assertEquals(null, enumMap.get(MyEnum.A));
    assertEquals(Integer.valueOf(4), enumMap.get(MyEnum.B));

    enumMap.put(MyEnum.A, 3);
    assertEquals(2, enumMap.size());
    assertEquals(Integer.valueOf(3), enumMap.get(MyEnum.A));
    assertEquals(Integer.valueOf(4), enumMap.get(MyEnum.B));

    enumMap.put(MyEnum.A, 7);
    assertEquals(2, enumMap.size());
    assertEquals(Integer.valueOf(7), enumMap.get(MyEnum.A));
    assertEquals(Integer.valueOf(4), enumMap.get(MyEnum.B));
  }

  private static void testEnumMap_remove() {
    EnumMap<MyEnum, Integer> enumMap = new EnumMap<>(MyEnum.class);
    assertEquals(null, enumMap.remove(MyEnum.A));
    assertEquals(null, enumMap.remove(MyEnum.B));

    enumMap.put(MyEnum.A, 3);
    enumMap.put(MyEnum.B, 7);
    assertEquals(Integer.valueOf(3), enumMap.remove(MyEnum.A));
    assertEquals(null, enumMap.remove(MyEnum.A));
    assertEquals(Integer.valueOf(7), enumMap.remove(MyEnum.B));
    assertEquals(null, enumMap.remove(MyEnum.B));
  }

  private static void testEnumMap_containsKey() {
    EnumMap<MyEnum, Integer> enumMap = new EnumMap<>(MyEnum.class);
    assertFalse(enumMap.containsKey(MyEnum.A));
    assertFalse(enumMap.containsKey(MyEnum.B));

    enumMap.put(MyEnum.A, 3);
    assertTrue(enumMap.containsKey(MyEnum.A));
    assertFalse(enumMap.containsKey(MyEnum.B));

    enumMap.put(MyEnum.B, 3);
    assertTrue(enumMap.containsKey(MyEnum.A));
    assertTrue(enumMap.containsKey(MyEnum.B));
  }
}
