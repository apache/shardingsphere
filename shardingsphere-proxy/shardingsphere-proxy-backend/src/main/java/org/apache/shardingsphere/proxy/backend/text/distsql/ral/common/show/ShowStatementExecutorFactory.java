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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.ExportSchemaConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.ShowDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTrafficRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ExportSchemaConfigurationExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowAllVariablesExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowAuthorityRuleExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowInstanceExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowInstanceModeExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowReadwriteSplittingReadResourcesExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowSQLParserRuleExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowTableMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowTrafficRulesExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowTransactionRuleExecutor;
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
        if (sqlStatement instanceof ShowAuthorityRuleStatement) {
            return new ShowAuthorityRuleExecutor();
        }
        if (sqlStatement instanceof ShowTransactionRuleStatement) {
            return new ShowTransactionRuleExecutor();
        }
        if (sqlStatement instanceof ShowSQLParserRuleStatement) {
            return new ShowSQLParserRuleExecutor();
        }
        if (sqlStatement instanceof ShowAllVariablesStatement) {
            return new ShowAllVariablesExecutor(connectionSession);
        }
        if (sqlStatement instanceof ShowVariableStatement) {
            return new ShowVariableExecutor((ShowVariableStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof ShowReadwriteSplittingReadResourcesStatement) {
            return new ShowReadwriteSplittingReadResourcesExecutor((ShowReadwriteSplittingReadResourcesStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof ShowTableMetadataStatement) {
            return new ShowTableMetadataExecutor((ShowTableMetadataStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof ShowInstanceModeStatement) {
            return new ShowInstanceModeExecutor();
        }
        if (sqlStatement instanceof ShowTrafficRulesStatement) {
            return new ShowTrafficRulesExecutor((ShowTrafficRulesStatement) sqlStatement);
        }
        if (sqlStatement instanceof ExportSchemaConfigurationStatement) {
            return new ExportSchemaConfigurationExecutor((ExportSchemaConfigurationStatement) sqlStatement, connectionSession);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
    }
}
