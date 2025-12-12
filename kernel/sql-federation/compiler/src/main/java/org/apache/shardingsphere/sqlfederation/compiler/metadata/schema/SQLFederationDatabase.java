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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Map;

/**
 * SQL federation database.
 */
@Getter
public final class SQLFederationDatabase extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Schema> subSchemaMap;
    
    public SQLFederationDatabase(final ShardingSphereDatabase database, final DatabaseType protocolType) {
        name = database.getName();
        subSchemaMap = createSubSchemaMap(database, protocolType);
    }
    
    private Map<String, Schema> createSubSchemaMap(final ShardingSphereDatabase database, final DatabaseType protocolType) {
        Map<String, Schema> result = new CaseInsensitiveMap<>(database.getAllSchemas().size(), 1F);
        for (ShardingSphereSchema each : database.getAllSchemas()) {
            result.put(each.getName(), new SQLFederationSchema(each.getName(), each, protocolType));
        }
        return result;
    }
}
