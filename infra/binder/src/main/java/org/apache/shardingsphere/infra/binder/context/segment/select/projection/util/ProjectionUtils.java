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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.type.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * Projection utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionUtils {
    
    /**
     * Get column label from alias.
     * 
     * @param alias alias
     * @param databaseType database type
     * @return column label
     */
    public static String getColumnLabelFromAlias(final IdentifierValue alias, final DatabaseType databaseType) {
        return getIdentifierValueByDatabaseType(alias, databaseType);
    }
    
    private static String getIdentifierValueByDatabaseType(final IdentifierValue identifierValue, final DatabaseType databaseType) {
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
     * Get column name from column.
     *
     * @param columnName column name
     * @param databaseType database type
     * @return column name
     */
    public static String getColumnNameFromColumn(final IdentifierValue columnName, final DatabaseType databaseType) {
        return getIdentifierValueByDatabaseType(columnName, databaseType);
    }
    
    /**
     * Get column name from function.
     * 
     * @param functionName function name
     * @param functionExpression function expression
     * @param databaseType database type
     * @return column name
     */
    public static String getColumnNameFromFunction(final String functionName, final String functionExpression, final DatabaseType databaseType) {
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
     * @param databaseType database type
     * @return column name
     */
    public static String getColumnNameFromExpression(final String expression, final DatabaseType databaseType) {
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
     * @param databaseType database type
     * @return column name
     */
    public static String getColumnNameFromSubquery(final SubqueryProjectionSegment subquerySegment, final DatabaseType databaseType) {
        // TODO support postgresql subquery projection
        if (databaseType instanceof OracleDatabaseType) {
            return subquerySegment.getText().replace(" ", "").toUpperCase();
        }
        return subquerySegment.getText();
    }
}
