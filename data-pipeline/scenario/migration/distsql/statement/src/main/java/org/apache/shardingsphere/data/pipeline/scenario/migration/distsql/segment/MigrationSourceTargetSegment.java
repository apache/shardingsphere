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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Migration source target segment.
 */
@RequiredArgsConstructor
@Getter
public final class MigrationSourceTargetSegment implements DistSQLSegment {
    
    private final IdentifierValue sourceDatabaseIdentifier;
    
    private final IdentifierValue sourceSchemaIdentifier;
    
    private final IdentifierValue sourceTableIdentifier;
    
    private final IdentifierValue targetTableIdentifier;
    
    /**
     * Construct a migration source target segment from textual identifiers.
     *
     * @param sourceDatabaseName source database name
     * @param sourceSchemaName source schema name
     * @param sourceTableName source table name
     * @param targetTableName target table name
     */
    public MigrationSourceTargetSegment(final String sourceDatabaseName, final String sourceSchemaName, final String sourceTableName, final String targetTableName) {
        this(new IdentifierValue(sourceDatabaseName), null == sourceSchemaName ? null : new IdentifierValue(sourceSchemaName),
                new IdentifierValue(sourceTableName), new IdentifierValue(targetTableName));
    }
    
    /**
     * Get source database name.
     *
     * @return source database name
     */
    public String getSourceDatabaseName() {
        return sourceDatabaseIdentifier.getValue();
    }
    
    /**
     * Get source schema name.
     *
     * @return source schema name
     */
    public String getSourceSchemaName() {
        return null == sourceSchemaIdentifier ? null : sourceSchemaIdentifier.getValue();
    }
    
    /**
     * Get source table name.
     *
     * @return source table name
     */
    public String getSourceTableName() {
        return sourceTableIdentifier.getValue();
    }
    
    /**
     * Get target table name.
     *
     * @return target table name
     */
    public String getTargetTableName() {
        return targetTableIdentifier.getValue();
    }
}
