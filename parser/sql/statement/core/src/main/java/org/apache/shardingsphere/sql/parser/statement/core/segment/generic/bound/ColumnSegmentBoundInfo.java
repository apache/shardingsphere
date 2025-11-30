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

package org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Column segment bound info.
 */
@Getter
public final class ColumnSegmentBoundInfo {
    
    private final TableSegmentBoundInfo tableBoundInfo;
    
    private final IdentifierValue originalTable;
    
    private final IdentifierValue originalColumn;
    
    private final TableSourceType tableSourceType;
    
    public ColumnSegmentBoundInfo(final IdentifierValue originalColumn) {
        this(null, null, originalColumn, TableSourceType.PHYSICAL_TABLE);
    }
    
    public ColumnSegmentBoundInfo(final TableSegmentBoundInfo tableBoundInfo, final IdentifierValue originalTable, final IdentifierValue originalColumn, final TableSourceType tableSourceType) {
        this.tableBoundInfo = null == tableBoundInfo ? new TableSegmentBoundInfo(null, null) : tableBoundInfo;
        this.originalTable = null == originalTable ? new IdentifierValue("") : originalTable;
        this.originalColumn = null == originalColumn ? new IdentifierValue("") : originalColumn;
        this.tableSourceType = tableSourceType;
    }
    
    /**
     * Get original database.
     *
     * @return original database
     */
    public IdentifierValue getOriginalDatabase() {
        return tableBoundInfo.getOriginalDatabase();
    }
    
    /**
     * Get original schema.
     *
     * @return original schema
     */
    public IdentifierValue getOriginalSchema() {
        return tableBoundInfo.getOriginalSchema();
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (null != getOriginalDatabase()) {
            result.append(getOriginalDatabase().getValue()).append(".");
        }
        if (null != getOriginalSchema()) {
            result.append(getOriginalSchema().getValue()).append(".");
        }
        if (null != originalTable) {
            result.append(originalTable.getValue()).append(".");
        }
        if (null != originalColumn) {
            result.append(originalColumn.getValue());
        }
        return result.toString();
    }
}
