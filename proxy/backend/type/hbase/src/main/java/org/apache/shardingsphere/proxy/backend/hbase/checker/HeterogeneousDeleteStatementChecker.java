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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;

import java.util.Optional;

/**
 * Delete statement checker.
 */
public final class HeterogeneousDeleteStatementChecker extends CommonHeterogeneousSQLStatementChecker {
    
    private final DeleteStatement sqlStatement;
    
    public HeterogeneousDeleteStatementChecker(final DeleteStatement sqlStatement) {
        super(sqlStatement);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    public void execute() {
        Optional<WhereSegment> whereSegment = sqlStatement.getWhere();
        Preconditions.checkArgument(whereSegment.isPresent(), "Must contain where segment.");
        if (whereSegment.get().getExpr() instanceof InExpression) {
            checkInExpressionIsExpected(whereSegment.get().getExpr());
        } else {
            checkIsSinglePointQuery(whereSegment.get());
        }
    }
}
