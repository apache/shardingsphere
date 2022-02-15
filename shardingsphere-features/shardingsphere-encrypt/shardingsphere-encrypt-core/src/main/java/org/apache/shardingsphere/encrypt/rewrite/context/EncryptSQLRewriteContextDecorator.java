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
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriterBuilder;
import org.apache.shardingsphere.encrypt.rewrite.token.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.Collections;

/**
 * SQL rewrite context decorator for encrypt.
 */
public final class EncryptSQLRewriteContextDecorator implements SQLRewriteContextDecorator<EncryptRule> {
    
    @SuppressWarnings("rawtypes")
    @Override
    public void decorate(final EncryptRule encryptRule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        Collection<EncryptCondition> encryptConditions = getEncryptConditions(encryptRule, sqlRewriteContext);
        SQLStatementContext<?> sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        boolean containsEncryptTable = containsEncryptTable(encryptRule, sqlStatementContext);
        if (!sqlRewriteContext.getParameters().isEmpty()) {
            Collection<ParameterRewriter> parameterRewriters = new EncryptParameterRewriterBuilder(encryptRule,
                    sqlRewriteContext.getSchemaName(), sqlRewriteContext.getSchema(), sqlStatementContext, encryptConditions, containsEncryptTable).getParameterRewriters();
            rewriteParameters(sqlRewriteContext, parameterRewriters);
        }
        // TODO Optimize logic of statement context init for encryptors
        encryptRule.setUpEncryptorSchema(sqlRewriteContext.getSchema());
        sqlRewriteContext.addSQLTokenGenerators(new EncryptTokenGenerateBuilder(
                encryptRule, sqlStatementContext, encryptConditions, containsEncryptTable, sqlRewriteContext.getSchemaName()).getSQLTokenGenerators());
    }
    
    private Collection<EncryptCondition> getEncryptConditions(final EncryptRule encryptRule, final SQLRewriteContext sqlRewriteContext) {
        SQLStatementContext<?> sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        Collection<WhereSegment> whereSegments = sqlStatementContext instanceof WhereAvailable ? ((WhereAvailable) sqlStatementContext).getWhereSegments() : Collections.emptyList();
        return whereSegments.isEmpty() ? Collections.emptyList() : new EncryptConditionEngine(encryptRule, sqlRewriteContext.getSchema()).createEncryptConditions(sqlStatementContext);
    }
    
    @SuppressWarnings("rawtypes")
    private boolean containsEncryptTable(final EncryptRule encryptRule, final SQLStatementContext sqlStatementContext) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            if (encryptRule.findEncryptTable(each).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
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
