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
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Expression projection segment.
 */
@Getter
public final class ExpressionProjectionSegment implements ProjectionSegment, ComplexExpressionSegment, AliasAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String text;
    
    private final ExpressionSegment expr;
    
    @Setter
    private AliasSegment alias;
    
    public ExpressionProjectionSegment(final int startIndex, final int stopIndex, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = SQLUtils.getExpressionWithoutOutsideParentheses(text);
        expr = null;
    }
    
    public ExpressionProjectionSegment(final int startIndex, final int stopIndex, final String text, final ExpressionSegment expr) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = SQLUtils.getExpressionWithoutOutsideParentheses(text);
        this.expr = expr;
    }
    
    @Override
    public String getColumnLabel() {
        return getAliasName().orElse(text);
    }
    
    @Override
    public Optional<String> getAliasName() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias).map(AliasSegment::getIdentifier);
    }
    
    /**
     * Get alias segment.
     *
     * @return alias segment
     */
    @Override
    public Optional<AliasSegment> getAliasSegment() {
        return Optional.ofNullable(alias);
    }
    
    @Override
    public int getStartIndex() {
        return null != alias && alias.getStartIndex() < startIndex ? alias.getStartIndex() : startIndex;
    }
    
    @Override
    public int getStopIndex() {
        return null != alias && alias.getStopIndex() > stopIndex ? alias.getStopIndex() : stopIndex;
    }
}
