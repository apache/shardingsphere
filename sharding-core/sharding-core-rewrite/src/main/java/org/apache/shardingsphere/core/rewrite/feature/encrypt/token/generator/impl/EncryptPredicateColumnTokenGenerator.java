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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.generic.WhereSegmentAvailable;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.pojo.EncryptColumnNameToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.TableMetasAware;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Predicate column token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator implements CollectionSQLTokenGenerator, TableMetasAware, EncryptRuleAware, QueryWithCipherColumnAware {
    
    private TableMetas tableMetas;
    
    private EncryptRule encryptRule;
    
    private boolean queryWithCipherColumn;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable && ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent();
    }
    
    @Override
    public Collection<EncryptColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent());
        Collection<EncryptColumnNameToken> result = new LinkedList<>();
        for (AndPredicate each : ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().get().getAndPredicates()) {
            for (PredicateSegment predicateSegment : each.getPredicates()) {
                Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(predicateSegment.getColumn(), tableMetas);
                if (!tableName.isPresent()) {
                    continue;
                }
                Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName.get());
                if (!encryptTable.isPresent() || !encryptTable.get().findShardingEncryptor(predicateSegment.getColumn().getName()).isPresent()) {
                    continue;
                }
                int startIndex = predicateSegment.getColumn().getOwner().isPresent() ? predicateSegment.getColumn().getOwner().get().getStopIndex() + 2 : predicateSegment.getColumn().getStartIndex();
                int stopIndex = predicateSegment.getColumn().getStopIndex();
                if (!queryWithCipherColumn) { 
                    Optional<String> plainColumn = encryptTable.get().findPlainColumn(predicateSegment.getColumn().getName());
                    if (plainColumn.isPresent()) {
                        result.add(new EncryptColumnNameToken(startIndex, stopIndex, plainColumn.get()));
                        continue;
                    }
                }
                Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(predicateSegment.getColumn().getName());
                if (assistedQueryColumn.isPresent()) {
                    result.add(new EncryptColumnNameToken(startIndex, stopIndex, assistedQueryColumn.get()));
                } else {
                    result.add(new EncryptColumnNameToken(startIndex, stopIndex, encryptTable.get().getCipherColumn(predicateSegment.getColumn().getName())));
                }
            }
        }
        return result;
    }
}
