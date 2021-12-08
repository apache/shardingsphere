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
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.AddShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.AddShardingHintTableValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearShardingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ShowReadwriteSplittingHintStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ShowShardingHintStatusExecutor;
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
 * Hint statement executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintStatementExecutorFactory {
    
    /**
     * Create hint statement executor instance.
     *
     * @param sqlStatement hint statement
     * @param connectionSession connection session
     * @return hint command executor
     * @throws SQLException SQL exception
     */
    public static HintStatementExecutor newInstance(final HintDistSQLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
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
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
