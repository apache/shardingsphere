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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Subquery segment.
 */
@RequiredArgsConstructor
@Getter
public final class SubquerySegment implements ExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    @Setter
    private SelectStatement select;
    
    @Setter
    private MergeStatement merge;
    
    private final String text;
    
    public SubquerySegment(final int startIndex, final int stopIndex, final SelectStatement select, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.select = select;
        this.text = text;
    }
}
