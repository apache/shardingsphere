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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * XA data source definition factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XADataSourceDefinitionFactory {
    
    private static final Map<DatabaseType, XADataSourceDefinition> XA_DATA_SOURCE_DEFINITIONS = new HashMap<>();
    
    static {
        for (XADataSourceDefinition each : ServiceLoader.load(XADataSourceDefinition.class)) {
            XA_DATA_SOURCE_DEFINITIONS.put(DatabaseTypes.getActualDatabaseType(each.getDatabaseType()), each);
        }
    }
    
    /**
     * Get XA data source definition.
     * 
     * @param databaseType database type
     * @return XA data source definition
     */
    public static XADataSourceDefinition getXADataSourceDefinition(final DatabaseType databaseType) {
        return XA_DATA_SOURCE_DEFINITIONS.get(databaseType);
    }
}
