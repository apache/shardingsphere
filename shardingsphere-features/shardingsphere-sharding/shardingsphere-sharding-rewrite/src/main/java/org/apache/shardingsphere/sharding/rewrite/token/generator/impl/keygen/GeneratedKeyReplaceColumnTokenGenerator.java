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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.GeneratedKeyInsertColumnToken;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.ReplaceStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.ReplaceStatement;

import java.util.Optional;

/**
 * Generated key replace column token generator.
 */
public final class GeneratedKeyReplaceColumnTokenGenerator extends BaseGeneratedKeyReplaceTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLToken(final ReplaceStatement replaceStatement) {
        Optional<InsertColumnsSegment> sqlSegment = replaceStatement.getReplaceColumns();
        return sqlSegment.isPresent() && !sqlSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public GeneratedKeyInsertColumnToken generateSQLToken(final ReplaceStatementContext replaceStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = replaceStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        Optional<InsertColumnsSegment> sqlSegment = replaceStatementContext.getSqlStatement().getReplaceColumns();
        Preconditions.checkState(sqlSegment.isPresent());
        return new GeneratedKeyInsertColumnToken(sqlSegment.get().getStopIndex(), generatedKey.get().getColumnName());
    }
}
