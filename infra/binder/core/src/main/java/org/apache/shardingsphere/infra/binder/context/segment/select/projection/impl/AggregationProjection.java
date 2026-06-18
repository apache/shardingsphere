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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

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
    
    private final AggregationProjectionSegment aggregationSegment;
    
    private final IdentifierValue alias;
    
    private final DatabaseType databaseType;
    
    private final String separator;
    
    private final List<AggregationProjection> derivedAggregationProjections = new ArrayList<>(2);
    
    @Setter
    private int index = -1;
    
    public AggregationProjection(final AggregationType type, final AggregationProjectionSegment aggregationSegment, final IdentifierValue alias, final DatabaseType databaseType) {
        this.type = type;
        this.aggregationSegment = aggregationSegment;
        this.alias = alias;
        this.databaseType = databaseType;
        separator = null;
    }
    
    /**
     * Get separator.
     *
     * @return separator
     */
    public Optional<String> getSeparator() {
        return Optional.ofNullable(separator);
    }
    
    @Override
    public String getColumnName() {
        return getColumnLabel();
    }
    
    @Override
    public String getColumnLabel() {
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(databaseType);
        return getAlias().isPresent() && !DerivedColumn.isDerivedColumnName(getAlias().get().getValueWithQuoteCharacters())
                ? extractEngine.getIdentifierValue(getAlias().get())
                : extractEngine.getColumnNameFromFunction(type.name(), aggregationSegment.getExpression());
    }
    
    @Override
    public String getExpression() {
        return aggregationSegment.getExpression();
    }
    
    @Override
    public final Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
}
