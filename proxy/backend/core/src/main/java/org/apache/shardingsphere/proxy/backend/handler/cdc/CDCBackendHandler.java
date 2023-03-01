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
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.StreamDataParameter;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckHolder;
import org.apache.shardingsphere.data.pipeline.cdc.core.connector.SocketSinkImporterConnector;
import org.apache.shardingsphere.data.pipeline.cdc.core.job.CDCJob;
import org.apache.shardingsphere.data.pipeline.cdc.exception.NotFindStreamDataSourceTableException;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.generator.DataRecordComparatorGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
     * Stream data.
     *
     * @param requestId request id
     * @param requestBody stream data request body
     * @param connectionContext connection context
     * @param channel channel
     * @return CDC response
     */
    public CDCResponse streamData(final String requestId, final StreamDataRequestBody requestBody, final CDCConnectionContext connectionContext, final Channel channel) {
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(requestBody.getDatabase());
        if (null == database) {
            return CDCResponseGenerator.failed(requestId, CDCResponseErrorCode.SERVER_ERROR, String.format("%s database is not exists", requestBody.getDatabase()));
        }
        Map<String, Collection<String>> schemaTableNameMap;
        Collection<String> tableNames;
        Set<String> schemaTableNames = new HashSet<>();
        if (database.getProtocolType().isSchemaAvailable()) {
            schemaTableNameMap = getSchemaTableMapWithSchema(database, requestBody.getSourceSchemaTablesList());
            // TODO if different schema have same table names, table name may be overwritten, because the table name at sharding rule not contain schema.
            tableNames = schemaTableNameMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            schemaTableNameMap.forEach((k, v) -> v.forEach(tableName -> schemaTableNames.add(k.isEmpty() ? tableName : String.join(".", k, tableName))));
        } else {
            schemaTableNames.addAll(getTableNamesWithoutSchema(database, requestBody.getSourceSchemaTablesList()));
            tableNames = schemaTableNames;
        }
        if (tableNames.isEmpty()) {
            throw new NotFindStreamDataSourceTableException();
        }
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return CDCResponseGenerator.failed(requestId, CDCResponseErrorCode.SERVER_ERROR, "Not find sharding rule");
        }
        Map<String, List<DataNode>> actualDataNodesMap = new HashMap<>();
        // TODO need support case-insensitive later
        for (String each : tableNames) {
            actualDataNodesMap.put(each, getActualDataNodes(shardingRule.get(), each));
        }
        boolean decodeWithTx = database.getProtocolType() instanceof OpenGaussDatabaseType;
        StreamDataParameter parameter = new StreamDataParameter(requestBody.getDatabase(), new LinkedList<>(schemaTableNames), requestBody.getFull(), actualDataNodesMap, decodeWithTx);
        String jobId = jobAPI.createJob(parameter, CDCSinkType.SOCKET, new Properties());
        connectionContext.setJobId(jobId);
        startStreaming(requestId, jobId, connectionContext, channel);
        return CDCResponseGenerator.succeedBuilder(requestId).setStreamDataResult(StreamDataResult.newBuilder().setStreamingId(jobId).build()).build();
    }
    
    private Map<String, Collection<String>> getSchemaTableMapWithSchema(final ShardingSphereDatabase database, final List<SchemaTable> schemaTables) {
        Map<String, Collection<String>> result = new HashMap<>();
        Collection<String> systemSchemas = database.getProtocolType().getSystemSchemas();
        Optional<SchemaTable> allSchemaTablesOptional = schemaTables.stream().filter(each -> "*".equals(each.getTable()) && "*".equals(each.getSchema())).findFirst();
        if (allSchemaTablesOptional.isPresent()) {
            for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
                if (systemSchemas.contains(entry.getKey())) {
                    continue;
                }
                entry.getValue().getAllTableNames().forEach(tableName -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(tableName));
            }
            return result;
        }
        for (SchemaTable each : schemaTables) {
            if ("*".equals(each.getSchema())) {
                for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
                    if (systemSchemas.contains(entry.getKey())) {
                        continue;
                    }
                    entry.getValue().getAllTableNames().stream().filter(tableName -> tableName.equals(each.getTable())).findFirst()
                            .ifPresent(tableName -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(tableName));
                }
            } else if ("*".equals(each.getTable())) {
                String schemaName = each.getSchema().isEmpty() ? getDefaultSchema(database.getProtocolType()) : each.getSchema();
                ShardingSphereSchema schema = database.getSchema(schemaName);
                if (null == schema) {
                    throw new SchemaNotFoundException(each.getSchema());
                }
                schema.getAllTableNames().forEach(tableName -> result.computeIfAbsent(schemaName, ignored -> new HashSet<>()).add(tableName));
            } else {
                result.computeIfAbsent(each.getSchema(), ignored -> new HashSet<>()).add(each.getTable());
            }
        }
        return result;
    }
    
    private String getDefaultSchema(final DatabaseType databaseType) {
        if (!(databaseType instanceof SchemaSupportedDatabaseType)) {
            return null;
        }
        return ((SchemaSupportedDatabaseType) databaseType).getDefaultSchema();
    }
    
    private Collection<String> getTableNamesWithoutSchema(final ShardingSphereDatabase database, final List<SchemaTable> schemaTables) {
        Optional<SchemaTable> allTablesOptional = schemaTables.stream().filter(each -> each.getTable().equals("*")).findFirst();
        Set<String> allTableNames = new HashSet<>(database.getSchema(database.getName()).getAllTableNames());
        return allTablesOptional.isPresent() ? allTableNames : schemaTables.stream().map(SchemaTable::getTable).collect(Collectors.toSet());
    }
    
    private List<DataNode> getActualDataNodes(final ShardingRule shardingRule, final String logicTableName) {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        // TODO support virtual data source name
        return tableRule.getActualDataNodes();
    }
    
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
     * Start streaming.
     *
     * @param requestId request id
     * @param jobId job id
     * @param channel channel
     * @param connectionContext connection context
     * @return CDC response
     */
    // TODO not return CDCResponse
    public CDCResponse startStreaming(final String requestId, final String jobId, final CDCConnectionContext connectionContext, final Channel channel) {
        CDCJobConfiguration cdcJobConfig = jobAPI.getJobConfiguration(jobId);
        if (null == cdcJobConfig) {
            return CDCResponseGenerator.failed(jobId, CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, String.format("the %s job config doesn't exist", jobId));
        }
        JobConfigurationPOJO jobConfigPOJO = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        // TODO, ensure that there is only one consumer at a time, job config disable may not be updated when the program is forced to close
        jobConfigPOJO.setDisabled(false);
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(cdcJobConfig.getDatabaseName());
        Comparator<DataRecord> dataRecordComparator = cdcJobConfig.isDecodeWithTX()
                ? DataRecordComparatorGenerator.generatorIncrementalComparator(database.getProtocolType())
                : null;
        CDCJob job = new CDCJob(new SocketSinkImporterConnector(channel, cdcJobConfig.getDatabaseName(), cdcJobConfig.getJobShardingCount(), cdcJobConfig.getSchemaTableNames(), dataRecordComparator));
        PipelineJobCenter.addJob(jobConfigPOJO.getJobName(), job);
        OneOffJobBootstrap oneOffJobBootstrap = new OneOffJobBootstrap(PipelineAPIFactory.getRegistryCenter(), job, jobConfigPOJO.toJobConfiguration());
        job.setJobBootstrap(oneOffJobBootstrap);
        oneOffJobBootstrap.execute();
        connectionContext.setJobId(jobId);
        return CDCResponseGenerator.succeedBuilder(requestId).build();
    }
    
    /**
     * Stop streaming.
     *
     * @param jobId job id
     */
    public void stopStreaming(final String jobId) {
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
     * Drop streaming.
     *
     * @param jobId job id.
     * @throws SQLException sql exception
     */
    public void dropStreaming(final String jobId) throws SQLException {
        jobAPI.rollback(jobId);
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
