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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.LinkedList;
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
    
    private ColumnSegmentBoundedInfo columnBoundedInfo;
    
    private ColumnSegmentBoundedInfo otherUsingColumnBoundedInfo;
    
    private boolean isVariable;
    
    private List<ParenthesesSegment> parentheses = new LinkedList<>();
    
    public ColumnSegment(final int startIndex, final int stopIndex, final IdentifierValue identifier) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        columnBoundedInfo = new ColumnSegmentBoundedInfo(identifier);
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
    
    @Override
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    @Override
    public String getText() {
        return getExpression();
    }
}
