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

package org.apache.shardingsphere.sqlfederation.executor.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;

import java.util.Map;

/**
 * Executor bind context.
 */
@RequiredArgsConstructor
public final class ExecutorBindContext implements DataContext {
    
    private final SQLFederationRelConverter converter;
    
    @Getter
    private final Map<String, Object> parameters;
    
    @Override
    public SchemaPlus getRootSchema() {
        return converter.getSchemaPlus();
    }
    
    @Override
    public JavaTypeFactory getTypeFactory() {
        return (JavaTypeFactory) converter.getCluster().getTypeFactory();
    }
    
    @Override
    public QueryProvider getQueryProvider() {
        return null;
    }
    
    @Override
    public Object get(final String name) {
        return parameters.get(name);
    }
}
