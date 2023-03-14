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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaName;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.SingleTableInventoryDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm.DataMatchDataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.E2EIncrementalTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CDC E2E IT.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class CDCE2EIT {
    
    private static final String CREATE_SHARDING_RULE_SQL = String.format("CREATE SHARDING TABLE RULE t_order("
            + "STORAGE_UNITS(%s,%s),"
            + "SHARDING_COLUMN=user_id,"
            + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='4')),"
            + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake'))"
            + ")", PipelineContainerComposer.DS_0, PipelineContainerComposer.DS_1);
    
    private static final String SOURCE_TABLE_ORDER_NAME = "t_order";
    
    private final PipelineContainerComposer containerComposer;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public CDCE2EIT(final PipelineTestParameter testParam) {
        containerComposer = new PipelineContainerComposer(testParam, new CDCJobType());
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType mysqlDatabaseType = new MySQLDatabaseType();
        for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(mysqlDatabaseType)) {
            result.add(new PipelineTestParameter(mysqlDatabaseType, each, "env/scenario/general/mysql.xml"));
        }
        OpenGaussDatabaseType openGaussDatabaseType = new OpenGaussDatabaseType();
        for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(openGaussDatabaseType)) {
            result.add(new PipelineTestParameter(openGaussDatabaseType, each, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @After
    public void tearDown() {
        containerComposer.close();
    }
    
    @Test
    public void assertCDCDataImportSuccess() throws SQLException, InterruptedException {
        // make sure the program time zone same with the database server at CI.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for (String each : Arrays.asList(PipelineContainerComposer.DS_0, PipelineContainerComposer.DS_1)) {
            containerComposer.registerStorageUnit(each);
        }
        createOrderTableRule();
        try (Connection connection = containerComposer.getProxyDataSource().getConnection()) {
            initSchemaAndTable(connection, 2);
        }
        DataSource jdbcDataSource = containerComposer.generateShardingSphereDataSourceFromProxy();
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(containerComposer.getDatabaseType(), 20);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(jdbcDataSource, containerComposer.getExtraSQLCommand().getFullInsertOrder(SOURCE_TABLE_ORDER_NAME), dataPair.getLeft());
        log.info("init data end: {}", LocalDateTime.now());
        try (
                Connection connection = DriverManager.getConnection(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false),
                        containerComposer.getUsername(), containerComposer.getPassword())) {
            initSchemaAndTable(connection, 0);
        }
        startCDCClient();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW STREAMING LIST").isEmpty());
        String jobId = containerComposer.queryForListWithLog("SHOW STREAMING LIST").get(0).get("id").toString();
        containerComposer.waitIncrementTaskFinished(String.format("SHOW STREAMING STATUS '%s'", jobId));
        containerComposer.startIncrementTask(new E2EIncrementalTask(jdbcDataSource, SOURCE_TABLE_ORDER_NAME, new SnowflakeKeyGenerateAlgorithm(), containerComposer.getDatabaseType(), 20));
        containerComposer.getIncreaseTaskThread().join(10000L);
        List<Map<String, Object>> actualProxyList;
        try (Connection connection = jdbcDataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", getOrderTableNameWithSchema()));
            actualProxyList = containerComposer.transformResultSetToList(resultSet);
        }
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).until(() -> listOrderRecords(getOrderTableNameWithSchema()).size() == actualProxyList.size());
        SchemaTableName schemaTableName = containerComposer.getDatabaseType().isSchemaAvailable()
                ? new SchemaTableName(new SchemaName(PipelineContainerComposer.SCHEMA_NAME), new TableName(SOURCE_TABLE_ORDER_NAME))
                : new SchemaTableName(new SchemaName(null), new TableName(SOURCE_TABLE_ORDER_NAME));
        PipelineDataSourceWrapper targetDataSource = new PipelineDataSourceWrapper(StorageContainerUtil.generateDataSource(
                containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false),
                containerComposer.getUsername(), containerComposer.getPassword()), containerComposer.getDatabaseType());
        PipelineDataSourceWrapper sourceDataSource = new PipelineDataSourceWrapper(jdbcDataSource, containerComposer.getDatabaseType());
        StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(targetDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(PipelineContainerComposer.SCHEMA_NAME, "t_order");
        PipelineColumnMetaData primaryKeyMetaData = tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0));
        ConsistencyCheckJobItemProgressContext progressContext = new ConsistencyCheckJobItemProgressContext("", 0);
        SingleTableInventoryDataConsistencyChecker checker = new SingleTableInventoryDataConsistencyChecker("", sourceDataSource, targetDataSource, schemaTableName, schemaTableName,
                tableMetaData.getColumnNames(), primaryKeyMetaData, null, progressContext);
        DataConsistencyCheckResult checkResult = checker.check(new DataMatchDataConsistencyCalculateAlgorithm());
        assertTrue(checkResult.isMatched());
    }
    
    private void createOrderTableRule() throws SQLException {
        containerComposer.proxyExecuteWithLog(CREATE_SHARDING_RULE_SQL, 2);
    }
    
    private void initSchemaAndTable(final Connection connection, final int sleepSeconds) throws SQLException {
        containerComposer.createSchema(connection, sleepSeconds);
        String sql = containerComposer.getExtraSQLCommand().getCreateTableOrder(SOURCE_TABLE_ORDER_NAME);
        log.info("create table sql: {}", sql);
        connection.createStatement().execute(sql);
        if (sleepSeconds > 0) {
            ThreadUtil.sleep(sleepSeconds, TimeUnit.SECONDS);
        }
    }
    
    private void startCDCClient() {
        ImportDataSourceParameter importDataSourceParam = new ImportDataSourceParameter(containerComposer.appendExtraParameter(
                containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false, 0)), containerComposer.getUsername(), containerComposer.getPassword());
        StartCDCClientParameter parameter = new StartCDCClientParameter(importDataSourceParam);
        parameter.setAddress("localhost");
        parameter.setPort(containerComposer.getContainerComposer().getProxyCDCPort());
        parameter.setUsername(ProxyContainerConstants.USERNAME);
        parameter.setPassword(ProxyContainerConstants.PASSWORD);
        parameter.setDatabase("sharding_db");
        // TODO add full=false test case later
        parameter.setFull(true);
        String schema = containerComposer.getDatabaseType().isSchemaAvailable() ? "test" : "";
        parameter.setSchemaTables(Collections.singletonList(SchemaTable.newBuilder().setTable(SOURCE_TABLE_ORDER_NAME).setSchema(schema).build()));
        parameter.setDatabaseType(containerComposer.getDatabaseType().getType());
        CompletableFuture.runAsync(() -> new CDCClient(parameter).start(), executor).whenComplete((unused, throwable) -> {
            if (null != throwable) {
                log.error("cdc client sync failed, ", throwable);
            }
        });
    }
    
    private List<Map<String, Object>> listOrderRecords(final String tableNameWithSchema) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(
                        containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, false), containerComposer.getUsername(), containerComposer.getPassword())) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", tableNameWithSchema));
            return containerComposer.transformResultSetToList(resultSet);
        }
    }
    
    private String getOrderTableNameWithSchema() {
        return containerComposer.getDatabaseType().isSchemaAvailable() ? String.join(".", PipelineContainerComposer.SCHEMA_NAME, SOURCE_TABLE_ORDER_NAME) : SOURCE_TABLE_ORDER_NAME;
    }
}
