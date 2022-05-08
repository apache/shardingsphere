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

package org.apache.shardingsphere.infra.binder.statement.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert SQL statement context util.
 */
public final class InsertStatementContextUtil {
    
    /**
     * Get first index on MultiInsertStatement.
     * @param sqlStatementContext InsertStatementContext
     * 
     * @return column names collection
     */
    public static SimpleTableSegment getTable(final InsertStatementContext sqlStatementContext) {
        return getInsertStatements(sqlStatementContext.getSqlStatement()).get(0).getTable();
    }
    
    /**
     * Get InsertStatement collection from MultiInsertStatement.
     * @param sqlStatement InsertStatement
     *
     * @return InsertStatement collection
     */
    public static List<InsertStatement> getInsertStatements(final InsertStatement sqlStatement) {
        Optional<InsertMultiTableElementSegment> optional = getInsertMultiTableElementSegment(sqlStatement);
        if (optional.isPresent()) {
            return new LinkedList<>(optional.get().getInsertStatements());
        }
        return Collections.singletonList(sqlStatement);
    }
    
    private static Optional<InsertMultiTableElementSegment> getInsertMultiTableElementSegment(final InsertStatement sqlStatement) {
        if (sqlStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) sqlStatement).getInsertMultiTableElementSegment();
        }
        return Optional.empty();
    }
}
