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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowInstanceModeStatement;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show instance mode handler.
 */
public final class ShowInstanceModeHandler extends QueryableRALBackendHandler<ShowInstanceModeStatement, ShowInstanceModeHandler> {
    
    private static final String INSTANCE_ID = "instance_id";
    
    private static final String TYPE = "type";
    
    private static final String REPOSITORY = "repository";
    
    private static final String PROPS = "props";
    
    private static final String OVERWRITE = "overwrite";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(INSTANCE_ID, TYPE, REPOSITORY, PROPS, OVERWRITE);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        PersistRepositoryConfiguration repositoryConfiguration = instanceContext.getModeConfiguration().getRepository();
        String instanceId = instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
        String modeType = instanceContext.getModeConfiguration().getType();
        String repositoryType = null == repositoryConfiguration ? "" : repositoryConfiguration.getType();
        String props = null == repositoryConfiguration || null == repositoryConfiguration.getProps() ? "" : new Gson().toJson(repositoryConfiguration.getProps());
        String overwrite = String.valueOf(instanceContext.getModeConfiguration().isOverwrite());
        return Collections.singleton(Arrays.asList(instanceId, modeType, repositoryType, props, overwrite));
    }
}
