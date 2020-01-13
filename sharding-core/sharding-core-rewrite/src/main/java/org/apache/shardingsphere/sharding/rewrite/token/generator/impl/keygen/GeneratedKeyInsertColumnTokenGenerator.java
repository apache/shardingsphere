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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.route.engine.keygen.GeneratedKey;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.GeneratedKeyInsertColumnToken;

/**
 * Generated key insert column token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class GeneratedKeyInsertColumnTokenGenerator extends BaseGeneratedKeyTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatement insertStatement) {
        Optional<InsertColumnsSegment> sqlSegment = insertStatement.findSQLSegment(InsertColumnsSegment.class);
        return sqlSegment.isPresent() && !sqlSegment.get().getColumns().isEmpty();
    }
    
    @Override
    protected GeneratedKeyInsertColumnToken generateSQLToken(final SQLStatementContext sqlStatementContext, final GeneratedKey generatedKey) {
        Optional<InsertColumnsSegment> sqlSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(sqlSegment.isPresent());
        return new GeneratedKeyInsertColumnToken(sqlSegment.get().getStopIndex(), generatedKey.getColumnName());
    }
}
