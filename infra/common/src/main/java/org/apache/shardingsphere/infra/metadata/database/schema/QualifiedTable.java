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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.Objects;

/**
 * Qualified table.
 */
public final class QualifiedTable {
    
    private final ShardingSphereIdentifier schemaName;
    
    private final ShardingSphereIdentifier tableName;
    
    public QualifiedTable(final String schemaName, final String tableName) {
        this.schemaName = new ShardingSphereIdentifier(schemaName);
        this.tableName = new ShardingSphereIdentifier(tableName);
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public String getSchemaName() {
        return schemaName.getValue();
    }
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getTableName() {
        return tableName.getValue();
    }
    
    /**
     * Get qualified table name.
     *
     * @return qualified table name
     */
    public String format() {
        return null == getSchemaName() ? getTableName() : String.join(".", getSchemaName(), getTableName());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        QualifiedTable that = (QualifiedTable) o;
        return Objects.equals(null == schemaName.getValue() ? null : schemaName, null == that.schemaName.getValue() ? null : that.schemaName)
                && Objects.equals(null == tableName.getValue() ? null : tableName, null == that.tableName.getValue() ? null : that.tableName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(schemaName, tableName);
    }
    
    @Override
    public String toString() {
        return format();
    }
}
