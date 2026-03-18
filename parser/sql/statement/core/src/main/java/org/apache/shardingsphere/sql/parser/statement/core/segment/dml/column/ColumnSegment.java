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
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Column segment.
 */
@Getter
@Setter
public final class ColumnSegment implements ExpressionSegment, OwnerAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private List<IdentifierValue> nestedObjectAttributes;
    
    private OwnerSegment owner;
    
    private ColumnSegmentBoundInfo columnBoundInfo;
    
    private ColumnSegmentBoundInfo otherUsingColumnBoundInfo;
    
    private boolean isVariable;
    
    private ParenthesesSegment leftParentheses;
    
    private ParenthesesSegment rightParentheses;
    
    public ColumnSegment(final int startIndex, final int stopIndex, final IdentifierValue identifier) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        columnBoundInfo = new ColumnSegmentBoundInfo(identifier);
    }
    
    /**
     * Get qualified name with quote characters.
     * i.e. `field1`, `table1`, field1, table1, `table1`.`field1`, `table1`.field1, table1.`field1` or table1.field1
     *
     * @return qualified name with quote characters
     */
    public String getQualifiedName() {
        String column = identifier.getValueWithQuoteCharacters();
        if (null != nestedObjectAttributes && !nestedObjectAttributes.isEmpty()) {
            column = String.join(".", column, nestedObjectAttributes.stream().map(IdentifierValue::getValueWithQuoteCharacters).collect(Collectors.joining(".")));
        }
        return null == owner ? column : String.join(".", owner.getIdentifier().getValueWithQuoteCharacters(), column);
    }
    
    /**
     * Get expression.
     *
     * @return expression
     */
    public String getExpression() {
        String column = identifier.getValue();
        if (null != nestedObjectAttributes && !nestedObjectAttributes.isEmpty()) {
            column = String.join(".", column, nestedObjectAttributes.stream().map(IdentifierValue::getValue).collect(Collectors.joining(".")));
        }
        return null == owner ? column : String.join(".", owner.getIdentifier().getValue(), column);
    }
    
    /**
     * Get left parentheses.
     *
     * @return left parentheses
     */
    public Optional<ParenthesesSegment> getLeftParentheses() {
        return Optional.ofNullable(leftParentheses);
    }
    
    /**
     * Get right parentheses.
     *
     * @return right parentheses
     */
    public Optional<ParenthesesSegment> getRightParentheses() {
        return Optional.ofNullable(rightParentheses);
    }
    
    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    @Override
    public String getText() {
        return getExpression();
    }
}
