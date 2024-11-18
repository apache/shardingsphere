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

package org.apache.shardingsphere.encrypt.rewrite.context;

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionEngine;
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewritersRegistry;
import org.apache.shardingsphere.encrypt.rewrite.token.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.encrypt.rewrite.util.EncryptPredicateSegmentUtils;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewritersBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.Collections;

/**
 * SQL rewrite context decorator for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptSQLRewriteContextDecorator implements SQLRewriteContextDecorator<EncryptRule> {
    
    @Override
    public void decorate(final EncryptRule rule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        SQLStatementContext sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        if (!containsEncryptTable(rule, sqlStatementContext)) {
            return;
        }
        Collection<EncryptCondition> encryptConditions = createEncryptConditions(rule, sqlRewriteContext);
        String databaseName = sqlRewriteContext.getDatabase().getName();
        if (!sqlRewriteContext.getParameters().isEmpty()) {
            Collection<ParameterRewriter> parameterRewriters = new ParameterRewritersBuilder(sqlStatementContext).build(new EncryptParameterRewritersRegistry(rule, databaseName, encryptConditions));
            rewriteParameters(sqlRewriteContext, parameterRewriters);
        }
        SQLTokenGeneratorBuilder sqlTokenGeneratorBuilder = new EncryptTokenGenerateBuilder(rule, sqlStatementContext, encryptConditions, databaseName);
        sqlRewriteContext.addSQLTokenGenerators(sqlTokenGeneratorBuilder.getSQLTokenGenerators());
    }
    
    private boolean containsEncryptTable(final EncryptRule rule, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable)) {
            return false;
        }
        for (String each : ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames()) {
            if (rule.findEncryptTable(each).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<EncryptCondition> createEncryptConditions(final EncryptRule rule, final SQLRewriteContext sqlRewriteContext) {
        SQLStatementContext sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && !(((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext()).getWhereSegments().isEmpty()) {
            return doCreateEncryptConditions(rule, sqlRewriteContext, ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext());
        }
        return doCreateEncryptConditions(rule, sqlRewriteContext, sqlStatementContext);
    }
    
    private Collection<EncryptCondition> doCreateEncryptConditions(final EncryptRule rule, final SQLRewriteContext sqlRewriteContext, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        Collection<SelectStatementContext> allSubqueryContexts = EncryptPredicateSegmentUtils.getAllSubqueryContexts(sqlStatementContext);
        Collection<WhereSegment> whereSegments = EncryptPredicateSegmentUtils.getWhereSegments((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        Collection<ColumnSegment> columnSegments = EncryptPredicateSegmentUtils.getColumnSegments((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        return new EncryptConditionEngine(rule, sqlRewriteContext.getDatabase().getSchemas()).createEncryptConditions(whereSegments, columnSegments, sqlStatementContext,
                sqlRewriteContext.getDatabase().getName());
    }
    
    private void rewriteParameters(final SQLRewriteContext sqlRewriteContext, final Collection<ParameterRewriter> parameterRewriters) {
        for (ParameterRewriter each : parameterRewriters) {
            each.rewrite(sqlRewriteContext.getParameterBuilder(), sqlRewriteContext.getSqlStatementContext(), sqlRewriteContext.getParameters());
        }
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
