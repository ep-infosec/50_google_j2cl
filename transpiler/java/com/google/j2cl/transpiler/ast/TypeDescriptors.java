/*
 * Copyright 2015 Google Inc.
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
package com.google.j2cl.transpiler.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.j2cl.transpiler.ast.TypeDeclaration.Kind;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Utility class holding type descriptors that need to be referenced directly. */
public class TypeDescriptors {

  public DeclaredTypeDescriptor javaLangBoolean;
  public DeclaredTypeDescriptor javaLangByte;
  public DeclaredTypeDescriptor javaLangCharacter;
  public DeclaredTypeDescriptor javaLangDouble;
  public DeclaredTypeDescriptor javaLangFloat;
  public DeclaredTypeDescriptor javaLangInteger;
  public DeclaredTypeDescriptor javaLangLong;
  public DeclaredTypeDescriptor javaLangShort;
  public DeclaredTypeDescriptor javaLangString;
  public DeclaredTypeDescriptor javaLangStringBuilder;
  public DeclaredTypeDescriptor javaLangVoid;

  public DeclaredTypeDescriptor javaLangClass;
  public DeclaredTypeDescriptor javaLangObject;
  public DeclaredTypeDescriptor javaUtilArrays;
  public DeclaredTypeDescriptor javaUtilObjects;
  public DeclaredTypeDescriptor javaLangThrowable;
  public DeclaredTypeDescriptor javaLangNulPointerException;
  public DeclaredTypeDescriptor javaLangEnum;
  public DeclaredTypeDescriptor javaLangRunnable;
  public DeclaredTypeDescriptor javaLangIterable;
  public DeclaredTypeDescriptor javaUtilCollection;
  public DeclaredTypeDescriptor javaUtilIterator;
  public DeclaredTypeDescriptor javaUtilList;
  public DeclaredTypeDescriptor javaUtilMap;
  public DeclaredTypeDescriptor javaUtilSet;

  public DeclaredTypeDescriptor javaLangNumber;
  public DeclaredTypeDescriptor javaLangComparable;
  public DeclaredTypeDescriptor javaLangCharSequence;
  public DeclaredTypeDescriptor javaLangCloneable;
  public DeclaredTypeDescriptor javaIoSerializable;

  public DeclaredTypeDescriptor javaemulInternalWasmArray;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfByte;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfShort;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfChar;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfInt;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfLong;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfFloat;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfDouble;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfBoolean;
  public DeclaredTypeDescriptor javaemulInternalWasmArrayOfObject;

  public DeclaredTypeDescriptor javaemulInternalArrayHelper;
  public DeclaredTypeDescriptor javaemulInternalAsserts;
  public DeclaredTypeDescriptor javaemulInternalValueType;
  public DeclaredTypeDescriptor javaemulInternalPreconditions;
  public DeclaredTypeDescriptor javaemulInternalPrimitives;
  public DeclaredTypeDescriptor javaemulInternalEnums;
  public DeclaredTypeDescriptor javaemulInternalBoxedLightEnum;
  public DeclaredTypeDescriptor javaemulInternalBoxedComparableLightEnum;
  public DeclaredTypeDescriptor javaemulInternalConstructor;
  public DeclaredTypeDescriptor javaemulInternalPlatform;
  public DeclaredTypeDescriptor javaemulInternalExceptions;

  public ArrayTypeDescriptor javaLangObjectArray;

  // Common browser native types.
  public final DeclaredTypeDescriptor nativeFunction = createGlobalNativeTypeDescriptor("Function");
  public final DeclaredTypeDescriptor nativeObject = createGlobalNativeTypeDescriptor("Object");
  public final DeclaredTypeDescriptor nativeArray = createGlobalNativeTypeDescriptor("Array");
  public final DeclaredTypeDescriptor nativeError = createGlobalNativeTypeDescriptor("Error");
  public final DeclaredTypeDescriptor nativeTypeError =
      createGlobalNativeTypeDescriptor("TypeError");

  /**
   * Global window reference that is the enclosing class of native global methods and properties.
   */
  public final DeclaredTypeDescriptor globalNamespace = createGlobalNativeTypeDescriptor("");

  /** Primitive type descriptors and boxed type descriptors mapping. */
  private final BiMap<PrimitiveTypeDescriptor, DeclaredTypeDescriptor> boxedTypeByPrimitiveType =
      HashBiMap.create();

