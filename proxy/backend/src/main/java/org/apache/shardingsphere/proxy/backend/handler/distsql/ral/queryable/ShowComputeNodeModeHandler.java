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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show compute node mode handler.
 */
public final class ShowComputeNodeModeHandler extends QueryableRALBackendHandler<ShowComputeNodeModeStatement> {
    
    private static final String TYPE = "type";
    
    private static final String REPOSITORY = "repository";
    
    private static final String PROPS = "props";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(TYPE, REPOSITORY, PROPS);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        PersistRepositoryConfiguration repositoryConfig = instanceContext.getModeConfiguration().getRepository();
        String modeType = instanceContext.getModeConfiguration().getType();
        String repositoryType = null == repositoryConfig ? "" : repositoryConfig.getType();
        String props = null == repositoryConfig || null == repositoryConfig.getProps() ? "" : new Gson().toJson(repositoryConfig.getProps());
        return Collections.singleton(new LocalDataQueryResultRow(modeType, repositoryType, props));
    }
}
