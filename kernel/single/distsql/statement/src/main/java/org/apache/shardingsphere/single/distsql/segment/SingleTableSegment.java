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

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;

import java.util.Optional;

/**
 * Single table segment.
 */
@RequiredArgsConstructor
@Getter
public final class SingleTableSegment implements DistSQLSegment {
    
    private final String storageUnitName;
    
    private final String schemaName;
    
    private final String tableName;
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        return Optional.ofNullable(schemaName);
    }
    
    @Override
    public String toString() {
        return null == schemaName ? storageUnitName + "." + tableName : storageUnitName + "." + schemaName + "." + tableName;
    }
    
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object || getClass() != object.getClass()) {
            return false;
        }
        SingleTableSegment segment = (SingleTableSegment) object;
        return Objects.equal(storageUnitName.toUpperCase(), segment.storageUnitName.toUpperCase())
                && Objects.equal(tableName.toUpperCase(), segment.tableName.toUpperCase())
                && Objects.equal(null == schemaName ? null : schemaName.toUpperCase(), null == segment.schemaName ? null : segment.schemaName.toUpperCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(storageUnitName.toUpperCase(), tableName.toUpperCase(), null == schemaName ? null : schemaName.toUpperCase());
    }
}
