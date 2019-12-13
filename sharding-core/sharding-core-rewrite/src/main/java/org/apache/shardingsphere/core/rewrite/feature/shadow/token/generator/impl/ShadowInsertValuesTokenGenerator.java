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

package org.apache.shardingsphere.core.rewrite.feature.shadow.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.rewrite.feature.shadow.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.InsertValuesToken.InsertValueToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.InsertValueContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Insert values token generator for shadow.
 *
 * @author zhyee
 */
@Setter
public final class ShadowInsertValuesTokenGenerator extends BaseShadowSQLTokenGenerator implements OptionalSQLTokenGenerator, PreviousSQLTokensAware {

    private List<SQLToken> previousSQLTokens;

    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && ((InsertStatement) sqlStatementContext.getSqlStatement()).getColumnNames().contains(getShadowRule().getColumn());
    }

    @Override
    public InsertValuesToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        //todo findPreviousSQLToken?
        return generateNewSQLToken((InsertSQLStatementContext) sqlStatementContext);
    }

    private InsertValuesToken generateNewSQLToken(final InsertSQLStatementContext sqlStatementContext) {
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            InsertValueToken insertValueToken = new InsertValueToken(each.getValueExpressions(), Collections.<DataNode>emptyList());
            Iterator<String> descendingColumnNames = sqlStatementContext.getDescendingColumnNames();
            while (descendingColumnNames.hasNext()) {
                String columnName = descendingColumnNames.next();
                if (getShadowRule().getColumn().equals(columnName)) {
                    removeValueToken(insertValueToken, sqlStatementContext, columnName);
                }
            }
            result.getInsertValueTokens().add(insertValueToken);
        }
        return result;
    }

    private void removeValueToken(final InsertValueToken insertValueToken, final InsertSQLStatementContext sqlStatementContext, final String columnName) {
        Optional<SQLToken> useDefaultInsertColumnsToken = findPreviousSQLToken(UseDefaultInsertColumnsToken.class);
        int columnIndex = useDefaultInsertColumnsToken.isPresent()
                ? ((UseDefaultInsertColumnsToken) useDefaultInsertColumnsToken.get()).getColumns().indexOf(columnName) : sqlStatementContext.getColumnNames().indexOf(columnName);
        insertValueToken.getValues().remove(columnIndex);
    }

    private Optional<SQLToken> findPreviousSQLToken(final Class<?> sqlToken) {
        for (SQLToken each : previousSQLTokens) {
            if (each.getClass().equals(sqlToken)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }

    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.min(result, each.getStartIndex());
        }
        return result;
    }

    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.max(result, each.getStopIndex());
        }
        return result;
    }

    @Override
    public void setPreviousSQLTokens(final List<SQLToken> previousSQLTokens) {

    }
}
