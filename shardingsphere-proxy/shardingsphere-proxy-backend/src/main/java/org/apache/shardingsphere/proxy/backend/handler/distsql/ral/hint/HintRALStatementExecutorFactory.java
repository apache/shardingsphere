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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.hint.ClearHintStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.AddShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.AddShardingHintTableValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearShardingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.SetReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.SetShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ShowReadwriteSplittingHintStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ShowShardingHintStatusExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintStatusStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ClearShardingHintStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.SetShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;

import java.sql.SQLException;

/**
 * Hint RAL statement executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintRALStatementExecutorFactory {
    
    /**
     * Create hint RAL statement executor instance.
     *
     * @param sqlStatement hint RAL statement
     * @param connectionSession connection session
     * @return hint RAL statement executor
     * @throws SQLException SQL exception
     */
    public static HintRALStatementExecutor<? extends HintRALStatement> newInstance(final HintRALStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof SetReadwriteSplittingHintStatement) {
            return new SetReadwriteSplittingHintExecutor((SetReadwriteSplittingHintStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowReadwriteSplittingHintStatusStatement) {
            return new ShowReadwriteSplittingHintStatusExecutor();
        }
        if (sqlStatement instanceof ClearReadwriteSplittingHintStatement) {
            return new ClearReadwriteSplittingHintExecutor();
        }
        if (sqlStatement instanceof ClearHintStatement) {
            return new ClearHintExecutor();
        }
        if (sqlStatement instanceof SetShardingHintDatabaseValueStatement) {
            return new SetShardingHintDatabaseValueExecutor((SetShardingHintDatabaseValueStatement) sqlStatement);
        }
        if (sqlStatement instanceof AddShardingHintDatabaseValueStatement) {
            return new AddShardingHintDatabaseValueExecutor((AddShardingHintDatabaseValueStatement) sqlStatement);
        }
        if (sqlStatement instanceof AddShardingHintTableValueStatement) {
            return new AddShardingHintTableValueExecutor((AddShardingHintTableValueStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowShardingHintStatusStatement) {
            return new ShowShardingHintStatusExecutor(connectionSession);
        }
        if (sqlStatement instanceof ClearShardingHintStatement) {
            return new ClearShardingHintExecutor();
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
    }
}
