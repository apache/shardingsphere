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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.RetryStreamingExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableInventoryCheckParameter;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.algorithm.keygen.snowflake.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CDC E2E IT.
 */
@PipelineE2ESettings(database = {
        @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/scenario/general/mysql.xml"),
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/scenario/general/postgresql.xml"),
        @PipelineE2EDatabaseSettings(type = "openGauss", scenarioFiles = "env/scenario/general/opengauss.xml")})
@Slf4j
class CDCE2EIT {
    
    private static final String CREATE_SHARDING_RULE_SQL = String.format("CREATE SHARDING TABLE RULE t_order("
            + "STORAGE_UNITS(%s,%s),"
            + "SHARDING_COLUMN=user_id,"
            + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='4')),"
            + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake'))"
            + ")", PipelineContainerComposer.DS_0, PipelineContainerComposer.DS_1);
    
    private static final String SOURCE_TABLE_NAME = "t_order";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertCDCDataImportSuccess(final PipelineTestParameter testParam) throws SQLException {
        // If run on NATIVE mode and database timezone is UTC, then: 1) set `e2e.timezone=UTC` in e2e-env.properties, 2) add this line of code in proxy Bootstrap main method.
        // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam)) {
            PipelineE2EDistSQLFacade distSQLFacade = new PipelineE2EDistSQLFacade(containerComposer, new CDCJobType());
            distSQLFacade.alterPipelineRule();
            for (String each : Arrays.asList(PipelineContainerComposer.DS_0, PipelineContainerComposer.DS_1)) {
                distSQLFacade.registerStorageUnit(each);
            }
            createOrderTableRule(containerComposer);
            distSQLFacade.createBroadcastRule("t_address");
            try (Connection connection = containerComposer.getProxyDataSource().getConnection()) {
                initSchemaAndTable(containerComposer, connection, 3);
            }
            PipelineDataSource sourceDataSource = new PipelineDataSource(containerComposer.generateShardingSphereDataSourceFromProxy(), containerComposer.getDatabaseType());
            List<Object[]> orderInsertData = PipelineCaseHelper.generateOrderInsertData(
                    containerComposer.getDatabaseType(), new AutoIncrementKeyGenerateAlgorithm(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            log.info("init data begin: {}", LocalDateTime.now());
            DataSourceExecuteUtils.execute(sourceDataSource, containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_NAME), orderInsertData);
            DataSourceExecuteUtils.execute(sourceDataSource, "INSERT INTO t_address(id, address_name) VALUES (?,?)", Arrays.asList(new Object[]{1, "a"}, new Object[]{2, "b"}));
            DataSourceExecuteUtils.execute(sourceDataSource, "INSERT INTO t_single(id) VALUES (?)", Arrays.asList(new Object[]{1}, new Object[]{2}, new Object[]{3}));
            log.info("init data end: {}", LocalDateTime.now());
            try (
                    Connection connection = DriverManager.getConnection(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false),
                            containerComposer.getUsername(), containerComposer.getPassword())) {
                initSchemaAndTable(containerComposer, connection, 0);
            }
            PipelineDataSource targetDataSource = createStandardDataSource(containerComposer, PipelineContainerComposer.DS_4);
            final CDCClient cdcClient = buildCDCClientAndStart(targetDataSource, containerComposer);
            Awaitility.waitAtMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !distSQLFacade.listJobIds().isEmpty());
            String jobId = distSQLFacade.listJobIds().get(0);
            distSQLFacade.waitJobIncrementalStageFinished(jobId);
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(containerComposer.getDatabaseType()).getDialectDatabaseMetaData();
            String tableName = dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() ? String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME) : SOURCE_TABLE_NAME;
            new E2EIncrementalTask(sourceDataSource, tableName, new SnowflakeKeyGenerateAlgorithm(), containerComposer.getDatabaseType(), 20).run();
            distSQLFacade.waitJobIncrementalStageFinished(jobId);
            for (int i = 1; i <= 4; i++) {
                int orderId = 10000 + i;
                containerComposer.proxyExecuteWithLog(String.format("INSERT INTO %s (order_id, user_id, status) VALUES (%d, %d, 'OK')", tableName, orderId, i), 0);
                containerComposer.assertRecordExists(targetDataSource, tableName, orderId);
            }
            QualifiedTable orderQualifiedTable = dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable()
                    ? new QualifiedTable(PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME)
                    : new QualifiedTable(null, SOURCE_TABLE_NAME);
            assertDataMatched(sourceDataSource, targetDataSource, orderQualifiedTable);
            assertDataMatched(sourceDataSource, targetDataSource, new QualifiedTable(null, "t_address"));
            assertDataMatched(sourceDataSource, targetDataSource, new QualifiedTable(null, "t_single"));
            cdcClient.close();
            Awaitility.waitAtMost(10L, TimeUnit.SECONDS).pollInterval(500L, TimeUnit.MILLISECONDS)
                    .until(() -> distSQLFacade.listJobs().stream().noneMatch(each -> Boolean.parseBoolean(each.get("active").toString())));
            distSQLFacade.drop(jobId);
            assertTrue(distSQLFacade.listJobs().isEmpty());
        }
    }
    
    private void createOrderTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(CREATE_SHARDING_RULE_SQL, 0);
        Awaitility.waitAtMost(20L, TimeUnit.SECONDS).pollInterval(2L, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW SHARDING TABLE RULE t_order").isEmpty());
    }
    
    private void initSchemaAndTable(final PipelineContainerComposer containerComposer, final Connection connection, final int seconds) throws SQLException {
        containerComposer.createSchema(connection, seconds);
        String sql = containerComposer.getExtraSQLCommand().getCreateTableOrder(SOURCE_TABLE_NAME);
        log.info("Create table sql: {}", sql);
        connection.createStatement().execute(sql);
        connection.createStatement().execute("CREATE TABLE t_address(id integer primary key, address_name varchar(255))");
        connection.createStatement().execute("CREATE TABLE t_single(id integer primary key)");
        containerComposer.sleepSeconds(seconds);
    }
    
    private PipelineDataSource createStandardDataSource(final PipelineContainerComposer containerComposer, final String storageUnitName) {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", containerComposer.getActualJdbcUrlTemplate(storageUnitName, false));
        poolProps.put("username", containerComposer.getUsername());
        poolProps.put("password", containerComposer.getPassword());
        return new PipelineDataSource(new StandardPipelineDataSourceConfiguration(poolProps));
    }
    
    private CDCClient buildCDCClientAndStart(final PipelineDataSource dataSource, final PipelineContainerComposer containerComposer) {
        DataSourceRecordConsumer recordConsumer = new DataSourceRecordConsumer(dataSource, containerComposer.getDatabaseType());
        CDCClient result = new CDCClient(new CDCClientConfiguration("localhost", containerComposer.getContainerComposer().getProxyCDCPort(), 10000));
        result.connect(recordConsumer, new RetryStreamingExceptionHandler(result, 5, 5000), (ctx, serverErrorResult) -> log.error("Server error: {}", serverErrorResult.getErrorMessage()));
        result.login(new CDCLoginParameter(ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD));
        // TODO add full=false test case later
        result.startStreaming(new StartStreamingParameter("sharding_db", Collections.singleton(SchemaTable.newBuilder().setTable("*").setSchema("*").build()), true));
        return result;
    }
    
    private void assertDataMatched(final PipelineDataSource sourceDataSource, final PipelineDataSource targetDataSource, final QualifiedTable qualifiedTable) {
        StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(targetDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(qualifiedTable.getSchemaName(), qualifiedTable.getTableName());
        List<PipelineColumnMetaData> uniqueKeys = Collections.singletonList(tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0)));
        ConsistencyCheckJobItemProgressContext progressContext = new ConsistencyCheckJobItemProgressContext("", 0, sourceDataSource.getDatabaseType().getType());
        progressContext.getTableCheckRangePositions().add(new TableCheckRangePosition(0, null, qualifiedTable.getTableName(),
                UniqueKeyIngestPosition.ofUnsplit(), UniqueKeyIngestPosition.ofUnsplit(), null));
        TableInventoryCheckParameter param = new TableInventoryCheckParameter("", sourceDataSource, targetDataSource, qualifiedTable, qualifiedTable,
                tableMetaData.getColumnNames(), uniqueKeys, null, progressContext);
        TableDataConsistencyChecker tableChecker = TypedSPILoader.getService(TableDataConsistencyChecker.class, "DATA_MATCH", new Properties());
        TableDataConsistencyCheckResult checkResult = tableChecker.buildTableInventoryChecker(param).checkSingleTableInventoryData();
        assertTrue(checkResult.isMatched());
    }
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
