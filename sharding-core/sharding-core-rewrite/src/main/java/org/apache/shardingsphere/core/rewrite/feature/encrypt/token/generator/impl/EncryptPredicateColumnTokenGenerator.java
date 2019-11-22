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
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.TableMetasAware;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Predicate column token generator for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, TableMetasAware, QueryWithCipherColumnAware {
    
    private TableMetas tableMetas;
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable && ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().isPresent());
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (AndPredicate each : ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere().get().getAndPredicates()) {
            result.addAll(generateSQLTokens(sqlStatementContext, each));
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final AndPredicate andPredicate) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (PredicateSegment each : andPredicate.getPredicates()) {
            Optional<EncryptTable> encryptTable = findEncryptTable(sqlStatementContext, each);
            if (!encryptTable.isPresent() || !encryptTable.get().findShardingEncryptor(each.getColumn().getName()).isPresent()) {
                continue;
            }
            int startIndex = each.getColumn().getOwner().isPresent() ? each.getColumn().getOwner().get().getStopIndex() + 2 : each.getColumn().getStartIndex();
            int stopIndex = each.getColumn().getStopIndex();
            if (!queryWithCipherColumn) { 
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(each.getColumn().getName());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, plainColumn.get()));
                    continue;
                }
            }
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(each.getColumn().getName());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.isPresent() ? new SubstitutableColumnNameToken(startIndex, stopIndex, assistedQueryColumn.get())
                    : new SubstitutableColumnNameToken(startIndex, stopIndex, encryptTable.get().getCipherColumn(each.getColumn().getName()));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private Optional<EncryptTable> findEncryptTable(final SQLStatementContext sqlStatementContext, final PredicateSegment segment) {
        Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(segment.getColumn(), getRelationMetas(tableMetas));
        return tableName.isPresent() ? getEncryptRule().findEncryptTable(tableName.get()) : Optional.<EncryptTable>absent();
    }
    
    private RelationMetas getRelationMetas(final TableMetas tableMetas) {
        Map<String, RelationMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size());
        for (String each : tableMetas.getAllTableNames()) {
            TableMetaData tableMetaData = tableMetas.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
}
