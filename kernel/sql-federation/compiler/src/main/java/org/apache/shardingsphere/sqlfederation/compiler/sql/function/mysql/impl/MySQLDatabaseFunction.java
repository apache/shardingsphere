/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.CallImplementor;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.FunctionParameter;
import org.apache.calcite.schema.ImplementableFunction;
import org.apache.calcite.schema.ScalarFunction;

import java.util.Collections;
import java.util.List;

/**
 * MySQL database function.
 */
@RequiredArgsConstructor
public final class MySQLDatabaseFunction implements ScalarFunction, ImplementableFunction {
    
    private final String databaseName;
    
    @Override
    public RelDataType getReturnType(final RelDataTypeFactory typeFactory) {
        return typeFactory.createJavaType(String.class);
    }
    
    @Override
    public List<FunctionParameter> getParameters() {
        return Collections.emptyList();
    }
    
    @Override
    public CallImplementor getImplementor() {
        return (translator, call, nullAs) -> nullAs.handle(Expressions.constant(databaseName));
    }
}
