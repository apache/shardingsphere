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
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * Insert columns token generator.
 *
 * @author panjuan
 */
public final class ShardingInsertColumnsTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Optional<InsertColumnsToken> generateSQLToken(final SQLStatement sqlStatement, final EncryptRule encryptRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatement.findSQLSegment(InsertColumnsSegment.class);
        if (!(sqlStatement instanceof InsertStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return Optional.of(createInsertColumnsToken((InsertStatement) sqlStatement, insertColumnsSegment.get(), encryptRule));
    }
    
    private InsertColumnsToken createInsertColumnsToken(final InsertStatement insertStatement, final InsertColumnsSegment segment, final EncryptRule encryptRule) {
        InsertColumnsToken result;
        if (segment.getColumns().isEmpty()) {
            result = new InsertColumnsToken(segment.getStopIndex(), false);
            result.getColumns().addAll(insertStatement.getColumnNames());
        } else {
            result = new InsertColumnsToken(segment.getStopIndex(), true);
        }
        fillWithQueryAssistedColumn(insertStatement, encryptRule, result);
        return result;
    }
    
    private void fillWithQueryAssistedColumn(final InsertStatement insertStatement, final EncryptRule encryptRule, final InsertColumnsToken insertColumnsToken) {
        for (String each : insertStatement.getColumnNames()) {
            Optional<String> assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                insertColumnsToken.getColumns().remove(assistedColumnName.get());
                insertColumnsToken.getColumns().add(assistedColumnName.get());
            }
        }
    }
}
