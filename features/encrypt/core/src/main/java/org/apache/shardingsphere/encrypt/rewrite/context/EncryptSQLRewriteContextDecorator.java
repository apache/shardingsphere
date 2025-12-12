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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewritersBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

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
        Collection<EncryptCondition> encryptConditions = createEncryptConditions(rule, sqlStatementContext);
        if (!sqlRewriteContext.getParameters().isEmpty()) {
            EncryptParameterRewritersRegistry rewritersRegistry = new EncryptParameterRewritersRegistry(rule, sqlRewriteContext, encryptConditions);
            Collection<ParameterRewriter> parameterRewriters = new ParameterRewritersBuilder(sqlStatementContext).build(rewritersRegistry);
            rewriteParameters(sqlRewriteContext, parameterRewriters);
        }
        SQLTokenGeneratorBuilder sqlTokenGeneratorBuilder = createSQLTokenGeneratorBuilder(rule, sqlRewriteContext, sqlStatementContext, encryptConditions);
        sqlRewriteContext.addSQLTokenGenerators(sqlTokenGeneratorBuilder.getSQLTokenGenerators());
    }
    
    private boolean containsEncryptTable(final EncryptRule rule, final SQLStatementContext sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            if (rule.findEncryptTable(each.getTableName().getIdentifier().getValue()).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<EncryptCondition> createEncryptConditions(final EncryptRule rule, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereContextAvailable)) {
            return Collections.emptyList();
        }
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<WhereSegment> whereSegments = SQLStatementContextExtractor.getWhereSegments((WhereContextAvailable) sqlStatementContext, allSubqueryContexts);
        return new EncryptConditionEngine(rule).createEncryptConditions(whereSegments);
    }
    
    private void rewriteParameters(final SQLRewriteContext sqlRewriteContext, final Collection<ParameterRewriter> parameterRewriters) {
        for (ParameterRewriter each : parameterRewriters) {
            each.rewrite(sqlRewriteContext.getParameterBuilder(), sqlRewriteContext.getSqlStatementContext(), sqlRewriteContext.getParameters());
        }
    }
    
    private SQLTokenGeneratorBuilder createSQLTokenGeneratorBuilder(final EncryptRule rule, final SQLRewriteContext sqlRewriteContext,
                                                                    final SQLStatementContext sqlStatementContext, final Collection<EncryptCondition> encryptConditions) {
        return new EncryptTokenGenerateBuilder(sqlStatementContext, encryptConditions, rule, sqlRewriteContext);
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
