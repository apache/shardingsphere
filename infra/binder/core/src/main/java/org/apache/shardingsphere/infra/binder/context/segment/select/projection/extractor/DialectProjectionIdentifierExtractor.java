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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Dialect projection identifier extractor.
 */
@SingletonSPI
public interface DialectProjectionIdentifierExtractor extends DatabaseTypedSPI {
    
    /**
     * Get identifier value.
     *
     * @param identifierValue identifier value
     * @return identifier value
     */
    String getIdentifierValue(IdentifierValue identifierValue);
    
    /**
     * Get column name from function.
     *
     * @param functionName function name
     * @param functionExpression function expression
     * @return column name
     */
    String getColumnNameFromFunction(String functionName, String functionExpression);
    
    /**
     * Get column name from expression segment.
     *
     * @param expressionSegment expression segment
     * @return column name
     */
    
    String getColumnNameFromExpression(ExpressionSegment expressionSegment);
    
    /**
     * Get column name from subquery segment.
     *
     * @param subquerySegment subquery segment
     * @return column name
     */
    String getColumnNameFromSubquery(SubqueryProjectionSegment subquerySegment);
}
