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

package org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.generic.SubstitutableColumnNameToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert cipher column name token generator.
 *
 * @author panjuan
 */
public final class InsertCipherNameTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return sqlStatementContext instanceof InsertSQLStatementContext && insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(sqlSegment.isPresent());
        Map<String, String> logicAndCipherColumns = getEncryptRule().getLogicAndCipherColumns(sqlStatementContext.getTablesContext().getSingleTableName());
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ColumnSegment each : sqlSegment.get().getColumns()) {
            if (logicAndCipherColumns.keySet().contains(each.getName())) {
                result.add(new SubstitutableColumnNameToken(each.getStartIndex(), each.getStopIndex(), logicAndCipherColumns.get(each.getName())));
            }
        }
        return result;
    }
}
