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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.ShowDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowAllVariablesExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowInstanceExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowReadwriteSplittingReadResourcesExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowVariableExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;

import java.sql.SQLException;

/**
 * Show statement executor factory.
 */
public final class ShowStatementExecutorFactory {
    
    /**
     * Create show statement executor instance.
     *
     * @param sqlStatement show statement
     * @param connectionSession connection session
     * @return show command executor
     * @throws SQLException SQL exception
     */
    public static ShowStatementExecutor newInstance(final ShowDistSQLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof ShowInstanceStatement) {
            return new ShowInstanceExecutor();
        }
        if (sqlStatement instanceof ShowReadwriteSplittingReadResourcesStatement) {
            return new ShowReadwriteSplittingReadResourcesExecutor((ShowReadwriteSplittingReadResourcesStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof ShowAllVariablesStatement) {
            return new ShowAllVariablesExecutor(connectionSession);
        }
        if (sqlStatement instanceof ShowVariableStatement) {
            return new ShowVariableExecutor((ShowVariableStatement) sqlStatement, connectionSession);
        }
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
