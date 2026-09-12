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

package org.apache.shardingsphere.linkedserver.rule.table;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.linkedserver.config.rule.LinkedServerConfiguration;

import java.util.Map;
import java.util.Optional;

/**
 * Linked server table, runtime representation of a single linked server.
 */
@Getter
public final class LinkedServerTable {
    
    private final String name;
    
    private final DatabaseType databaseType;
    
    private final Map<String, String> tables;
    
    public LinkedServerTable(final LinkedServerConfiguration config) {
        name = config.getName();
        databaseType = TypedSPILoader.getService(DatabaseType.class, config.getDatabaseType());
        tables = new CaseInsensitiveMap<>();
        tables.putAll(config.getTables());
    }
    
    /**
     * Find logical table name by remote table name.
     *
     * @param remoteTableName remote table name
     * @return logical table name
     */
    public Optional<String> findLogicalTable(final String remoteTableName) {
        return Optional.ofNullable(tables.get(remoteTableName));
    }
}
