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
import org.apache.shardingsphere.sql.parser.binder.statement.dml.ReplaceStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.ReplaceStatement;

import java.util.List;
import java.util.Optional;

/**
 * Generated key replace assignment token generator.
 */
@Setter
public final class GeneratedKeyReplaceAssignmentTokenGenerator extends BaseGeneratedKeyReplaceTokenGenerator implements ParametersAware {
    
    private List<Object> parameters;
    
    @Override
    protected boolean isGenerateSQLToken(final ReplaceStatement replaceStatement) {
        return replaceStatement.getSetAssignment().isPresent();
    }
    
    @Override
    public GeneratedKeyAssignmentToken generateSQLToken(final ReplaceStatementContext replaceStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = replaceStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        Preconditions.checkState(replaceStatementContext.getSqlStatement().getSetAssignment().isPresent());
        int startIndex = replaceStatementContext.getSqlStatement().getSetAssignment().get().getStopIndex() + 1;
        return parameters.isEmpty() ? new LiteralGeneratedKeyAssignmentToken(startIndex, generatedKey.get().getColumnName(), generatedKey.get().getGeneratedValues().getLast())
                : new ParameterMarkerGeneratedKeyAssignmentToken(startIndex, generatedKey.get().getColumnName());
    }
}
