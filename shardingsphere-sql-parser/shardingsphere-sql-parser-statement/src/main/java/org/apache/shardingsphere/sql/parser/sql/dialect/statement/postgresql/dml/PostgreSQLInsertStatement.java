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

package org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;

import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL insert statement.
 */
@Getter
@Setter
public final class PostgreSQLInsertStatement extends InsertStatement implements PostgreSQLStatement {
    
    private WithSegment withSegment;

    /**
     * Get with segment.
     *
     * @return with segment.
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }

    @Override
    public boolean useDefaultColumns() {
        return getColumns().isEmpty();
    }

    @Override
    public List<String> getColumnNames() {
        return getColumnNamesForInsertColumns();
    }

    @Override
    public int getValueListCount() {
        return getValues().size();
    }

    @Override
    public int getValueCountForPerGroup() {
        if (!getValues().isEmpty()) {
            return getValues().iterator().next().getValues().size();
        }
        Optional<SubquerySegment> insertSelectOptional = getInsertSelect();
        if (insertSelectOptional.isPresent()) {
            return insertSelectOptional.get().getSelect().getProjections().getProjections().size();
        }
        return 0;
    }

    @Override
    public List<List<ExpressionSegment>> getAllValueExpressions() {
        return getAllValueExpressionsFromValues();
    }
}
