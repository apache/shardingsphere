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
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Expression projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class ExpressionProjection implements Projection {
    
    private final ExpressionProjectionSegment expressionSegment;
    
    private final IdentifierValue alias;
    
    private final DatabaseType databaseType;
    
    @Override
    public String getColumnName() {
        return getColumnLabel();
    }
    
    @Override
    public String getColumnLabel() {
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(databaseType);
        return getAlias().map(extractEngine::getIdentifierValue).orElseGet(() -> extractEngine.getColumnNameFromExpression(expressionSegment));
    }
    
    @Override
    public String getExpression() {
        return expressionSegment.getText();
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
}
