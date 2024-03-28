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

package org.apache.shardingsphere.sqlfederation.optimizer.context.planner;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.schema.Schema;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.schema.SQLFederationSchema;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Optimizer meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerMetaDataFactory {
    
    private static final JavaTypeFactory DEFAULT_DATA_TYPE_FACTORY = new JavaTypeFactoryImpl();
    
    /**
     * Create optimizer meta data map.
     *
     * @param databases databases
     * @return created optimizer planner context map
     */
    public static Map<String, OptimizerMetaData> create(final Map<String, ShardingSphereDatabase> databases) {
        Map<String, OptimizerMetaData> result = new CaseInsensitiveMap<>(databases.size(), 1F);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            result.put(entry.getKey(), create(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Create optimizer meta data.
     *
     * @param database database
     * @return created optimizer planner context
     */
    public static OptimizerMetaData create(final ShardingSphereDatabase database) {
        Map<String, Schema> schemas = new CaseInsensitiveMap<>();
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            Schema sqlFederationSchema = new SQLFederationSchema(entry.getKey(), entry.getValue(), database.getProtocolType(), DEFAULT_DATA_TYPE_FACTORY);
            schemas.put(entry.getKey(), sqlFederationSchema);
        }
        return new OptimizerMetaData(schemas);
    }
}
