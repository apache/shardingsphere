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

package org.apache.shardingsphere.infra.binder.segment.select.projection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aggregation projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AggregationProjection implements Projection {
    
    private final AggregationType type;
    
    private final String innerExpression;
    
    private final IdentifierValue alias;
    
    private final DatabaseType databaseType;
    
    private final List<AggregationProjection> derivedAggregationProjections = new ArrayList<>(2);
    
    @Setter
    private int index = -1;
    
    @Override
    public final String getColumnName() {
        return type.name() + innerExpression;
    }
    
    @Override
    public String getColumnLabel() {
        return getAlias().map(IdentifierValue::getValue).orElseGet(() -> databaseType.getDefaultSchema().isPresent() ? type.name().toLowerCase() : getColumnName());
    }
    
    @Override
    public final Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
    
    @Override
    public Projection transformSubqueryProjection(final IdentifierValue subqueryTableAlias, final IdentifierValue originalOwner, final IdentifierValue originalName) {
        if (getAlias().isPresent()) {
            return new ColumnProjection(subqueryTableAlias, getAlias().get(), null);
        }
        AggregationProjection result = new AggregationProjection(type, innerExpression, alias, databaseType);
        result.setIndex(index);
        result.getDerivedAggregationProjections().addAll(derivedAggregationProjections);
        return result;
    }
}
