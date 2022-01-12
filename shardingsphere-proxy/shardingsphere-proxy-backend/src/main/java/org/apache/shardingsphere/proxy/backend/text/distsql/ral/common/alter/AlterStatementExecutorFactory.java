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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.AlterDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.excutor.AlterSQLParserRuleExecutor;

import java.sql.SQLException;

/**
 * Alter statement executor factory.
 */
public final class AlterStatementExecutorFactory {
    
    /**
     * Alter statement executor instance.
     *
     * @param sqlStatement alter distsql statement
     * @param connectionSession connection session
     * @return alter command executor
     * @throws SQLException SQL exception
     */
    public static AlterStatementExecutor newInstance(final AlterDistSQLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof AlterSQLParserRuleStatement) {
            return new AlterSQLParserRuleExecutor((AlterSQLParserRuleStatement) sqlStatement);
        }
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
