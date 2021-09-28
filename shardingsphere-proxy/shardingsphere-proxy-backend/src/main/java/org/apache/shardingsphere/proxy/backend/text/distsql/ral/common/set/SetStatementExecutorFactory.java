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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.SetDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.status.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.variable.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetInstanceStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetReadwriteSplittingStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetVariableExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;

import java.sql.SQLException;

/**
 * Set statement executor factory.
 */
public final class SetStatementExecutorFactory {
    
    /**
     * Create set statement executor instance.
     *
     * @param sqlStatement set statement
     * @param backendConnection backend connection
     * @return set command executor
     * @throws SQLException SQL exception
     */
    public static SetStatementExecutor newInstance(final SetDistSQLStatement sqlStatement, final BackendConnection backendConnection) throws SQLException {
        if (sqlStatement instanceof SetVariableStatement) {
            return new SetVariableExecutor((SetVariableStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof SetReadwriteSplittingStatusStatement) {
            return new SetReadwriteSplittingStatusExecutor((SetReadwriteSplittingStatusStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof SetInstanceStatusStatement) {
            return new SetInstanceStatusExecutor((SetInstanceStatusStatement) sqlStatement, backendConnection);
        }
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
