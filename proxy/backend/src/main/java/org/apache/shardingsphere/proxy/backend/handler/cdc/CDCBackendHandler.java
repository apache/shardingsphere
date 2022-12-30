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
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.connector.CDCImporterConnector;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.TableName;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CreateSubscriptionResult;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
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
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
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
        if (jobAPI.createJob(parameter)) {
            return CDCResponseGenerator.succeedBuilder(request.getRequestId()).setCreateSubscriptionResult(CreateSubscriptionResult.newBuilder()
                    .setSubscriptionName(subscriptionRequest.getSubscriptionName()).setExisting(false).build()).build();
        } else {
            return CDCResponseGenerator.succeedBuilder(request.getRequestId()).setCreateSubscriptionResult(CreateSubscriptionResult.newBuilder()
                    .setSubscriptionName(subscriptionRequest.getSubscriptionName()).setExisting(true).build()).build();
        }
    }
    
    private List<DataNode> getActualDataNodes(final ShardingRule shardingRule, final String logicTableName) {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        // TODO support virtual data source name
        return tableRule.getActualDataNodes();
    }
    
    /**
     * Start subscription.
     *
     * @param request request
     * @param channel channel
     * @param connectionContext connection context
     * @return CDC response
     */
    public CDCResponse startSubscription(final CDCRequest request, final Channel channel, final CDCConnectionContext connectionContext) {
        StartSubscriptionRequest startSubscriptionRequest = request.getStartSubscription();
        String jobId = jobAPI.marshalJobId(new CDCJobId(startSubscriptionRequest.getDatabase(), startSubscriptionRequest.getSubscriptionName()));
        JobConfigurationPOJO jobConfigPOJO = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        if (null == jobConfigPOJO) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, String.format("the %s job config doesn't exist",
                    startSubscriptionRequest.getSubscriptionName()));
        }
        if (!jobConfigPOJO.isDisabled()) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, String.format("the %s is being used", startSubscriptionRequest.getSubscriptionName()));
        }
        jobConfigPOJO.setDisabled(false);
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
        CDCJob job = new CDCJob(new CDCImporterConnector(channel));
        PipelineJobCenter.addJob(jobConfigPOJO.getJobName(), job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfigPOJO.toJobConfiguration());
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
        connectionContext.setStatus(CDCConnectionStatus.SUBSCRIBED);
        connectionContext.setJobId(jobId);
        return CDCResponseGenerator.succeedBuilder(request.getRequestId()).build();
    }
    
    /**
     * Stop subscription.
     *
     * @param jobId job id
     */
    public void stopSubscription(final String jobId) {
        PipelineJobCenter.stop(jobId);
        JobConfigurationPOJO jobConfig = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        jobConfig.setDisabled(true);
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfig);
    }
}
