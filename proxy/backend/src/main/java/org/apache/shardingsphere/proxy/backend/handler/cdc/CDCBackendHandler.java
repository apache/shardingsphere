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
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckHolder;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.connector.CDCImporterConnector;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.generator.DataRecordComparatorGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckRequest;
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

import java.sql.SQLException;
import java.util.Comparator;
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
        CreateSubscriptionRequest createSubscription = request.getCreateSubscription();
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(createSubscription.getDatabase());
        if (null == database) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, String.format("%s database is not exists", createSubscription.getDatabase()));
        }
        List<String> tableNames = new LinkedList<>();
        for (TableName each : createSubscription.getTableNamesList()) {
            tableNames.add(Strings.isNullOrEmpty(each.getSchema()) ? each.getName() : String.join(".", each.getSchema(), each.getName()));
        }
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, "Not find sharding rule");
        }
        Map<String, List<DataNode>> actualDataNodesMap = new HashMap<>();
        // TODO need support case-insensitive later
        for (TableName each : createSubscription.getTableNamesList()) {
            actualDataNodesMap.put(each.getName(), getActualDataNodes(shardingRule.get(), each.getName()));
        }
        CreateSubscriptionJobParameter parameter = new CreateSubscriptionJobParameter(createSubscription.getDatabase(), tableNames, createSubscription.getSubscriptionName(),
                createSubscription.getSubscriptionMode().name(), actualDataNodesMap, createSubscription.getIncrementalGlobalOrderly());
        if (jobAPI.createJob(parameter)) {
            return CDCResponseGenerator.succeedBuilder(request.getRequestId()).setCreateSubscriptionResult(CreateSubscriptionResult.newBuilder()
                    .setSubscriptionName(createSubscription.getSubscriptionName()).setExisting(false).build()).build();
        } else {
            return CDCResponseGenerator.succeedBuilder(request.getRequestId()).setCreateSubscriptionResult(CreateSubscriptionResult.newBuilder()
                    .setSubscriptionName(createSubscription.getSubscriptionName()).setExisting(true).build()).build();
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
        CDCJobConfiguration cdcJobConfig = (CDCJobConfiguration) jobAPI.getJobConfiguration(jobId);
        if (null == cdcJobConfig) {
            return CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, String.format("the %s job config doesn't exist",
                    startSubscriptionRequest.getSubscriptionName()));
        }
        JobConfigurationPOJO jobConfigPOJO = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        // TODO, ensure that there is only one consumer at a time, job config disable may not be updated when the program is forced to close
        jobConfigPOJO.setDisabled(false);
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(cdcJobConfig.getDatabase());
        Comparator<DataRecord> dataRecordComparator = cdcJobConfig.isDecodeWithTX()
                ? DataRecordComparatorGenerator.generatorIncrementalComparator(database.getProtocolType())
                : null;
        CDCJob job = new CDCJob(new CDCImporterConnector(channel, cdcJobConfig.getDatabase(), cdcJobConfig.getJobShardingCount(), cdcJobConfig.getTableNames(), dataRecordComparator));
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
        if (Strings.isNullOrEmpty(jobId)) {
            log.warn("job id is null or empty, ignored");
            return;
        }
        PipelineJobCenter.stop(jobId);
        JobConfigurationPOJO jobConfig = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        jobConfig.setDisabled(true);
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfig);
    }
    
    /**
     * Drop subscription.
     *
     * @param jobId job id.
     * @throws SQLException sql exception
     */
    public void dropSubscription(final String jobId) throws SQLException {
        jobAPI.rollback(jobId);
    }
    
    /**
     * Process ack.
     *
     * @param ackRequest ack request
     */
    public void processAck(final AckRequest ackRequest) {
        CDCAckHolder.getInstance().ack(ackRequest.getAckId());
    }
}
