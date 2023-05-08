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

package org.apache.shardingsphere.data.pipeline.cdc.handler;

import com.google.common.base.Strings;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.StreamDataParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.context.job.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckHolder;
import org.apache.shardingsphere.data.pipeline.cdc.core.connector.SocketSinkImporterConnector;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCExceptionWrapper;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCServerException;
import org.apache.shardingsphere.data.pipeline.cdc.exception.NotFindStreamDataSourceTableException;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.generator.DataRecordComparatorGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.apache.shardingsphere.data.pipeline.cdc.util.CDCSchemaTableUtils;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.NoAnyRuleExistsException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDC backend handler.
 */
@Slf4j
public final class CDCBackendHandler {
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    /**
     * Get database name by job id.
     *
     * @param jobId job id
     * @return database
     */
    public String getDatabaseNameByJobId(final String jobId) {
        return jobAPI.getJobConfiguration(jobId).getDatabaseName();
    }
    
    /**
     * Stream data.
     *
     * @param requestId request id
     * @param requestBody stream data request body
     * @param connectionContext connection context
     * @param channel channel
     * @return CDC response
     */
    public CDCResponse streamData(final String requestId, final StreamDataRequestBody requestBody, final CDCConnectionContext connectionContext, final Channel channel) {
        ShardingSphereDatabase database = PipelineContextManager.getProxyContext().getContextManager().getMetaDataContexts().getMetaData().getDatabase(requestBody.getDatabase());
        ShardingSpherePreconditions.checkNotNull(database, () -> new CDCExceptionWrapper(requestId, new CDCServerException(String.format("%s database is not exists", requestBody.getDatabase()))));
        Map<String, Set<String>> schemaTableNameMap;
        Collection<String> tableNames;
        Set<String> schemaTableNames = new HashSet<>();
        if (database.getProtocolType().isSchemaAvailable()) {
            schemaTableNameMap = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, requestBody.getSourceSchemaTableList());
            // TODO if different schema have same table names, table name may be overwritten, because the table name at sharding rule not contain schema.
            tableNames = schemaTableNameMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            schemaTableNameMap.forEach((k, v) -> v.forEach(tableName -> schemaTableNames.add(k.isEmpty() ? tableName : String.join(".", k, tableName))));
        } else {
            schemaTableNames.addAll(CDCSchemaTableUtils.parseTableExpressionWithoutSchema(database, requestBody.getSourceSchemaTableList().stream().map(SchemaTable::getTable)
                    .collect(Collectors.toList())));
            tableNames = schemaTableNames;
        }
        ShardingSpherePreconditions.checkState(!tableNames.isEmpty(), () -> new CDCExceptionWrapper(requestId, new NotFindStreamDataSourceTableException()));
        Map<String, List<DataNode>> actualDataNodesMap = buildDataNodesMap(database, tableNames);
        ShardingSpherePreconditions.checkState(!actualDataNodesMap.isEmpty(), () -> new PipelineInvalidParameterException(String.format("Not find table %s", tableNames)));
        boolean decodeWithTx = database.getProtocolType() instanceof OpenGaussDatabaseType;
        StreamDataParameter parameter = new StreamDataParameter(requestBody.getDatabase(), new LinkedList<>(schemaTableNames), requestBody.getFull(), actualDataNodesMap, decodeWithTx);
        String jobId = jobAPI.createJob(parameter, CDCSinkType.SOCKET, new Properties());
        connectionContext.setJobId(jobId);
        startStreaming(jobId, connectionContext, channel);
        return CDCResponseGenerator.succeedBuilder(requestId).setStreamDataResult(StreamDataResult.newBuilder().setStreamingId(jobId).build()).build();
    }
    
    private Map<String, List<DataNode>> buildDataNodesMap(final ShardingSphereDatabase database, final Collection<String> tableNames) {
        Map<String, List<DataNode>> result = new HashMap<>();
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        Optional<SingleRule> singleRule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        if (!shardingRule.isPresent() && !singleRule.isPresent()) {
            throw new NoAnyRuleExistsException(database.getName());
        }
        // TODO support virtual data source name
        for (String each : tableNames) {
            if (singleRule.isPresent() && singleRule.get().getAllDataNodes().containsKey(each)) {
                result.put(each, new ArrayList<>(singleRule.get().getAllDataNodes().get(each)));
                continue;
            }
            shardingRule.flatMap(value -> value.findTableRule(each)).ifPresent(rule -> result.put(each, rule.getActualDataNodes()));
        }
        return result;
    }
    
    /**
     * Start streaming.
     *
     * @param jobId job id
     * @param channel channel
     * @param connectionContext connection context
     */
    public void startStreaming(final String jobId, final CDCConnectionContext connectionContext, final Channel channel) {
        CDCJobConfiguration cdcJobConfig = jobAPI.getJobConfiguration(jobId);
        ShardingSpherePreconditions.checkNotNull(cdcJobConfig, () -> new PipelineJobNotFoundException(jobId));
        if (PipelineJobCenter.isJobExisting(jobId)) {
            PipelineJobCenter.stop(jobId);
        }
        ShardingSphereDatabase database = PipelineContextManager.getProxyContext().getContextManager().getMetaDataContexts().getMetaData().getDatabase(cdcJobConfig.getDatabaseName());
        Comparator<DataRecord> dataRecordComparator = cdcJobConfig.isDecodeWithTX()
                ? DataRecordComparatorGenerator.generatorIncrementalComparator(database.getProtocolType())
                : null;
        jobAPI.startJob(jobId, new SocketSinkImporterConnector(channel, database, cdcJobConfig.getJobShardingCount(), cdcJobConfig.getSchemaTableNames(), dataRecordComparator));
        connectionContext.setJobId(jobId);
    }
    
    /**
     * Stop streaming.
     *
     * @param jobId job id
     * @param channelId channel id
     */
    public void stopStreaming(final String jobId, final ChannelId channelId) {
        if (Strings.isNullOrEmpty(jobId)) {
            log.warn("job id is null or empty, ignored");
            return;
        }
        List<Integer> shardingItems = new ArrayList<>(PipelineJobCenter.getShardingItems(jobId));
        if (0 == shardingItems.size()) {
            return;
        }
        Optional<PipelineJobItemContext> jobItemContext = PipelineJobCenter.getJobItemContext(jobId, shardingItems.get(0));
        if (!jobItemContext.isPresent()) {
            return;
        }
        CDCJobItemContext cdcJobItemContext = (CDCJobItemContext) jobItemContext.get();
        if (cdcJobItemContext.getImporterConnector() instanceof SocketSinkImporterConnector) {
            Channel channel = (Channel) cdcJobItemContext.getImporterConnector().getConnector();
            if (channelId.equals(channel.id())) {
                log.info("close CDC job, channel id: {}", channelId);
                PipelineJobCenter.stop(jobId);
                jobAPI.updateJobConfigurationDisabled(jobId, true);
            }
        }
    }
    
    /**
     * Rollback streaming.
     *
     * @param jobId job id.
     * @throws SQLException sql exception
     */
    public void rollbackStreaming(final String jobId) throws SQLException {
        jobAPI.rollback(jobId);
    }
    
    /**
     * Commit streaming.
     *
     * @param jobId job id.
     * @throws SQLException sql exception
     */
    public void commitStreaming(final String jobId) throws SQLException {
        jobAPI.commit(jobId);
    }
    
    /**
     * Process ack.
     *
     * @param requestBody request body
     */
    public void processAck(final AckStreamingRequestBody requestBody) {
        CDCAckHolder.getInstance().ack(requestBody.getAckId());
    }
}
