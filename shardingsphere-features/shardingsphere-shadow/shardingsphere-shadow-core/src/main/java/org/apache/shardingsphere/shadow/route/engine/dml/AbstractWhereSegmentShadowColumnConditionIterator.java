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
import org.apache.shardingsphere.shadow.route.engine.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Iterator;
import java.util.Optional;

/**
 * Abstract shadow column condition iterator of where segment.
 */
@RequiredArgsConstructor
@Getter(value = AccessLevel.PROTECTED)
public abstract class AbstractWhereSegmentShadowColumnConditionIterator implements Iterator<Optional<ShadowColumnCondition>> {
    
    private final String shadowColumn;
    
    private final Iterator<ExpressionSegment> iterator;
    
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    @Override
    public Optional<ShadowColumnCondition> next() {
        ExpressionSegment expressionSegment = iterator.next();
        Optional<ColumnSegment> columnSegment = ShadowExtractor.extractColumn(expressionSegment);
        if (!columnSegment.isPresent()) {
            return Optional.empty();
        }
        String column = columnSegment.get().getIdentifier().getValue();
        if (!shadowColumn.equals(column)) {
            return Optional.empty();
        }
        return nextShadowColumnCondition(expressionSegment, columnSegment.get());
    }
    
    /**
     * Next shadow column condition.
     *
     * @param expressionSegment expression segment
     * @param columnSegment column segment
     * @return shadow column condition
     */
    protected abstract Optional<ShadowColumnCondition> nextShadowColumnCondition(ExpressionSegment expressionSegment, ColumnSegment columnSegment);
}
