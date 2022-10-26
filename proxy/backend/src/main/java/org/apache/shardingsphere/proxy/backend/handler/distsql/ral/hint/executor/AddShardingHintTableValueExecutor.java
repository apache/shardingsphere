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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.HintManagerHolder;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;

/**
 * Add sharding hint table value executor.
 */
@RequiredArgsConstructor
public final class AddShardingHintTableValueExecutor extends AbstractHintUpdateExecutor<AddShardingHintTableValueStatement> {
    
    private final AddShardingHintTableValueStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() {
        HintManagerHolder.get().addTableShardingValue(sqlStatement.getLogicTableName(), sqlStatement.getShardingValue());
        return new UpdateResponseHeader(new EmptyStatement());
    }
}
