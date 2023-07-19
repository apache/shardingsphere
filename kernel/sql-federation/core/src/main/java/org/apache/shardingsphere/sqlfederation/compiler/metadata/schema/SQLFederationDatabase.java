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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.schema;

import lombok.Getter;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL federation database.
 */
@Getter
public final class SQLFederationDatabase extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Schema> subSchemaMap;
    
    public SQLFederationDatabase(final ShardingSphereDatabase database, final DatabaseType protocolType, final JavaTypeFactory javaTypeFactory) {
        name = database.getName();
        subSchemaMap = createSubSchemaMap(database, protocolType, javaTypeFactory);
    }
    
    private Map<String, Schema> createSubSchemaMap(final ShardingSphereDatabase database, final DatabaseType protocolType, final JavaTypeFactory javaTypeFactory) {
        Map<String, Schema> result = new LinkedHashMap<>(database.getSchemas().size(), 1F);
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            result.put(entry.getKey(), new SQLFederationSchema(entry.getKey(), entry.getValue(), protocolType, javaTypeFactory));
        }
        return result;
    }
}
