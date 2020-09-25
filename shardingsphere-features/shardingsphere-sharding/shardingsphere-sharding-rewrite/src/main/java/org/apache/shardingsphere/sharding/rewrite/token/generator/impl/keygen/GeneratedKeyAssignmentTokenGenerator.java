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
import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.GeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.LiteralGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ParameterMarkerGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.List;
import java.util.Optional;

/**
 * Generated key assignment token generator.
 */
@Setter
public final class GeneratedKeyAssignmentTokenGenerator extends BaseGeneratedKeyTokenGenerator implements ParametersAware {
    
    private List<Object> parameters;
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatementContext insertStatementContext) {
        return InsertStatementHandler.getSetAssignmentSegment(insertStatementContext.getSqlStatement()).isPresent();
    }
    
    @Override
    public GeneratedKeyAssignmentToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        InsertStatement insertStatement = insertStatementContext.getSqlStatement();
        Preconditions.checkState(InsertStatementHandler.getSetAssignmentSegment(insertStatement).isPresent());
        int startIndex = InsertStatementHandler.getSetAssignmentSegment(insertStatement).get().getStopIndex() + 1;
        return parameters.isEmpty() ? new LiteralGeneratedKeyAssignmentToken(startIndex, generatedKey.get().getColumnName(), generatedKey.get().getGeneratedValues().iterator().next())
                : new ParameterMarkerGeneratedKeyAssignmentToken(startIndex, generatedKey.get().getColumnName());
    }
}
