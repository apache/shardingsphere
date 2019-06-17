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
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertColumnsToken;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert columns token generator.
 *
 * @author panjuan
 */
public final class InsertColumnsTokenGenerator implements OptionalSQLTokenGenerator<BaseRule> {
    
    @Override
    public Optional<InsertColumnsToken> generateSQLToken(final SQLStatement sqlStatement, final List<Object> parameters, final BaseRule rule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatement.findSQLSegment(InsertColumnsSegment.class);
        if (!(sqlStatement instanceof InsertStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertColumnsToken((InsertStatement) sqlStatement, rule, insertColumnsSegment.get());
    }
    
    private Optional<InsertColumnsToken> createInsertColumnsToken(final InsertStatement insertStatement, final BaseRule rule, final InsertColumnsSegment segment) {
        if (!segment.getColumns().isEmpty()) {
            return Optional.absent();
        }
        InsertColumnsToken result = new InsertColumnsToken(segment.getStopIndex(), new LinkedList<>(insertStatement.getColumnNames()), !isNeededToAppendColumns(insertStatement, rule));
        return Optional.of(result);
    }
    
    private boolean isNeededToAppendColumns(final InsertStatement insertStatement, final BaseRule rule) {
        if (rule instanceof ShardingRule) {
            return isNeededToAppendColumns(insertStatement, (ShardingRule) rule);
        }
        if (rule instanceof EncryptRule) {
            return isNeededToAppendColumns(insertStatement.getTables().getSingleTableName(), (EncryptRule) rule);
        }
        return false;
    }
    
    private boolean isNeededToAppendColumns(final InsertStatement insertStatement, final ShardingRule shardingRule) {
        String tableName = insertStatement.getTables().getSingleTableName();
        return isNeededToAppendGeneratedKey(tableName, insertStatement.getColumnNames(), shardingRule) || isNeededToAppendColumns(tableName, shardingRule.getEncryptRule());
    }
    
    private boolean isNeededToAppendGeneratedKey(final String tableName, final Collection<String> columnNames, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumnName.isPresent() && !columnNames.contains(generateKeyColumnName.get());
    }
    
    private boolean isNeededToAppendColumns(final String tableName, final EncryptRule encryptRule) {
        return !encryptRule.getEncryptorEngine().getAssistedQueryColumns(tableName).isEmpty();
    }
}
