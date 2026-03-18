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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;

import java.util.Optional;

/**
 * Single table segment.
 */
@RequiredArgsConstructor
public final class SingleTableSegment implements DistSQLSegment {
    
    @Getter
    private final String storageUnitName;
    
    private final String schemaName;
    
    @Getter
    private final String tableName;
    
    public SingleTableSegment(final String storageUnitName, final String tableName) {
        this(storageUnitName, null, tableName);
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        return Optional.ofNullable(schemaName);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SingleTableSegment)) {
            return false;
        }
        if (null == schemaName) {
            return storageUnitName.equalsIgnoreCase(((SingleTableSegment) obj).storageUnitName) && tableName.equalsIgnoreCase(((SingleTableSegment) obj).tableName)
                    && null == ((SingleTableSegment) obj).schemaName;
        }
        return storageUnitName.equalsIgnoreCase(((SingleTableSegment) obj).storageUnitName)
                && schemaName.equalsIgnoreCase(((SingleTableSegment) obj).schemaName) && tableName.equalsIgnoreCase(((SingleTableSegment) obj).tableName);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(storageUnitName).append(schemaName).append(tableName).toHashCode();
    }
    
    @Override
    public String toString() {
        return null == schemaName ? String.join(".", storageUnitName, tableName) : String.join(".", storageUnitName, schemaName, tableName);
    }
}
