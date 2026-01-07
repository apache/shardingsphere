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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Aggregation projection segment.
 */
@RequiredArgsConstructor
@Getter
public class AggregationProjectionSegment implements ProjectionSegment, AliasAvailable, ExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final AggregationType type;
    
    private final String expression;
    
    private final String separator;
    
    private final Collection<ExpressionSegment> parameters = new LinkedList<>();
    
    @Setter
    private AliasSegment alias;
    
    public AggregationProjectionSegment(final int startIndex, final int stopIndex, final AggregationType type, final String expression) {
        this(startIndex, stopIndex, type, expression, null);
    }
    
    @Override
    public String getColumnLabel() {
        return getAliasName().orElse(expression);
    }
    
    @Override
    public final Optional<String> getAliasName() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
    
    @Override
    public final Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias).map(AliasSegment::getIdentifier);
    }
    
    /**
     * Get alias segment.
     *
     * @return alias segment
     */
    public Optional<AliasSegment> getAliasSegment() {
        return Optional.ofNullable(alias);
    }
    
    @Override
    public String getText() {
        return expression;
    }
    
    public Optional<String> getSeparator() {
        return Optional.ofNullable(separator);
    }
}
