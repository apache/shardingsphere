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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

/**
 * HBase checker factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseCheckerFactory {
    
    /**
     * Create new instance of HBase checker.
     *
     * @param sqlStatement SQL statement
     * @return created instance
     */
    public static HeterogeneousSQLStatementChecker newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return new HeterogeneousSelectStatementChecker((SelectStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new HeterogeneousInsertStatementChecker((InsertStatement) sqlStatement);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new HeterogeneousDeleteStatementChecker((DeleteStatement) sqlStatement);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new HeterogeneousUpdateStatementChecker((UpdateStatement) sqlStatement);
        }
        return new CommonHeterogeneousSQLStatementChecker(sqlStatement);
    }
}
