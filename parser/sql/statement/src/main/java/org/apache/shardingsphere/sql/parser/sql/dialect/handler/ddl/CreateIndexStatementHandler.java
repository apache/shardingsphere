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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.IndexPartitionsSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;

import java.util.Optional;

/**
 * Create index statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateIndexStatementHandler implements SQLStatementHandler {
    
    /**
     * Get generated index start index.
     *
     * @param createIndexStatement create index statement
     * @return generated index start index
     */
    public static Optional<Integer> getGeneratedIndexStartIndex(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCreateIndexStatement) createIndexStatement).getGeneratedIndexStartIndex();
        }
        if (createIndexStatement instanceof OpenGaussStatement) {
            return ((OpenGaussCreateIndexStatement) createIndexStatement).getGeneratedIndexStartIndex();
        }
        return Optional.empty();
    }
    
    /**
     * Judge whether contains if not exists or not.
     *
     * @param createIndexStatement create index statement
     * @return whether contains if not exists or not
     */
    public static boolean ifNotExists(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCreateIndexStatement) createIndexStatement).isIfNotExists();
        }
        if (createIndexStatement instanceof OpenGaussStatement) {
            return ((OpenGaussCreateIndexStatement) createIndexStatement).isIfNotExists();
        }
        return false;
    }
    
    /**
     * Get algorithm type segment.
     *
     * @param createIndexStatement create index statement
     * @return algorithm type segment
     */
    public static Optional<AlgorithmTypeSegment> getAlgorithmTypeSegment(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof MySQLCreateIndexStatement) {
            return ((MySQLCreateIndexStatement) createIndexStatement).getGetAlgorithmSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get lock table segment.
     *
     * @param createIndexStatement create index statement
     * @return lock table segment
     */
    public static Optional<LockTableSegment> getLockTableSegment(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof MySQLCreateIndexStatement) {
            return ((MySQLCreateIndexStatement) createIndexStatement).getLockTableSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get index partitions segment.
     *
     * @param createIndexStatement create index statement
     * @return index partitions segment
     */
    public static Optional<IndexPartitionsSegment> getIndexPartitionsSegment(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof OpenGaussCreateIndexStatement) {
            return ((OpenGaussCreateIndexStatement) createIndexStatement).getIndexPartitionsSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get tablespace segment.
     *
     * @param createIndexStatement create index statement
     * @return tablespace segment
     */
    public static Optional<TablespaceSegment> getTablespaceSegment(final CreateIndexStatement createIndexStatement) {
        if (createIndexStatement instanceof OpenGaussCreateIndexStatement) {
            return ((OpenGaussCreateIndexStatement) createIndexStatement).getTablespace();
        }
        return Optional.empty();
    }
    
}
