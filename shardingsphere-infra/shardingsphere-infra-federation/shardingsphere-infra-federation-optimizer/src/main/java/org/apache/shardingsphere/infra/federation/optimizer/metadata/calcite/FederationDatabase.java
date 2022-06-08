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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.calcite;

import lombok.Getter;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Federation database.
 */
@Getter
public final class FederationDatabase extends AbstractSchema {
    
    private final Map<String, Schema> subSchemaMap;
    
    public FederationDatabase(final FederationDatabaseMetaData metaData) {
        subSchemaMap = getSubSchemaMap(metaData);
    }
    
    private Map<String, Schema> getSubSchemaMap(final FederationDatabaseMetaData metaData) {
        Map<String, Schema> result = new LinkedHashMap<>(metaData.getSchemas().size(), 1);
        for (FederationSchemaMetaData each : metaData.getSchemas().values()) {
            result.put(each.getName(), new FederationSchema(each));
        }
        return result;
    }
}
