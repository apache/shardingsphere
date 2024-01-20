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

package org.apache.shardingsphere.data.pipeline.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.distsql.handler.type.ral.update.UpdatableRALExecutor;
import org.apache.shardingsphere.data.pipeline.migration.distsql.statement.DropMigrationCheckStatement;

/**
 * Drop migration check executor.
 */
public final class DropMigrationCheckExecutor implements UpdatableRALExecutor<DropMigrationCheckStatement> {
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
    
    @Override
    public void executeUpdate(final DropMigrationCheckStatement sqlStatement) {
        jobAPI.drop(sqlStatement.getJobId());
    }
    
    @Override
    public Class<DropMigrationCheckStatement> getType() {
        return DropMigrationCheckStatement.class;
    }
}
