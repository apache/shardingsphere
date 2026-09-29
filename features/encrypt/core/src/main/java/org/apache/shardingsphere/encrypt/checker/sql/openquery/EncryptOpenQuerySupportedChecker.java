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

package org.apache.shardingsphere.encrypt.checker.sql.openquery;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

/**
 * OPENQUERY supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptOpenQuerySupportedChecker implements SupportedSQLChecker<SQLStatementContext, EncryptRule> {

    private static final String OPENQUERY_FUNCTION_NAME = "OPENQUERY";

    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getFrom().map(this::containsOpenQuery).orElse(false);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return containsOpenQuery(((UpdateStatement) sqlStatement).getTable());
        }
        if (sqlStatement instanceof DeleteStatement) {
            return containsOpenQuery(((DeleteStatement) sqlStatement).getTable());
        }
        return false;
    }

    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SQLStatementContext sqlStatementContext) {
        ShardingSpherePreconditions.checkState(false, () -> new UnsupportedEncryptSQLException("OPENQUERY"));
    }

    private boolean containsOpenQuery(final TableSegment tableSegment) {
        if (tableSegment instanceof FunctionTableSegment) {
            return isOpenQuery((FunctionTableSegment) tableSegment);
        }
        if (tableSegment instanceof JoinTableSegment) {
            return containsOpenQuery(((JoinTableSegment) tableSegment).getLeft()) || containsOpenQuery(((JoinTableSegment) tableSegment).getRight());
        }
        return false;
    }

    private boolean isOpenQuery(final FunctionTableSegment functionTableSegment) {
        ExpressionSegment tableFunction = functionTableSegment.getTableFunction();
        return tableFunction instanceof FunctionSegment && OPENQUERY_FUNCTION_NAME.equalsIgnoreCase(((FunctionSegment) tableFunction).getFunctionName());
    }
}
