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

package org.apache.shardingsphere.single.distsql.segment;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;

/**
 * Single table segment.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public final class SingleTableSegment implements DistSQLSegment {
    
    private final CaseInsensitiveIdentifier storageUnitName;
    
    private final CaseInsensitiveIdentifier schemaName;
    
    private final CaseInsensitiveIdentifier tableName;
    
    public SingleTableSegment(final String storageUnitName, final String tableName) {
        this(storageUnitName, null, tableName);
    }
    
    public SingleTableSegment(final String storageUnitName, final String schemaName, final String tableName) {
        this.storageUnitName = new CaseInsensitiveIdentifier(storageUnitName);
        this.schemaName = null == schemaName ? null : new CaseInsensitiveIdentifier(schemaName);
        this.tableName = new CaseInsensitiveIdentifier(tableName);
    }
    
    /**
     * Get storage unit name.
     *
     * @return storage unit name
     */
    public String getStorageUnitName() {
        return storageUnitName.toString();
    }
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getTableName() {
        return tableName.toString();
    }
    
    /**
     * Whether to contain schema.
     *
     * @return contains schema or not
     */
    public boolean containsSchema() {
        return null != schemaName;
    }
    
    @Override
    public String toString() {
        return null == schemaName ? String.join(".", getStorageUnitName(), getTableName()) : String.join(".", getStorageUnitName(), schemaName.toString(), getTableName());
    }
}
