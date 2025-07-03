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

package org.apache.shardingsphere.sql.parser.statement.core.segment.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Pivot segment.
 */
@RequiredArgsConstructor
@Getter
public final class PivotSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final Collection<ColumnSegment> pivotForColumns;
    
    private final Collection<ColumnSegment> pivotInColumns;
    
    private final boolean isUnPivot;
    
    @Setter
    private Collection<ColumnSegment> unpivotColumns;
    
    public PivotSegment(final int startIndex, final int stopIndex, final Collection<ColumnSegment> pivotForColumns, final Collection<ColumnSegment> pivotInColumns) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.pivotForColumns = pivotForColumns;
        this.pivotInColumns = pivotInColumns;
        isUnPivot = false;
    }
    
    /**
     * Get pivot column names.
     *
     * @return pivot column names
     */
    public Collection<String> getPivotColumnNames() {
        Collection<ColumnSegment> result = new HashSet<>(pivotInColumns);
        result.addAll(pivotForColumns);
        if (null != unpivotColumns) {
            result.addAll(unpivotColumns);
        }
        return result.stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
}
