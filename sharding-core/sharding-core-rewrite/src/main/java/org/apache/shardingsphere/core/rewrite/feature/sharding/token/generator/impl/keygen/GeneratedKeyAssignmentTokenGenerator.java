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

package org.apache.shardingsphere.core.rewrite.feature.sharding.token.generator.impl.keygen;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.core.rewrite.feature.sharding.token.pojo.GeneratedKeyAssignmentToken;
import org.apache.shardingsphere.core.rewrite.feature.sharding.token.pojo.LiteralGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.core.rewrite.feature.sharding.token.pojo.ParameterMarkerGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;

import java.util.List;

/**
 * Generated key assignment token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class GeneratedKeyAssignmentTokenGenerator extends BaseGeneratedKeyTokenGenerator implements ParametersAware {
    
    private List<Object> parameters;
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatement insertStatement) {
        return insertStatement.getSetAssignment().isPresent();
    }
    
    @Override
    protected GeneratedKeyAssignmentToken generateSQLToken(final SQLStatementContext sqlStatementContext, final GeneratedKey generatedKey) {
        Preconditions.checkState(((InsertStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().isPresent());
        int startIndex = ((InsertStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().get().getStopIndex() + 1;
        return parameters.isEmpty() ? new LiteralGeneratedKeyAssignmentToken(startIndex, generatedKey.getColumnName(), generatedKey.getGeneratedValues().getLast())
                : new ParameterMarkerGeneratedKeyAssignmentToken(startIndex, generatedKey.getColumnName());
    }
}
