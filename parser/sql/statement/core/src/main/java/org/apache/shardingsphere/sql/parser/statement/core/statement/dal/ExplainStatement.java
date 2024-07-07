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

package org.apache.shardingsphere.sql.parser.statement.core.statement.dal;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.Optional;

/**
 * Explain statement.
 */
@Setter
public abstract class ExplainStatement extends AbstractSQLStatement implements DALStatement {
    
    private SQLStatement sqlStatement;
    
    /**
     * Get SQL statement.
     *
     * @return SQL statement
     */
    public Optional<SQLStatement> getSqlStatement() {
        return Optional.ofNullable(sqlStatement);
    }
    
    /**
     * Get simple table.
     *
     * @return simple table
     */
    public Optional<SimpleTableSegment> getSimpleTable() {
        return Optional.empty();
    }
    
    /**
     * Get column segment.
     *
     * @return column segment
     */
    public Optional<ColumnSegment> getColumnWild() {
        return Optional.empty();
    }
}
