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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Common table expression segment.
 */
@AllArgsConstructor
@Getter
public final class CommonTableExpressionSegment implements TableSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private AliasSegment aliasSegment;
    
    private final SubquerySegment subquery;
    
    private final Collection<ColumnSegment> columns = new LinkedList<>();
    
    @Override
    public Optional<String> getAliasName() {
        return getAlias().map(IdentifierValue::getValue);
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.of(aliasSegment).map(AliasSegment::getIdentifier);
    }
    
    @Override
    public Optional<AliasSegment> getAliasSegment() {
        return Optional.ofNullable(aliasSegment);
    }
    
    @Override
    public void setAlias(final AliasSegment alias) {
        aliasSegment = alias;
    }
}
