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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.LoggerExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataMatchDataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtils;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
    void assertCDCDataImportSuccess(final PipelineTestParameter testParam) throws SQLException, InterruptedException {
        if (TimeZone.getDefault() != TimeZone.getTimeZone("UTC") && PipelineEnvTypeEnum.DOCKER == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            // make sure the time zone of locally running program same with the database server at CI.
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        }
        try (PipelineContainerComposer containerComposer = new PipelineContainerComposer(testParam, new CDCJobType())) {
            for (String each : Arrays.asList(PipelineContainerComposer.DS_0, PipelineContainerComposer.DS_1)) {
                containerComposer.registerStorageUnit(each);
            }
            createOrderTableRule(containerComposer);
            try (Connection connection = containerComposer.getProxyDataSource().getConnection()) {
                initSchemaAndTable(containerComposer, connection, 3);
            }
            DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
            Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(containerComposer.getDatabaseType(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
            log.info("init data begin: {}", LocalDateTime.now());
            DataSourceExecuteUtils.execute(jdbcDataSource, containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_NAME), dataPair.getLeft());
            DataSourceExecuteUtils.execute(jdbcDataSource, "INSERT INTO t_address(id, address_name) VALUES (?,?)", Arrays.asList(new Object[]{1, "a"}, new Object[]{2, "b"}));
            log.info("init data end: {}", LocalDateTime.now());
            try (
                    Connection connection = DriverManager.getConnection(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false),
                            containerComposer.getUsername(), containerComposer.getPassword())) {
                initSchemaAndTable(containerComposer, connection, 0);
            }
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(containerComposer.getDatabaseType()).getDialectDatabaseMetaData();
            startCDCClient(containerComposer, dialectDatabaseMetaData);
            Awaitility.await().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW STREAMING LIST").isEmpty());
            String jobId = containerComposer.queryForListWithLog("SHOW STREAMING LIST").get(0).get("id").toString();
            containerComposer.waitIncrementTaskFinished(String.format("SHOW STREAMING STATUS '%s'", jobId));
            String tableName = dialectDatabaseMetaData.isSchemaAvailable() ? String.join(".", "test", SOURCE_TABLE_NAME) : SOURCE_TABLE_NAME;
            containerComposer.startIncrementTask(new E2EIncrementalTask(jdbcDataSource, tableName, new SnowflakeKeyGenerateAlgorithm(), containerComposer.getDatabaseType(), 20));
            containerComposer.getIncreaseTaskThread().join(10000L);
            List<Map<String, Object>> actualProxyList;
            try (Connection connection = jdbcDataSource.getConnection()) {
                ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", getOrderTableNameWithSchema(dialectDatabaseMetaData)));
                actualProxyList = containerComposer.transformResultSetToList(resultSet);
            }
            Awaitility.await().atMost(20L, TimeUnit.SECONDS).pollInterval(2L, TimeUnit.SECONDS)
                    .until(() -> listOrderRecords(containerComposer, getOrderTableNameWithSchema(dialectDatabaseMetaData)).size() == actualProxyList.size());
            SchemaTableName orderSchemaTableName = dialectDatabaseMetaData.isSchemaAvailable()
                    ? new SchemaTableName(new SchemaName(PipelineContainerComposer.SCHEMA_NAME), new TableName(SOURCE_TABLE_NAME))
                    : new SchemaTableName(new SchemaName(null), new TableName(SOURCE_TABLE_NAME));
            PipelineDataSourceWrapper sourceDataSource = new PipelineDataSourceWrapper(jdbcDataSource, containerComposer.getDatabaseType());
            PipelineDataSourceWrapper targetDataSource = new PipelineDataSourceWrapper(createStandardDataSource(containerComposer, PipelineContainerComposer.DS_4),
                    containerComposer.getDatabaseType());
            assertDataMatched(sourceDataSource, targetDataSource, orderSchemaTableName);
            assertDataMatched(sourceDataSource, targetDataSource, new SchemaTableName(new SchemaName(null), new TableName("t_address")));
            containerComposer.proxyExecuteWithLog(String.format("DROP STREAMING '%s'", jobId), 0);
            assertTrue(containerComposer.queryForListWithLog("SHOW STREAMING LIST").isEmpty());
        }
    }
    
    private void createOrderTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(CREATE_SHARDING_RULE_SQL, 0);
        Awaitility.await().atMost(20L, TimeUnit.SECONDS).pollInterval(2L, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW SHARDING TABLE RULE t_order").isEmpty());
    }
    
    private void initSchemaAndTable(final PipelineContainerComposer containerComposer, final Connection connection, final int sleepSeconds) throws SQLException {
        containerComposer.createSchema(connection, sleepSeconds);
        String sql = containerComposer.getExtraSQLCommand().getCreateTableOrder(SOURCE_TABLE_NAME);
        log.info("Create table sql: {}", sql);
        connection.createStatement().execute(sql);
        connection.createStatement().execute("CREATE TABLE t_address(id integer primary key, address_name varchar(255))");
        if (sleepSeconds > 0) {
            Awaitility.await().pollDelay(sleepSeconds, TimeUnit.SECONDS).until(() -> true);
        }
    }
    
    private DataSource createStandardDataSource(final PipelineContainerComposer containerComposer, final String storageUnitName) {
        return PipelineDataSourceFactory.newInstance(new StandardPipelineDataSourceConfiguration(containerComposer.getActualJdbcUrlTemplate(storageUnitName, false),
                containerComposer.getUsername(), containerComposer.getPassword()));
    }
    
    private void startCDCClient(final PipelineContainerComposer containerComposer, final DialectDatabaseMetaData dialectDatabaseMetaData) {
        DataSource dataSource = createStandardDataSource(containerComposer, PipelineContainerComposer.DS_4);
        DataSourceRecordConsumer recordConsumer = new DataSourceRecordConsumer(dataSource, containerComposer.getDatabaseType());
        CDCClientConfiguration cdcConfig = new CDCClientConfiguration("localhost", containerComposer.getContainerComposer().getProxyCDCPort(), recordConsumer, new LoggerExceptionHandler());
        String schema = dialectDatabaseMetaData.isSchemaAvailable() ? "test" : "";
        CDCClient cdcClient = new CDCClient(cdcConfig);
        cdcClient.connect();
        cdcClient.login(new CDCLoginParameter(ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD));
        // TODO add full=false test case later
        cdcClient.startStreaming(new StartStreamingParameter("sharding_db", new HashSet<>(Arrays.asList(SchemaTable.newBuilder().setTable(SOURCE_TABLE_NAME).setSchema(schema).build(),
                SchemaTable.newBuilder().setTable("t_address").build())), true));
    }
    
    private List<Map<String, Object>> listOrderRecords(final PipelineContainerComposer containerComposer, final String tableNameWithSchema) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(
                        containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false), containerComposer.getUsername(), containerComposer.getPassword())) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", tableNameWithSchema));
            return containerComposer.transformResultSetToList(resultSet);
        }
    }
    
    private String getOrderTableNameWithSchema(final DialectDatabaseMetaData dialectDatabaseMetaData) {
        return dialectDatabaseMetaData.isSchemaAvailable() ? String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_NAME) : SOURCE_TABLE_NAME;
    }
    
    private void assertDataMatched(final PipelineDataSourceWrapper sourceDataSource, final PipelineDataSourceWrapper targetDataSource, final SchemaTableName schemaTableName) {
        StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(targetDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(schemaTableName.getSchemaName().getOriginal(), schemaTableName.getTableName().getOriginal());
        PipelineColumnMetaData primaryKeyMetaData = tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0));
        ConsistencyCheckJobItemProgressContext progressContext = new ConsistencyCheckJobItemProgressContext("", 0);
        SingleTableInventoryDataConsistencyChecker checker = new SingleTableInventoryDataConsistencyChecker("", sourceDataSource, targetDataSource, schemaTableName, schemaTableName,
                tableMetaData.getColumnNames(), primaryKeyMetaData, null, progressContext);
        DataConsistencyCheckResult checkResult = checker.check(new DataMatchDataConsistencyCalculateAlgorithm());
        assertTrue(checkResult.isMatched());
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(TypedSPILoader.getService(DatabaseType.class, "MySQL"),
                TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    }
}
