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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Abstract shadow column condition iterator of where segment.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractWhereSegmentShadowColumnConditionIterator implements Iterator<Optional<ShadowColumnCondition>> {
    
    @Getter(AccessLevel.PROTECTED)
    private final String shadowColumnName;
    
    private final Iterator<ExpressionSegment> expressions;
    
    @Override
    public boolean hasNext() {
        return expressions.hasNext();
    }
    
    @Override
    public Optional<ShadowColumnCondition> next() {
        ExpressionSegment expression = expressions.next();
        Collection<ColumnSegment> columns = ColumnExtractor.extract(expression);
        if (1 != columns.size()) {
            return Optional.empty();
        }
        ColumnSegment column = columns.iterator().next();
        return shadowColumnName.equals(column.getIdentifier().getValue()) ? next(expression, column) : Optional.empty();
    }
    
    protected abstract Optional<ShadowColumnCondition> next(ExpressionSegment expression, ColumnSegment column);
}