  private static final ThreadLocal<TypeDescriptors> typeDescriptors = new ThreadLocal<>();

  private static void set(TypeDescriptors typeDescriptors) {
    checkState(
        TypeDescriptors.typeDescriptors.get() == null,
        "TypeDescriptors has already been initialized and cannot be reassigned.");
    TypeDescriptors.typeDescriptors.set(typeDescriptors);
  }

  public static TypeDescriptors get() {
    checkState(isInitialized(), "TypeDescriptors must be initialized before access.");
    return typeDescriptors.get();
  }

  public static boolean isInitialized() {
    return typeDescriptors.get() != null;
  }

  public static TypeVariable getUnknownType() {
    return TypeVariable.createWildcard();
  }

  static DeclaredTypeDescriptor getBoxTypeFromPrimitiveType(PrimitiveTypeDescriptor primitiveType) {
    return get().boxedTypeByPrimitiveType.get(primitiveType);
  }

  static PrimitiveTypeDescriptor getPrimitiveTypeFromBoxType(TypeDescriptor boxType) {
    return get().boxedTypeByPrimitiveType.inverse().get(boxType.toNullable());
  }

  public static boolean isBoxedType(TypeDescriptor typeDescriptor) {
    return get()
            .boxedTypeByPrimitiveType
            .containsValue(typeDescriptor.toRawTypeDescriptor().toNullable())
        && !isJavaLangVoid(typeDescriptor);
  }

  public static boolean isNonVoidPrimitiveType(TypeDescriptor typeDescriptor) {
    return get().boxedTypeByPrimitiveType.containsKey(typeDescriptor.toRawTypeDescriptor())
        && !isPrimitiveVoid(typeDescriptor);
  }

  public static boolean isBoxedBooleanOrDouble(TypeDescriptor typeDescriptor) {
    return TypeDescriptors.isJavaLangBoolean(typeDescriptor)
        || TypeDescriptors.isJavaLangDouble(typeDescriptor);
  }

  public static boolean isPrimitiveBoolean(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.BOOLEAN.equals(typeDescriptor);
  }

  public static boolean isPrimitiveByte(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.BYTE.equals(typeDescriptor);
  }

  public static boolean isPrimitiveChar(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.CHAR.equals(typeDescriptor);
  }

  public static boolean isPrimitiveDouble(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.DOUBLE.equals(typeDescriptor);
  }

  public static boolean isPrimitiveFloat(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.FLOAT.equals(typeDescriptor);
  }

  public static boolean isPrimitiveFloatOrDouble(TypeDescriptor typeDescriptor) {
    return isPrimitiveFloat(typeDescriptor) || isPrimitiveDouble(typeDescriptor);
  }

  public static boolean isPrimitiveInt(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.INT.equals(typeDescriptor);
  }

  public static boolean isPrimitiveLong(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.LONG.equals(typeDescriptor);
  }

  public static boolean isPrimitiveShort(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.SHORT.equals(typeDescriptor);
  }

  public static boolean isPrimitiveVoid(TypeDescriptor typeDescriptor) {
    return PrimitiveTypes.VOID.equals(typeDescriptor);
  }

  public static boolean isPrimitiveBooleanOrDouble(TypeDescriptor typeDescriptor) {
    return isPrimitiveBoolean(typeDescriptor) || isPrimitiveDouble(typeDescriptor);
  }

