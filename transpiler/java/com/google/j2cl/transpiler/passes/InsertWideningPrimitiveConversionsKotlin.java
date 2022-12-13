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
package com.google.j2cl.transpiler.passes;

import com.google.j2cl.transpiler.ast.AbstractRewriter;
import com.google.j2cl.transpiler.ast.ArrayAccess;
import com.google.j2cl.transpiler.ast.AstUtils;
import com.google.j2cl.transpiler.ast.CastExpression;
import com.google.j2cl.transpiler.ast.CompilationUnit;
import com.google.j2cl.transpiler.ast.Expression;
import com.google.j2cl.transpiler.ast.Node;
import com.google.j2cl.transpiler.ast.NumberLiteral;
import com.google.j2cl.transpiler.ast.PrimitiveTypeDescriptor;
import com.google.j2cl.transpiler.ast.PrimitiveTypes;
import com.google.j2cl.transpiler.ast.TypeDescriptor;
import com.google.j2cl.transpiler.ast.TypeDescriptors;

/**
 * Inserts a widening operation when a smaller primitive type is being put into a large primitive
 * type slot in assignment, binary numeric promotion, cast and method invocation conversion
 * contexts.
 */
public class InsertWideningPrimitiveConversionsKotlin extends NormalizationPass {

  @Override
  public void applyTo(CompilationUnit compilationUnit) {
    compilationUnit.accept(new ConversionContextVisitor(getContextRewriter()));

    // TODO(b/238147260): Move to rewriteUnaryNumericPromotionContext() when the bug is fixed.
    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteArrayAccess(ArrayAccess arrayAccess) {
            Expression indexExpression = arrayAccess.getIndexExpression();
            return shouldWiden(PrimitiveTypes.INT, indexExpression)
                ? ArrayAccess.Builder.from(arrayAccess)
                    .setIndexExpression(widenTo(PrimitiveTypes.INT, indexExpression))
                    .build()
                : arrayAccess;
          }
        });
  }

  private ConversionContextVisitor.ContextRewriter getContextRewriter() {
    return new ConversionContextVisitor.ContextRewriter() {

      @Override
      public Expression rewriteTypeConversionContext(
          TypeDescriptor toTypeDescriptor,
          TypeDescriptor declaredTypeDescriptor,
          Expression expression) {
        return shouldWiden(toTypeDescriptor, expression)
            ? widenTo(toTypeDescriptor, expression)
            : expression;
      }

      @Override
      public Expression rewriteBinaryNumericPromotionContext(
          TypeDescriptor otherOperandTypeDescriptor, Expression operand) {
        if (!isBasicNumericType(operand.getTypeDescriptor())
            || !isBasicNumericType(otherOperandTypeDescriptor)) {
          return operand;
        }
        TypeDescriptor widenedTypeDescriptor =
            AstUtils.getNumericBinaryExpressionTypeDescriptor(
                operand.getTypeDescriptor().toUnboxedType(),
                otherOperandTypeDescriptor.toUnboxedType());
        return shouldWiden(widenedTypeDescriptor, operand)
            ? widenTo(widenedTypeDescriptor, operand)
            : operand;
      }

      @Override
      public Expression rewriteCastContext(CastExpression castExpression) {
        return shouldWiden(castExpression.getCastTypeDescriptor(), castExpression.getExpression())
            ? widenTo(castExpression.getCastTypeDescriptor(), castExpression.getExpression())
            : castExpression;
      }
    };
  }

  private static boolean isBasicNumericType(TypeDescriptor type) {
    return (type.isPrimitive() || TypeDescriptors.isBoxedType(type))
        && TypeDescriptors.isNumericPrimitive(type.toUnboxedType());
  }

  private static boolean shouldWiden(TypeDescriptor toTypeDescriptor, Expression expression) {
    TypeDescriptor fromTypeDescriptor = expression.getTypeDescriptor();
    if (!TypeDescriptors.isNumericPrimitive(fromTypeDescriptor)
        || !TypeDescriptors.isNumericPrimitive(toTypeDescriptor)) {
      // Widening only applies between primitive types.
      return false;
    }
    return ((PrimitiveTypeDescriptor) toTypeDescriptor)
        .isWiderThan((PrimitiveTypeDescriptor) fromTypeDescriptor);
  }

  private Expression widenTo(TypeDescriptor toTypeDescriptor, Expression expression) {
    // Widen literals at compile time.
    if (expression instanceof NumberLiteral) {
      PrimitiveTypeDescriptor literalTypeDescriptor = (PrimitiveTypeDescriptor) toTypeDescriptor;
      return new NumberLiteral(literalTypeDescriptor, ((NumberLiteral) expression).getValue());
    }
    // Otherwise, insert an explicit cast.
    return CastExpression.newBuilder()
        .setExpression(expression)
        .setCastTypeDescriptor(toTypeDescriptor)
        .build();
  }
}
