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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.impl.keygen;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.GeneratedKeyAssignmentToken;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated key assignment token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class GeneratedKeyAssignmentTokenGenerator extends BaseGeneratedKeyTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatement insertStatement) {
        return insertStatement.getSetAssignment().isPresent();
    }
    
    @Override
    protected GeneratedKeyAssignmentToken generateSQLToken(final SQLStatementContext sqlStatementContext, final GeneratedKey generatedKey) {
        Preconditions.checkState(((InsertStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().isPresent());
        List<AssignmentSegment> assignments = new ArrayList<>(((InsertStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().get().getAssignments());
        int index = ((InsertSQLStatementContext) sqlStatementContext).getColumnNames().contains(generatedKey.getColumnName())
                ? ((InsertSQLStatementContext) sqlStatementContext).getColumnNames().indexOf(generatedKey.getColumnName()) : ((InsertSQLStatementContext) sqlStatementContext).getColumnNames().size();
        ExpressionSegment expressionSegment = ((InsertSQLStatementContext) sqlStatementContext).getInsertValueContexts().get(0).getValueExpressions().get(index);
        return new GeneratedKeyAssignmentToken(assignments.get(assignments.size() - 1).getStopIndex() + 1, generatedKey.getColumnName(), expressionSegment);
    }
}