  public static boolean isJavaLangObject(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangObject);
  }

  public static boolean isJavaLangString(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangString);
  }

  public static boolean isJavaLangByte(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangByte);
  }

  public static boolean isJavaLangShort(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangShort);
  }

  public static boolean isJavaLangInteger(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangInteger);
  }

  public static boolean isJavaLangCharacter(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangCharacter);
  }

  public static boolean isJavaLangLong(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangLong);
  }

  public static boolean isJavaLangFloat(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangFloat);
  }

  public static boolean isJavaLangDouble(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangDouble);
  }

  public static boolean isJavaLangBoolean(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangBoolean);
  }

  public static boolean isJavaLangVoid(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangVoid);
  }

  public static boolean isJavaLangComparable(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangComparable);
  }

  public static boolean isJavaLangCharSequence(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangCharSequence);
  }

  public static boolean isJavaLangNumber(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangNumber);
  }

  public static boolean isJavaIoSerializable(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaIoSerializable);
  }

  public static boolean isJavaLangCloneable(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangCloneable);
  }

  public static boolean isJavaLangClass(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangClass);
  }

  public static boolean isJavaLangEnum(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangEnum);
  }

  public static boolean isJavaLangThrowable(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(get().javaLangThrowable);
  }

  public static boolean isNumericPrimitive(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isPrimitive()
        && !isPrimitiveBoolean(typeDescriptor)
        && !isPrimitiveVoid(typeDescriptor);
  }

  public static boolean isIntegralPrimitiveType(TypeDescriptor typeDescriptor) {
    return isPrimitiveShort(typeDescriptor)
        || isPrimitiveByte(typeDescriptor)
        || isPrimitiveChar(typeDescriptor)
        || isPrimitiveInt(typeDescriptor)
        || isPrimitiveLong(typeDescriptor);
  }

  public static boolean isBoxedOrPrimitiveType(TypeDescriptor typeDescriptor) {
    return isBoxedType(typeDescriptor) || isNonVoidPrimitiveType(typeDescriptor);
  }

  public static boolean isBoxedTypeAsJsPrimitives(TypeDescriptor typeDescriptor) {
    return isBoxedBooleanOrDouble(typeDescriptor)
        || isJavaLangString(typeDescriptor)
        || isJavaLangVoid(typeDescriptor);
  }

  public static boolean isNonBoxedReferenceType(TypeDescriptor typeDescriptor) {
    return !typeDescriptor.isPrimitive() && !isBoxedType(typeDescriptor);
  }

  public static boolean isBoxedEnum(TypeDescriptor typeDescriptor) {
    return typeDescriptor.isSameBaseType(TypeDescriptors.get().javaemulInternalBoxedLightEnum)
        || typeDescriptor.isSameBaseType(
            TypeDescriptors.get().javaemulInternalBoxedComparableLightEnum);
  }

  public static boolean isWasmArraySubtype(TypeDescriptor typeDescriptor) {
    if (typeDescriptor instanceof DeclaredTypeDescriptor) {
      DeclaredTypeDescriptor superTypeDescriptor =
          ((DeclaredTypeDescriptor) typeDescriptor).getSuperTypeDescriptor();
      return get().javaemulInternalWasmArray.isSameBaseType(superTypeDescriptor);
    }
    return false;
  }

  public static TypeDescriptor getEnumBoxType(TypeDescriptor typeDescriptor) {
    checkState(AstUtils.isNonNativeJsEnum(typeDescriptor));
    TypeDescriptor boxType =
        typeDescriptor.getJsEnumInfo().supportsComparable()
            ? TypeDescriptors.get().javaemulInternalBoxedComparableLightEnum
            : TypeDescriptors.get().javaemulInternalBoxedLightEnum;
    TypeDescriptor specializedType =
        boxType.specializeTypeVariables(
            ImmutableMap.of(
                Iterables.getOnlyElement(boxType.getAllTypeVariables()), typeDescriptor));
    return typeDescriptor.isNullable() ? specializedType : specializedType.toNonNullable();
  }

  static Function<TypeVariable, ? extends TypeDescriptor> mappingFunctionFromMap(
      Map<TypeVariable, TypeDescriptor> replacingTypeDescriptorByTypeVariable) {
    return replacingTypeDescriptorByTypeVariable.isEmpty()
        ? Function.identity()
        : typeDescriptor ->
            replacingTypeDescriptorByTypeVariable.getOrDefault(typeDescriptor, typeDescriptor);
  }

  /** Holds the bootstrap types. */
  @SuppressWarnings("ImmutableEnumChecker")
  public enum BootstrapType {
    OBJECTS("vmbootstrap", "Objects"),
    COMPARABLES("vmbootstrap", "Comparables"),
    CHAR_SEQUENCES("vmbootstrap", "CharSequences"),
    NUMBERS("vmbootstrap", "Numbers"),
    ARRAYS("vmbootstrap", "Arrays"),
    CASTS("vmbootstrap", "Casts"),
    LONG_UTILS("vmbootstrap", "LongUtils"),
    JAVA_SCRIPT_OBJECT("vmbootstrap", "JavaScriptObject"),
    JAVA_SCRIPT_INTERFACE("vmbootstrap", "JavaScriptInterface"),
    JAVA_SCRIPT_FUNCTION("vmbootstrap", "JavaScriptFunction"),
    NATIVE_EQUALITY("nativebootstrap", "Equality"),
    NATIVE_UTIL("nativebootstrap", "Util"),
    NATIVE_LONG("nativebootstrap", "Long");

    private final DeclaredTypeDescriptor typeDescriptor;

    BootstrapType(String namespace, String name) {
      this.typeDescriptor = createSyntheticTypeDescriptor(Kind.CLASS, namespace, name);
    }

    public DeclaredTypeDescriptor getDescriptor() {
      return typeDescriptor;
    }

    public TypeDeclaration getDeclaration() {
      return typeDescriptor.getTypeDeclaration();
    }
  }

  // Not externally instantiable.
  private TypeDescriptors() {}

  public static DeclaredTypeDescriptor createPrimitiveMetadataTypeDescriptor(
      PrimitiveTypeDescriptor primitiveTypeDescriptor) {
    // Prepend "$" so that internal aliases start with "$".
    return createSyntheticTypeDescriptor(
        Kind.CLASS, "vmbootstrap.primitives", "$" + primitiveTypeDescriptor.getSimpleSourceName());
  }

  public static DeclaredTypeDescriptor createGlobalNativeTypeDescriptor(
      String jsName, TypeDescriptor... typeArgumentDescriptors) {
    return createNativeTypeDescriptor(JsUtils.JS_PACKAGE_GLOBAL, jsName, typeArgumentDescriptors);
  }

  static DeclaredTypeDescriptor createNativeTypeDescriptor(
      String jsNamespace, String className, TypeDescriptor... typeArgumentDescriptors) {
    return createSyntheticTypeDescriptor(
        Kind.INTERFACE, jsNamespace, className, typeArgumentDescriptors);
  }

  /**
   * Returns a TypeDescriptor that is not related to Java classes.
   *
   * <p>Used to synthesize type descriptors to Bootstrap types and native JS types.
   */
  private static DeclaredTypeDescriptor createSyntheticTypeDescriptor(
      Kind kind, String jsNamespace, String className, TypeDescriptor... typeArgumentDescriptors) {

    TypeDeclaration typeDeclaration =
        TypeDeclaration.newBuilder()
            .setClassComponents(ImmutableList.of(className))
            // Mark bootstrap classes as non native so that the goog.require doesn't reference
            // overlay.
            .setNative(!isBootstrapNamespace(jsNamespace))
            .setCustomizedJsNamespace(jsNamespace)
            .setPackageName(getSyntheticPackageName(jsNamespace))
            .setUnparameterizedTypeDescriptorFactory(
                () -> createSyntheticTypeDescriptor(kind, jsNamespace, className))
            // Synthetic type declarations do not need to have type variables.
            // TODO(b/63118697): Make sure declarations are consistent with descriptor w.r.t
            // type parameters.
            .setTypeParameterDescriptors(ImmutableList.of())
            .setVisibility(Visibility.PUBLIC)
            .setKind(kind)
            .build();

    return DeclaredTypeDescriptor.newBuilder()
        .setTypeDeclaration(typeDeclaration)
        .setTypeArgumentDescriptors(Arrays.asList(typeArgumentDescriptors))
        .build();
  }

  private static boolean isBootstrapNamespace(String jsNamespace) {
    String topNamespace = Iterables.get(Splitter.on('.').split(jsNamespace), 0);
    return topNamespace.equals("vmbootstrap")
        || topNamespace.equals("nativebootstrap")
        || topNamespace.equals("javaemul");
  }

  public static boolean isBootstrapNamespace(DeclaredTypeDescriptor typeDescriptor) {
    return isBootstrapNamespace(typeDescriptor.getQualifiedJsName());
  }

  /**
   * Synthesize a package name with an explicit prefix to avoid colliding with existing types. The
   * package name is part of the key for TypeDeclaration, and if two keys collide they will resolve
   * to the same TypeDeclaration creating potential for inconsistencies. The prefix "$synthetic" was
   * chosen to avoid collision with packages in the actual source code.
   */
  private static String getSyntheticPackageName(String jsNamespace) {
    if (isBootstrapNamespace(jsNamespace)) {
      // Avoid prepending synthetic to our runtime types. Those are not really synthetic. Bootstrap
      // types are handwritten non native types.
      return jsNamespace;
    }
    return "$synthetic." + jsNamespace;
  }

  /** Returns the unparameterized version of {@code typeDescriptors}. */
  @SuppressWarnings("unchecked")
  public static <T extends TypeDescriptor> ImmutableList<T> toUnparameterizedTypeDescriptors(
      List<T> typeDescriptors) {
    return typeDescriptors.stream()
        .map(TypeDescriptor::toUnparameterizedTypeDescriptor)
        .map(typeDescriptor -> (T) typeDescriptor)
        .collect(toImmutableList());
  }

  /** Return java implementation class for an array */
  public static DeclaredTypeDescriptor getWasmArrayType(ArrayTypeDescriptor arrayTypeDescriptor) {
    TypeDescriptor componentTypeDescriptor = arrayTypeDescriptor.getComponentTypeDescriptor();

    if (!componentTypeDescriptor.isPrimitive()) {
      return TypeDescriptors.get().javaemulInternalWasmArrayOfObject;
    }

    switch (((PrimitiveTypeDescriptor) componentTypeDescriptor).getSimpleSourceName()) {
      case "boolean":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfBoolean;
      case "short":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfShort;
      case "char":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfChar;
      case "byte":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfByte;
      case "int":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfInt;
      case "long":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfLong;
      case "float":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfFloat;
      case "double":
        return TypeDescriptors.get().javaemulInternalWasmArrayOfDouble;
      default:
        throw new AssertionError("Unsupported primitive type: " + componentTypeDescriptor);
    }
  }

  /** Builder for TypeDescriptors. */
  public static class SingletonBuilder {

    private final TypeDescriptors typeDescriptors = new TypeDescriptors();

    public void buildSingleton() {
      set(typeDescriptors);
      typeDescriptors.javaLangObjectArray =
          ArrayTypeDescriptor.newBuilder()
              .setComponentTypeDescriptor(typeDescriptors.javaLangObject)
              .build();
    }

    public SingletonBuilder addPrimitiveBoxedTypeDescriptorPair(
        PrimitiveTypeDescriptor primitiveType, DeclaredTypeDescriptor boxedType) {
      addReferenceType(boxedType);
      addBoxedTypeMapping(primitiveType, boxedType);
      return this;
    }

    public SingletonBuilder addReferenceType(DeclaredTypeDescriptor referenceType) {
      checkArgument(
          !referenceType.isPrimitive(),
          "%s is not a reference type",
          referenceType.getQualifiedSourceName());
      String name = referenceType.getQualifiedSourceName();
      switch (name) {
        case "java.io.Serializable":
          typeDescriptors.javaIoSerializable = referenceType;
          break;
        case "java.lang.Boolean":
          typeDescriptors.javaLangBoolean = referenceType;
          break;
        case "java.lang.Byte":
          typeDescriptors.javaLangByte = referenceType;
          break;
        case "java.lang.Character":
          typeDescriptors.javaLangCharacter = referenceType;
          break;
        case "java.lang.Double":
          typeDescriptors.javaLangDouble = referenceType;
          break;
        case "java.lang.Float":
          typeDescriptors.javaLangFloat = referenceType;
          break;
        case "java.lang.Integer":
          typeDescriptors.javaLangInteger = referenceType;
          break;
        case "java.lang.Long":
          typeDescriptors.javaLangLong = referenceType;
          break;
        case "java.lang.Short":
          typeDescriptors.javaLangShort = referenceType;
          break;
        case "java.lang.String":
          typeDescriptors.javaLangString = referenceType;
          break;
        case "java.lang.StringBuilder":
          typeDescriptors.javaLangStringBuilder = referenceType;
          break;
        case "java.lang.Void":
          typeDescriptors.javaLangVoid = referenceType;
          break;
        case "java.lang.Class":
          typeDescriptors.javaLangClass = referenceType;
          break;
        case "java.lang.Object":
          typeDescriptors.javaLangObject = referenceType;
          break;
        case "java.util.Arrays":
          typeDescriptors.javaUtilArrays = referenceType;
          break;
        case "java.util.Objects":
          typeDescriptors.javaUtilObjects = referenceType;
          break;
        case "java.lang.Throwable":
          typeDescriptors.javaLangThrowable = referenceType;
          break;
        case "java.lang.NullPointerException":
          typeDescriptors.javaLangNulPointerException = referenceType;
          break;
        case "java.lang.Number":
          typeDescriptors.javaLangNumber = referenceType;
          break;
        case "java.lang.Comparable":
          typeDescriptors.javaLangComparable = referenceType;
          break;
        case "java.lang.CharSequence":
          typeDescriptors.javaLangCharSequence = referenceType;
          break;
        case "java.lang.Cloneable":
          typeDescriptors.javaLangCloneable = referenceType;
          break;
        case "java.lang.Enum":
          typeDescriptors.javaLangEnum = referenceType;
          break;
        case "java.lang.Runnable":
          typeDescriptors.javaLangRunnable = referenceType;
          break;
        case "java.lang.Iterable":
          typeDescriptors.javaLangIterable = referenceType;
          break;
        case "java.util.Collection":
          typeDescriptors.javaUtilCollection = referenceType;
          break;
        case "java.util.Iterator":
          typeDescriptors.javaUtilIterator = referenceType;
          break;
        case "java.util.List":
          typeDescriptors.javaUtilList = referenceType;
          break;
        case "java.util.Map":
          typeDescriptors.javaUtilMap = referenceType;
          break;
        case "java.util.Set":
          typeDescriptors.javaUtilSet = referenceType;
          break;
        case "javaemul.internal.ValueType":
          typeDescriptors.javaemulInternalValueType = referenceType;
          break;
        case "javaemul.internal.InternalPreconditions":
          typeDescriptors.javaemulInternalPreconditions = referenceType;
          break;
        case "javaemul.internal.Primitives":
          typeDescriptors.javaemulInternalPrimitives = referenceType;
          break;
        case "javaemul.internal.Enums":
          typeDescriptors.javaemulInternalEnums = referenceType;
          break;
        case "javaemul.internal.Enums.BoxedLightEnum":
          typeDescriptors.javaemulInternalBoxedLightEnum = referenceType;
          break;
        case "javaemul.internal.Enums.BoxedComparableLightEnum":
          typeDescriptors.javaemulInternalBoxedComparableLightEnum = referenceType;
          break;
        case "javaemul.internal.Constructor":
          typeDescriptors.javaemulInternalConstructor = referenceType;
          break;
        case "javaemul.internal.Platform":
          typeDescriptors.javaemulInternalPlatform = referenceType;
          break;
        case "javaemul.internal.Exceptions":
          typeDescriptors.javaemulInternalExceptions = referenceType;
          break;
        case "javaemul.internal.WasmArray":
          typeDescriptors.javaemulInternalWasmArray = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfByte":
          typeDescriptors.javaemulInternalWasmArrayOfByte = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfShort":
          typeDescriptors.javaemulInternalWasmArrayOfShort = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfChar":
          typeDescriptors.javaemulInternalWasmArrayOfChar = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfInt":
          typeDescriptors.javaemulInternalWasmArrayOfInt = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfLong":
          typeDescriptors.javaemulInternalWasmArrayOfLong = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfFloat":
          typeDescriptors.javaemulInternalWasmArrayOfFloat = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfDouble":
          typeDescriptors.javaemulInternalWasmArrayOfDouble = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfBoolean":
          typeDescriptors.javaemulInternalWasmArrayOfBoolean = referenceType;
          break;
        case "javaemul.internal.WasmArray.OfObject":
          typeDescriptors.javaemulInternalWasmArrayOfObject = referenceType;
          break;
        case "javaemul.internal.ArrayHelper":
          typeDescriptors.javaemulInternalArrayHelper = referenceType;
          break;
        case "javaemul.internal.Asserts":
          typeDescriptors.javaemulInternalAsserts = referenceType;
          break;
        default:
          throw new IllegalStateException("Unexpected reference type in well known set: " + name);
      }
      return this;
    }

    private TypeDescriptor addBoxedTypeMapping(
        PrimitiveTypeDescriptor primitiveType, DeclaredTypeDescriptor boxedType) {
      return typeDescriptors.boxedTypeByPrimitiveType.put(primitiveType, boxedType);
    }
  }
}
