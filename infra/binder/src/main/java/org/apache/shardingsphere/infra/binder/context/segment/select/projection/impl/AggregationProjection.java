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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.util.ProjectionUtils;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
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
    
    private final String expression;
    
    private final IdentifierValue alias;
    
    private final DatabaseType databaseType;
    
    private final List<AggregationProjection> derivedAggregationProjections = new ArrayList<>(2);
    
    @Setter
    private int index = -1;
    
    @Override
    public String getColumnName() {
        return getColumnLabel();
    }
    
    @Override
    public String getColumnLabel() {
        return getAlias().isPresent() && !DerivedColumn.isDerivedColumnName(getAlias().get().getValueWithQuoteCharacters()) ? ProjectionUtils.getColumnLabelFromAlias(getAlias().get(), databaseType)
                : ProjectionUtils.getColumnNameFromFunction(type.name(), expression, databaseType);
    }
    
    @Override
    public final Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
}
