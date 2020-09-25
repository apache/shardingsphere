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
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generated key for use default insert columns token generator.
 */
public final class GeneratedKeyForUseDefaultInsertColumnsTokenGenerator extends BaseGeneratedKeyTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatementContext insertStatementContext) {
        return insertStatementContext.useDefaultColumns();
    }
    
    @Override
    public UseDefaultInsertColumnsToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Preconditions.checkState(insertColumnsSegment.isPresent());
        return new UseDefaultInsertColumnsToken(insertColumnsSegment.get().getStopIndex(), getColumnNames(insertStatementContext));
    }
    
    private List<String> getColumnNames(final InsertStatementContext insertStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        List<String> result = new ArrayList<>(insertStatementContext.getColumnNames());
        result.remove(generatedKey.get().getColumnName());
        result.add(generatedKey.get().getColumnName());
        return result;
    }
}
