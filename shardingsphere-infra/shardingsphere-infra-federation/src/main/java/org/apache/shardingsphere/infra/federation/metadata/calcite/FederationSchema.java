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

package org.apache.shardingsphere.infra.federation.metadata.calcite;

import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.federation.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.federation.metadata.FederationTableMetaData;

import java.util.Map;

/**
 * Federation schema.
 */
@Getter
public final class FederationSchema extends AbstractSchema {
    
    private final Map<String, Table> tableMap;
    
    public FederationSchema(final FederationSchemaMetaData metaData) {
        tableMap = getTableMap(metaData);
    }
    
    private Map<String, Table> getTableMap(final FederationSchemaMetaData metaData) {
        Map<String, Table> result = new LinkedMap<>(metaData.getTables().size(), 1);
        for (FederationTableMetaData each : metaData.getTables().values()) {
            result.put(each.getName(), new FederationTable(each));
        }
        return result;
    }
}
