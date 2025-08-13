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

package org.apache.shardingsphere.single.distsql.statement.rql;

import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;

import java.util.Optional;

/**
 * Show unloaded single tables statement.
 */
public final class ShowUnloadedSingleTablesStatement extends ShowTablesStatement {
    
    private final String storageUnitName;
    
    private final String schemaName;
    
    public ShowUnloadedSingleTablesStatement(final FromDatabaseSegment fromDatabase, final String storageUnitName, final String schemaName) {
        // TODO support like later
        super(fromDatabase, null);
        this.storageUnitName = storageUnitName;
        this.schemaName = schemaName;
    }
    
    /**
     * Get storage unit name.
     *
     * @return storage unit name
     */
    public Optional<String> getStorageUnitName() {
        return Optional.ofNullable(storageUnitName);
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        return Optional.ofNullable(schemaName);
    }
}
