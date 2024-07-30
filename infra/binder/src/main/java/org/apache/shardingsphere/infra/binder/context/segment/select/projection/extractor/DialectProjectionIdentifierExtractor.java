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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.type.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Dialect projection identifier extractor.
 */
@RequiredArgsConstructor
public final class DialectProjectionIdentifierExtractor {
    
    private final DatabaseType databaseType;
    
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
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            return identifierValue.getValue().toLowerCase();
        }
        if (databaseType instanceof OracleDatabaseType) {
            return identifierValue.getValue().toUpperCase();
        }
        return identifierValue.getValue();
    }
    
    /**
     * Get column name from function.
     *
     * @param functionName function name
     * @param functionExpression function expression
     * @return column name
     */
    public String getColumnNameFromFunction(final String functionName, final String functionExpression) {
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            return functionName.toLowerCase();
        }
        if (databaseType instanceof OracleDatabaseType) {
            return functionExpression.replace(" ", "").toUpperCase();
        }
        return functionExpression;
    }
    
    /**
     * Get column name from expression.
     *
     * @param expression expression
     * @return column name
     */
    public String getColumnNameFromExpression(final String expression) {
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            return "?column?";
        }
        if (databaseType instanceof OracleDatabaseType) {
            return expression.replace(" ", "").toUpperCase();
        }
        return expression;
    }
    
    /**
     * Get column name from subquery segment.
     *
     * @param subquerySegment subquery segment
     * @return column name
     */
    public String getColumnNameFromSubquery(final SubqueryProjectionSegment subquerySegment) {
        // TODO support postgresql subquery projection
        if (databaseType instanceof OracleDatabaseType) {
            return subquerySegment.getText().replace(" ", "").toUpperCase();
        }
        return subquerySegment.getText();
    }
}
