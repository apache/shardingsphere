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

package org.apache.shardingsphere.core.rewrite.token.generator.optional.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.generator.GeneratedKeyAware;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertRegularNamesToken;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert generated key from metadata token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertGeneratedKeyFromMetadataTokenGenerator implements OptionalSQLTokenGenerator, GeneratedKeyAware {
    
    private GeneratedKey generatedKey;
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final SQLStatementContext sqlStatementContext, final ParameterBuilder parameterBuilder) {
        return isNeedToGenerateSQLToken(sqlStatementContext.getSqlStatement())
                ? Optional.of(createInsertColumnsToken(sqlStatementContext)) : Optional.<InsertRegularNamesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatement sqlStatement) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).useDefaultColumns() && sqlStatement.findSQLSegment(InsertColumnsSegment.class).isPresent();
    }
    
    private InsertRegularNamesToken createInsertColumnsToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        return new InsertRegularNamesToken(insertColumnsSegment.get().getStopIndex(), 
                getActualInsertColumns((InsertSQLStatementContext) sqlStatementContext, generatedKey), true);
    }
    
    private List<String> getActualInsertColumns(final InsertSQLStatementContext insertSQLStatementContext, final GeneratedKey generatedKey) {
        List<String> result = new ArrayList<>(insertSQLStatementContext.getColumnNames());
        if (null != generatedKey && generatedKey.isGenerated()) {
            result.remove(generatedKey.getColumnName());
            result.add(generatedKey.getColumnName());
        }
        return result;
    }
}
