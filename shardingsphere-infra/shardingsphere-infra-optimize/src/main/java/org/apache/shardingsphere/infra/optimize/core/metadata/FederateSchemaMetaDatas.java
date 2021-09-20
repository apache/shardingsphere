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

package org.apache.shardingsphere.infra.optimize.core.metadata;

import lombok.Getter;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Federate schema meta datas.
 */
@Getter
public final class FederateSchemaMetaDatas {
    
    private final Map<String, FederateSchemaMetaData> schemas = new LinkedMap<>();
    
    public FederateSchemaMetaDatas(final Map<String, ShardingSphereMetaData> metaDataMap) {
        for (Entry<String, ShardingSphereMetaData> each : metaDataMap.entrySet()) {
            schemas.put(each.getKey(), new FederateSchemaMetaData(each.getKey(), each.getValue().getSchema().getTables()));
        }
    }
    
    /**
     * Get schema meta data by schema name.
     * 
     * @param schemaName schema name
     * @return schema meta data
     */
    public FederateSchemaMetaData getSchemaMetaDataBySchemaName(final String schemaName) {
        return schemas.get(schemaName);
    }
}
