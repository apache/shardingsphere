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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCopyStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Copy statement handler class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CopyStatementHandler implements SQLStatementHandler {
    
    /**
     * Get prepare statement query segment.
     *
     * @param copyStatement copy statement
     * @return prepare statement query segment
     */
    public static Optional<PrepareStatementQuerySegment> getPrepareStatementQuerySegment(final CopyStatement copyStatement) {
        if (copyStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCopyStatement) copyStatement).getPrepareStatementQuerySegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get list of column segment.
     *
     * @param copyStatement copy statement
     * @return list of columns
     */
    public static Collection<ColumnSegment> getColumns(final CopyStatement copyStatement) {
        if (copyStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCopyStatement) copyStatement).getColumns();
        }
        return Collections.emptyList();
    }
}
