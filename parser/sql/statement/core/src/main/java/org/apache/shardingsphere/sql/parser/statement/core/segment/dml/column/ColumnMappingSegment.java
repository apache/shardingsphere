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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;

import java.util.Optional;

/**
 * Column mapping segment.
 * Represents a column name with an optional mapping expression, e.g., "v3 = k1 * 100".
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ColumnMappingSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final ColumnSegment column;
    
    private ExpressionSegment mappingExpression;
    
    /**
     * Get mapping expression.
     *
     * @return mapping expression
     */
    public Optional<ExpressionSegment> getMappingExpression() {
        return Optional.ofNullable(mappingExpression);
    }
}
