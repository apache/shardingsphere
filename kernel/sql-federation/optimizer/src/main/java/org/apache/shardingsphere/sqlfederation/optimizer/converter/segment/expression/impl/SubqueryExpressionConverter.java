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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.select.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;

import java.util.Optional;

/**
 * Subquery expression converter.
 */
public final class SubqueryExpressionConverter implements SQLSegmentConverter<SubqueryExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final SubqueryExpressionSegment expression) {
        return null == expression ? Optional.empty() : Optional.of(new SelectStatementConverter().convert(expression.getSubquery().getSelect()));
    }
}
