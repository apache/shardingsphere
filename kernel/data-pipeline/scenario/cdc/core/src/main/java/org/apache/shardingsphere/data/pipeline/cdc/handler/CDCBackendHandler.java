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
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.StreamDataParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckId;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporterManager;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.CDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCExceptionWrapper;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCServerException;
import org.apache.shardingsphere.data.pipeline.cdc.exception.NotFindStreamDataSourceTableException;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.ResponseCase;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.apache.shardingsphere.data.pipeline.cdc.util.CDCDataNodeUtils;
import org.apache.shardingsphere.data.pipeline.cdc.util.CDCSchemaTableUtils;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * Get database name by job ID.
     *
     * @param jobId job ID
     * @return database
     */
    public String getDatabaseNameByJobId(final String jobId) {
        return jobAPI.getJobConfiguration(jobId).getDatabaseName();
    }
    
    /**
     * Stream data.
     *
     * @param requestId request ID
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
        Map<String, List<DataNode>> actualDataNodesMap = CDCDataNodeUtils.buildDataNodesMap(database, tableNames);
        ShardingSpherePreconditions.checkState(!actualDataNodesMap.isEmpty(), () -> new PipelineInvalidParameterException(String.format("Not find table %s", tableNames)));
        boolean decodeWithTx = database.getProtocolType() instanceof OpenGaussDatabaseType;
        StreamDataParameter parameter = new StreamDataParameter(requestBody.getDatabase(), new LinkedList<>(schemaTableNames), requestBody.getFull(), actualDataNodesMap, decodeWithTx);
        String jobId = jobAPI.createJob(parameter, CDCSinkType.SOCKET, new Properties());
        connectionContext.setJobId(jobId);
        startStreaming(jobId, connectionContext, channel);
        return CDCResponseUtils.succeed(requestId, ResponseCase.STREAM_DATA_RESULT, StreamDataResult.newBuilder().setStreamingId(jobId).build());
    }
    
    /**
     * Start streaming.
     *
     * @param jobId job ID
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
        jobAPI.startJob(jobId, new CDCSocketSink(channel, database, cdcJobConfig.getSchemaTableNames()));
        connectionContext.setJobId(jobId);
    }
    
    /**
     * Stop streaming.
     *
     * @param jobId job ID
     * @param channelId channel ID
     */
    public void stopStreaming(final String jobId, final ChannelId channelId) {
        if (Strings.isNullOrEmpty(jobId)) {
            log.warn("job id is null or empty, ignored");
            return;
        }
        CDCJob job = (CDCJob) PipelineJobCenter.getJob(jobId);
        if (null == job) {
            return;
        }
        if (job.getSink().identifierMatched(channelId)) {
            log.info("close CDC job, channel id: {}", channelId);
            PipelineJobCenter.stop(jobId);
            jobAPI.updateJobConfigurationDisabled(jobId, true);
        }
    }
    
    /**
     * Drop streaming.
     *
     * @param jobId job ID
     */
    public void dropStreaming(final String jobId) {
        jobAPI.stopAndDrop(jobId);
    }
    
    /**
     * Process ack.
     *
     * @param requestBody request body
     */
    public void processAck(final AckStreamingRequestBody requestBody) {
        CDCAckId ackId = CDCAckId.unmarshal(requestBody.getAckId());
        CDCImporter importer = CDCImporterManager.getImporter(ackId.getImporterId());
        if (null == importer) {
            log.warn("Could not find importer, ack id: {}", ackId.marshal());
            return;
        }
        importer.ack(ackId.marshal());
    }
}
