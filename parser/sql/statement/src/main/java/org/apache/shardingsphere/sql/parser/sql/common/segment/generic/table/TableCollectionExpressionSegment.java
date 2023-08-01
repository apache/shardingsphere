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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Table Collection Expression segment.
 */
@RequiredArgsConstructor
@Getter
public final class TableCollectionExpressionSegment implements TableSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    @Setter
    private FunctionSegment functionSegment;
    
    @Setter
    private ExpressionSegment expressionSegment;
    
    @Setter
    private OwnerSegment owner;
    
    @Override
    public Optional<String> getAliasName() {
        return Optional.empty();
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.empty();
    }
    
    @Override
    public void setAlias(final AliasSegment alias) {
        
    }
}
