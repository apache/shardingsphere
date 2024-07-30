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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.dialect;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.DialectProjectionIdentifierExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Projection identifier extractor or Oracle.
 */
public final class OracleProjectionIdentifierExtractor implements DialectProjectionIdentifierExtractor {
    
    @Override
    public String getIdentifierValue(final IdentifierValue identifierValue) {
        return identifierValue.getValue().toUpperCase();
    }
    
    @Override
    public String getColumnNameFromFunction(final String functionName, final String functionExpression) {
        return functionExpression.replace(" ", "").toUpperCase();
    }
    
    @Override
    public String getColumnNameFromExpression(final String expression) {
        return expression.replace(" ", "").toUpperCase();
    }
    
    @Override
    public String getColumnNameFromSubquery(final SubqueryProjectionSegment subquerySegment) {
        return subquerySegment.getText().replace(" ", "").toUpperCase();
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
