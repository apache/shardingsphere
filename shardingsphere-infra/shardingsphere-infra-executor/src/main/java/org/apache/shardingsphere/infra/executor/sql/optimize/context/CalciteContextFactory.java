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

package org.apache.shardingsphere.infra.executor.sql.optimize.context;

import org.apache.shardingsphere.infra.executor.sql.optimize.schema.CalciteLogicSchema;
import org.apache.shardingsphere.infra.executor.sql.optimize.schema.row.CalciteRowExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContext;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;

import java.util.Map;

/**
 * Calcite context factory.
 */
public final class CalciteContextFactory {
    
    private final OptimizeContextFactory optimizeContextFactory;
    
    public CalciteContextFactory(final Map<String, ShardingSphereMetaData> metaDataMap) {
        optimizeContextFactory = new OptimizeContextFactory(metaDataMap);
    }
    
    /**
     * Create.
     *
     * @param schema schema
     * @param executor executor
     * @return calcite context
     */
    public OptimizeContext create(final String schema, final CalciteRowExecutor executor) {
        CalciteLogicSchema calciteLogicSchema = new CalciteLogicSchema(optimizeContextFactory.getSchemaMetadatas().getSchemas().get(schema), executor);
        return optimizeContextFactory.create(schema, calciteLogicSchema);
    }
}
