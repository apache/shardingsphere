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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.HintDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.hint.ClearHintStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetReadwriteSplittingHintSourceExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintSourceStatement;

import java.sql.SQLException;

/**
 * Hint statement executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintStatementExecutorFactory {
    
    /**
     * Create hint statement executor instance.
     *
     * @param sqlStatement hint statement
     * @param backendConnection backend connection
     * @return hint command executor
     * @throws SQLException SQL exception
     */
    public static HintStatementExecutor newInstance(final HintDistSQLStatement sqlStatement, final BackendConnection backendConnection) throws SQLException {
        if (sqlStatement instanceof SetReadwriteSplittingHintSourceStatement) {
            return new SetReadwriteSplittingHintSourceExecutor((SetReadwriteSplittingHintSourceStatement) sqlStatement);
        }
        if (sqlStatement instanceof ClearHintStatement) {
            return new ClearHintExecutor((ClearHintStatement) sqlStatement);
        }
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
