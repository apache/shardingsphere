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

package org.apache.shardingsphere.proxy.backend.handler.cdc;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.TableName;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CDC backend handler.
 */
@Slf4j
public final class CDCBackendHandler {
    
    /**
     * Create subscription.
     *
     * @param request CDC request
     * @return CDC response
     */
    public CDCResponse createSubscription(final CDCRequest request) {
        CreateSubscriptionRequest subscriptionRequest = request.getCreateSubscription();
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(subscriptionRequest.getDatabase());
        if (null == database) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, String.format("%s database is not exists", subscriptionRequest.getDatabase()));
        }
        List<String> tableNames = new LinkedList<>();
        for (TableName each : subscriptionRequest.getTableNamesList()) {
            tableNames.add(Strings.isNullOrEmpty(each.getSchema()) ? each.getName() : String.join(".", each.getSchema(), each.getName()));
        }
        Optional<ShardingRule> rule = database.getRuleMetaData().getRules().stream().filter(each -> each instanceof ShardingRule).map(each -> (ShardingRule) each).findFirst();
        if (!rule.isPresent()) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, "Not find sharding rule");
        }
        Map<String, List<DataNode>> actualDataNodesMap = new HashMap<>();
        for (String each : tableNames) {
            actualDataNodesMap.put(each, getActualDataNodes(rule.get(), each));
        }
        CreateSubscriptionJobParameter parameter = new CreateSubscriptionJobParameter(subscriptionRequest.getDatabase(), tableNames, subscriptionRequest.getSubscriptionName(),
                subscriptionRequest.getSubscriptionMode().name(), actualDataNodesMap);
        CDCJobAPIFactory.getInstance().createJobAndStart(parameter);
        return CDCResponseGenerator.succeedBuilder(request.getRequestId()).build();
    }
    
    private List<DataNode> getActualDataNodes(final ShardingRule shardingRule, final String logicTableName) {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        // TODO support virtual data source name
        return tableRule.getActualDataNodes();
    }
}
