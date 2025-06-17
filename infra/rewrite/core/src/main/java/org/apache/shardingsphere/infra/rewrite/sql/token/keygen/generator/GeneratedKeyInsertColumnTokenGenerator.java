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

package org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.pojo.GeneratedKeyInsertColumnToken;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;

import java.util.Optional;

/**
 * Generated key insert column token generator.
 */
public final class GeneratedKeyInsertColumnTokenGenerator extends BaseGeneratedKeyTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatementContext statementContext) {
        Optional<InsertColumnsSegment> insertColumns = statementContext.getSqlStatement().getInsertColumns();
        return insertColumns.isPresent() && !insertColumns.get().getColumns().isEmpty()
                && statementContext.getGeneratedKeyContext().isPresent() && !statementContext.getGeneratedKeyContext().get().getGeneratedValues().isEmpty();
    }
    
    @Override
    public GeneratedKeyInsertColumnToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        Optional<InsertColumnsSegment> insertColumns = insertStatementContext.getSqlStatement().getInsertColumns();
        Preconditions.checkState(insertColumns.isPresent());
        return new GeneratedKeyInsertColumnToken(insertColumns.get().getStopIndex(), generatedKey.get().getColumnName());
    }
}
