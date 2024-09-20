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

package org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptConditionsAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;

import java.util.Collection;
import java.util.List;

/**
 * Insert predicate parameter rewriter for encrypt.
 */
@RequiredArgsConstructor
@Setter
public final class EncryptInsertPredicateParameterRewriter implements ParameterRewriter, EncryptConditionsAware, DatabaseNameAware {
    
    private final EncryptRule encryptRule;
    
    private Collection<EncryptCondition> encryptConditions;
    
    private String databaseName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && !((WhereAvailable) ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext()).getWhereSegments().isEmpty();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        EncryptPredicateParameterRewriter rewriter = new EncryptPredicateParameterRewriter(encryptRule);
        rewriter.setEncryptConditions(encryptConditions);
        rewriter.setDatabaseName(databaseName);
        rewriter.rewrite(paramBuilder, ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext(), params);
    }
}
