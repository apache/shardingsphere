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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterIndexStatement;

import java.util.Optional;

/**
 * Alter index statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterIndexStatementHandler implements SQLStatementHandler {
    
    /**
     * Get simple table segment.
     *
     * @param alterIndexStatement alter index statement
     * @return simple table segment
     */
    public static Optional<SimpleTableSegment> getSimpleTableSegment(final AlterIndexStatement alterIndexStatement) {
        if (alterIndexStatement instanceof SQLServerStatement) {
            return ((SQLServerAlterIndexStatement) alterIndexStatement).getTable();
        }
        return Optional.empty();
    }
    
    /**
     * Get rename index segment.
     *
     * @param alterIndexStatement alter index statement
     * @return rename index segment
     */
    public static Optional<IndexSegment> getRenameIndexSegment(final AlterIndexStatement alterIndexStatement) {
        if (alterIndexStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLAlterIndexStatement) alterIndexStatement).getRenameIndex();
        }
        if (alterIndexStatement instanceof OpenGaussStatement) {
            return ((OpenGaussAlterIndexStatement) alterIndexStatement).getRenameIndex();
        }
        return Optional.empty();
    }
}
