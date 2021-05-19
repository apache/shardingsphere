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

package org.apache.shardingsphere.infra.executor.sql.optimize.schema;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.optimize.schema.row.CalciteRowExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.schema.LogicSchemaMetadatas;

import java.util.Map;

/**
 * Calcite logic schema factory.
 */
public final class CalciteLogicSchemaFactory {
    
    private final LogicSchemaMetadatas metadatas;
    
    public CalciteLogicSchemaFactory(final Map<String, ShardingSphereMetaData> metaDataMap) {
        metadatas = new LogicSchemaMetadatas(metaDataMap);
    }
    
    /**
     * Create schema.
     *
     * @param name name
     * @param executor executor
     * @return schema
     */
    public CalciteLogicSchema create(final String name, final CalciteRowExecutor executor) {
        if (!metadatas.getSchemas().containsKey(name)) {
            throw new ShardingSphereException("No `%s` schema.", name);
        }
        return new CalciteLogicSchema(metadatas.getSchemas().get(name), executor);
    }
}
