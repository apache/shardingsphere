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

package org.apache.shardingsphere.cdc.distsql.handler.query;

import org.apache.shardingsphere.cdc.distsql.statement.ShowStreamingRuleStatement;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show streaming rule executor.
 */
public final class ShowStreamingRuleExecutor implements QueryableRALExecutor<ShowStreamingRuleStatement> {
    
    private final PipelineProcessConfigurationPersistService persistService = new PipelineProcessConfigurationPersistService();
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingRuleStatement sqlStatement) {
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(persistService.load(new PipelineContextKey(InstanceType.PROXY), "STREAMING"));
        return Collections.singleton(new LocalDataQueryResultRow(getString(processConfig.getRead()), getString(processConfig.getWrite()), getString(processConfig.getStreamChannel())));
    }
    
    private String getString(final Object obj) {
        return null == obj ? "" : JsonUtils.toJsonString(obj);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("read", "write", "stream_channel");
    }
    
    @Override
    public Class<ShowStreamingRuleStatement> getType() {
        return ShowStreamingRuleStatement.class;
    }
}
