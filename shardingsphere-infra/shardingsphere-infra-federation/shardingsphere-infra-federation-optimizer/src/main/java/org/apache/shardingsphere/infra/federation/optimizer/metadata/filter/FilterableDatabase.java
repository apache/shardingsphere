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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.filter;

import lombok.Getter;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.federation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Filterable database.
 */
@Getter
public final class FilterableDatabase extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Schema> subSchemaMap;
    
    public FilterableDatabase(final ShardingSphereDatabase database, final TableScanExecutor executor) {
        name = database.getName();
        subSchemaMap = createSubSchemaMap(database, executor);
    }
    
    private Map<String, Schema> createSubSchemaMap(final ShardingSphereDatabase database, final TableScanExecutor executor) {
        Map<String, Schema> result = new LinkedHashMap<>(database.getSchemas().size(), 1);
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            result.put(entry.getKey(), new FilterableSchema(entry.getKey(), entry.getValue(), executor));
        }
        return result;
    }
}
