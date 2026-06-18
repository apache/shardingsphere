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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Optional;

/**
 * Alter database statement.
 */
@Getter
@Setter
public final class AlterDatabaseStatement extends DDLStatement {
    
    private String databaseName;
    
    private String renameDatabaseName;
    
    private String quotaType;
    
    private Long quotaValue;
    
    private PropertiesSegment properties;
    
    public AlterDatabaseStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get rename database name.
     *
     * @return rename database name
     */
    public Optional<String> getRenameDatabaseName() {
        return Optional.ofNullable(renameDatabaseName);
    }
    
    /**
     * Get quota type.
     *
     * @return quota type
     */
    public Optional<String> getQuotaType() {
        return Optional.ofNullable(quotaType);
    }
    
    /**
     * Get quota value.
     *
     * @return quota value
     */
    public Optional<Long> getQuotaValue() {
        return Optional.ofNullable(quotaValue);
    }
}
