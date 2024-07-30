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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor;

import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Projection identifier extract engine.
 */
public final class ProjectionIdentifierExtractEngine {
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<DialectProjectionIdentifierExtractor> projectionIdentifierExtractor;
    
    public ProjectionIdentifierExtractEngine(final DatabaseType databaseType) {
        projectionIdentifierExtractor = DatabaseTypedSPILoader.findService(DialectProjectionIdentifierExtractor.class, databaseType);
    }
    
    /**
     * Get identifier value.
     *
     * @param identifierValue identifier value
     * @return identifier value
     */
    public String getIdentifierValue(final IdentifierValue identifierValue) {
        if (QuoteCharacter.NONE != identifierValue.getQuoteCharacter()) {
            return identifierValue.getValue();
        }
        return projectionIdentifierExtractor.map(optional -> optional.getIdentifierValue(identifierValue)).orElseGet(identifierValue::getValue);
    }
    
    /**
     * Get column name from function.
     *
     * @param functionName function name
     * @param functionExpression function expression
     * @return column name
     */
    public String getColumnNameFromFunction(final String functionName, final String functionExpression) {
        return projectionIdentifierExtractor.map(optional -> optional.getColumnNameFromFunction(functionName, functionExpression)).orElse(functionExpression);
    }
    
    /**
     * Get column name from expression.
     *
     * @param expression expression
     * @return column name
     */
    public String getColumnNameFromExpression(final String expression) {
        return projectionIdentifierExtractor.map(optional -> optional.getColumnNameFromExpression(expression)).orElse(expression);
    }
    
    /**
     * Get column name from subquery segment.
     *
     * @param subquerySegment subquery segment
     * @return column name
     */
    public String getColumnNameFromSubquery(final SubqueryProjectionSegment subquerySegment) {
        return projectionIdentifierExtractor.map(optional -> optional.getColumnNameFromSubquery(subquerySegment)).orElseGet(subquerySegment::getText);
    }
}
