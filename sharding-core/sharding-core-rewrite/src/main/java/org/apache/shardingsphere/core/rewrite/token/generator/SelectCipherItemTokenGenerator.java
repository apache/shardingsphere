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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectCipherItemToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Select cipher item token generator.
 *
 * @author panjuan
 */
public final class SelectCipherItemTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    private EncryptRule encryptRule;
    
    private OptimizedStatement optimizedStatement;
    
    @Override
    public Collection<SelectCipherItemToken> generateSQLTokens(final OptimizedStatement optimizedStatement, 
                                                               final ParameterBuilder parameterBuilder, final EncryptRule rule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Collections.emptyList();
        }
        initParameters(rule, optimizedStatement);
        return createSelectCipherItemTokens();
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        if (!(optimizedStatement instanceof ShardingSelectOptimizedStatement)) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    private void initParameters(final EncryptRule rule, final OptimizedStatement optimizedStatement) {
        encryptRule = rule;
        this.optimizedStatement = optimizedStatement;
    }
    
    private Collection<SelectCipherItemToken> createSelectCipherItemTokens() {
        Collection<SelectCipherItemToken> result = new LinkedList<>();
        SelectItemsSegment selectItemsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SelectItemsSegment.class).get();
        Collection<String> logicColumns = encryptRule.getCipherColumns(optimizedStatement.getTables().getSingleTableName());
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            if (isLogicColumn(each, logicColumns)) {
                result.add(new SelectCipherItemToken(each.getStartIndex(), each.getStopIndex(), ((ColumnSelectItemSegment) each).getName(), getColumnAlias(each)));
            }
        }
        return result;
    }
    
    private boolean isLogicColumn(final SelectItemSegment each, final Collection<String> logicColumns) {
        return each instanceof ColumnSelectItemSegment && logicColumns.contains(((ColumnSelectItemSegment) each).getName());
    }
    
    private String getColumnAlias(final SelectItemSegment selectItemSegment) {
        Optional<String> alias = ((ColumnSelectItemSegment) selectItemSegment).getAlias();
        return alias.isPresent() ? alias.get() : "";
    }
}
