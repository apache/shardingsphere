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
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertGeneratedKeyToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert generated key token generator.
 *
 * @author panjuan
 */
public final class InsertGeneratedKeyTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertGeneratedKeyToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatement.findSQLSegment(InsertColumnsSegment.class);
        if (!(sqlStatement instanceof InsertStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertGeneratedKeyToken((InsertStatement) sqlStatement, insertColumnsSegment.get(), shardingRule);
    }
    
    private Optional<InsertGeneratedKeyToken> createInsertGeneratedKeyToken(final InsertStatement insertStatement, final InsertColumnsSegment segment, final ShardingRule shardingRule) {
        Optional<String> generatedKeyColumn = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (insertStatement.isNeededToAppendGeneratedKey()) {
            return Optional.of(new InsertGeneratedKeyToken(segment.getStopIndex(), generatedKeyColumn.get(), isToAddCloseParenthesis(insertStatement, segment)));
        }
        return Optional.absent();
    }
    
    private boolean isToAddCloseParenthesis(final InsertStatement insertStatement, final InsertColumnsSegment segment) {
        return !insertStatement.isNeededToAppendAssistedColumns() && segment.getColumns().isEmpty();
    }
}
