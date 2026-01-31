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

package org.apache.shardingsphere.distsql.handler.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Show compute node mode executor.
 */
public final class ShowComputeNodeModeExecutor implements DistSQLQueryExecutor<ShowComputeNodeModeStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowComputeNodeModeStatement sqlStatement) {
        return Arrays.asList("type", "repository", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowComputeNodeModeStatement sqlStatement, final ContextManager contextManager) {
        PersistRepositoryConfiguration repositoryConfig = contextManager.getComputeNodeInstanceContext().getModeConfiguration().getRepository();
        String modeType = contextManager.getComputeNodeInstanceContext().getModeConfiguration().getType();
        String repositoryType = null == repositoryConfig ? null : repositoryConfig.getType();
        Properties props = null == repositoryConfig ? null : repositoryConfig.getProps();
        return Collections.singleton(new LocalDataQueryResultRow(modeType, repositoryType, props));
    }
    
    @Override
    public Class<ShowComputeNodeModeStatement> getType() {
        return ShowComputeNodeModeStatement.class;
    }
}
