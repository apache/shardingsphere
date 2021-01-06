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

package org.apache.shardingsphere.infra.optimize.execute.raw.context;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.infra.optimize.context.CalciteContext;

/**
 * Calcite data context.
 */
@RequiredArgsConstructor
public final class CalciteDataContext implements DataContext {
    
    private final CalciteContext context;
    
    @Override
    public SchemaPlus getRootSchema() {
        return context.getValidator().getCatalogReader().getRootSchema().plus();
    }
    
    @Override
    public JavaTypeFactory getTypeFactory() {
        return (JavaTypeFactory) context.getRelConverter().getCluster().getTypeFactory();
    }
    
    @Override
    public QueryProvider getQueryProvider() {
        return null;
    }
    
    @Override
    public Object get(final String name) {
        return null;
    }
}
