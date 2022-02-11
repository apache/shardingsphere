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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show instance mode executor.
 */
public final class ShowInstanceModeExecutor extends AbstractShowExecutor {
    
    private static final String ID = "id";
    
    private static final String TYPE = "type";
    
    private static final String REPOSITORY = "repository";
    
    private static final String PROPS = "props";
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", ID, ID, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", TYPE, TYPE, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", REPOSITORY, REPOSITORY, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", PROPS, PROPS, Types.VARCHAR, "VARCHAR", 1024, 0, false, false, false, false)
        );
    }
    
    @Override
    protected MergedResult createMergedResult() {
        return new MultipleLocalDataMergedResult(buildRows());
    }
    
    private Collection<List<Object>> buildRows() {
        // TODO Add display of overwrite after metadata save overwrite.
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        PersistRepositoryConfiguration repositoryConfiguration = instanceContext.getModeConfiguration().getRepository();
        return Collections.singleton(Arrays.asList(instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId(), instanceContext.getModeConfiguration().getType(),
                null == repositoryConfiguration ? "" : repositoryConfiguration.getType(),
                null == repositoryConfiguration ? "" : PropertiesConverter.convert(repositoryConfiguration.getProps())));
    }
}
