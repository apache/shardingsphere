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

package org.apache.shardingsphere.transaction.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Auto commit utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AutoCommitUtils {
    
    /**
     * Judge whether to start a new transaction.
     *
     * @param sqlStatement sqlStatement.
     * @return need to open a new transaction.
     */
    public static boolean needOpenTransaction(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement && !((SelectStatement) sqlStatement).getFrom().isPresent()) {
            return false;
        }
        return sqlStatement instanceof DDLStatement || sqlStatement instanceof DMLStatement;
    }
}
