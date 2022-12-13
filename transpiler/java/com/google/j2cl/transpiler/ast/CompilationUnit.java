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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.j2cl.common.OutputUtils;
import com.google.j2cl.common.visitor.Context;
import com.google.j2cl.common.visitor.Processor;
import com.google.j2cl.common.visitor.Visitable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A model class that represents a Java Compilation Unit.
 */
@Visitable
@Context
public class CompilationUnit extends Node {
  private final String filePath;
  private final String packageName;
  private final String packageRelativePath;
  private boolean flattened = false;

  @Visitable List<Type> types = new ArrayList<>();

  public CompilationUnit(String filePath, String packageName) {
    this.filePath = checkNotNull(filePath);
    this.packageName = checkNotNull(packageName);
    this.packageRelativePath =
        OutputUtils.getPackageRelativePath(packageName, new File(filePath).getName());
  }

  public String getFilePath() {
    return filePath;
  }

  public String getDirectoryPath() {
    if (!filePath.contains(File.separator)) {
      return "";
    }
    return filePath.substring(0, filePath.lastIndexOf(File.separator));
  }

  public String getPackageRelativePath() {
    return packageRelativePath;
  }

  public String getPackageName() {
    return packageName;
  }

  public void addType(Type type) {
    types.add(checkNotNull(type));
  }

  public void addType(int position, Type type) {
    types.add(position, checkNotNull(type));
  }

  public void addTypes(Iterable<Type> types) {
    types.forEach(this::addType);
  }

  public List<Type> getTypes() {
    return types;
  }

  public Stream<Type> streamTypes() {
    if (flattened) {
      return getTypes().stream();
    }
    List<Type> allTypes = new ArrayList<>();
    accept(
        new AbstractVisitor() {
          @Override
          public void exitType(Type type) {
            allTypes.add(type);
          }
        });
    return allTypes.stream();
  }

  public boolean isFlattened() {
    return flattened;
  }

  public void setFlattened() {
    flattened = true;
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_CompilationUnit.visit(processor, this);
  }
}
