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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.MySQLIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.PostgreSQLIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testcontainers.shaded.org.awaitility.Awaitility;

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL CDC E2E IT.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class CDCE2EIT extends PipelineBaseE2EIT {
    
    private static final String CREATE_SHARDING_RULE_SQL = String.format("CREATE SHARDING TABLE RULE t_order("
            + "STORAGE_UNITS(%s,%s),"
            + "SHARDING_COLUMN=user_id,"
            + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='4')),"
            + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake'))"
            + ")", DS_0, DS_1);
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public CDCE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        MySQLDatabaseType mysqlDatabaseType = new MySQLDatabaseType();
        for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(mysqlDatabaseType)) {
            result.add(new PipelineTestParameter(mysqlDatabaseType, each, "env/scenario/general/mysql.xml"));
        }
        OpenGaussDatabaseType openGaussDatabaseType = new OpenGaussDatabaseType();
        for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(openGaussDatabaseType)) {
            result.add(new PipelineTestParameter(openGaussDatabaseType, each, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @Test
    public void assertCDCDataImportSuccess() throws SQLException, InterruptedException {
        // make sure the program time zone same with the database server at CI.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        initEnvironment(getDatabaseType(), new CDCJobType());
        for (String each : Arrays.asList(DS_0, DS_1)) {
            registerStorageUnit(each);
        }
        createOrderTableRule();
        try (Connection connection = getProxyDataSource().getConnection()) {
            initSchemaAndTable(connection);
        }
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(getDatabaseType(), 20);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getProxyDataSource(), getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        log.info("init data end: {}", LocalDateTime.now());
        try (Connection connection = DriverManager.getConnection(getActualJdbcUrlTemplate(DS_4, false), getUsername(), getPassword())) {
            initSchemaAndTable(connection);
        }
        startCDCClient();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> !queryForListWithLog("SHOW STREAMING LIST").isEmpty());
        if (getDatabaseType() instanceof MySQLDatabaseType) {
            startIncrementTask(new MySQLIncrementTask(getProxyDataSource(), getSourceTableOrderName(), new SnowflakeKeyGenerateAlgorithm(), 20));
        } else {
            startIncrementTask(new PostgreSQLIncrementTask(getProxyDataSource(), PipelineBaseE2EIT.SCHEMA_NAME, getSourceTableOrderName(), 20));
        }
        getIncreaseTaskThread().join(10000);
        List<Map<String, Object>> actualProxyList;
        try (Connection connection = getProxyDataSource().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", getOrderTableNameWithSchema()));
            actualProxyList = transformResultSetToList(resultSet);
        }
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).until(() -> listOrderRecords(getOrderTableNameWithSchema()).size() == actualProxyList.size());
        List<Map<String, Object>> actualImportedList = listOrderRecords(getOrderTableNameWithSchema());
        assertThat(actualProxyList.size(), is(actualImportedList.size()));
        SchemaTableName schemaTableName = getDatabaseType().isSchemaAvailable()
                ? new SchemaTableName(new SchemaName(PipelineBaseE2EIT.SCHEMA_NAME), new TableName(getSourceTableOrderName()))
                : new SchemaTableName(new SchemaName(null), new TableName(getSourceTableOrderName()));
        PipelineDataSourceWrapper targetDataSource = new PipelineDataSourceWrapper(StorageContainerUtil.generateDataSource(getActualJdbcUrlTemplate(DS_4, false), getUsername(), getPassword()),
                getDatabaseType());
        PipelineDataSourceWrapper sourceDataSource = new PipelineDataSourceWrapper(generateShardingSphereDataSourceFromProxy(), getDatabaseType());
        StandardPipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(targetDataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(PipelineBaseE2EIT.SCHEMA_NAME, "t_order");
        PipelineColumnMetaData primaryKeyMetaData = tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0));
        ConsistencyCheckJobItemProgressContext progressContext = new ConsistencyCheckJobItemProgressContext("", 0);
        SingleTableInventoryDataConsistencyChecker checker = new SingleTableInventoryDataConsistencyChecker("", sourceDataSource, targetDataSource, schemaTableName, schemaTableName,
                primaryKeyMetaData, metaDataLoader, null, progressContext);
        DataConsistencyCheckResult checkResult = checker.check(new DataMatchDataConsistencyCalculateAlgorithm());
        assertTrue(checkResult.isMatched());
    }
    
    private void createOrderTableRule() throws SQLException {
        proxyExecuteWithLog(CREATE_SHARDING_RULE_SQL, 2);
    }
    
    private void initSchemaAndTable(final Connection connection) throws SQLException {
        if (getDatabaseType().isSchemaAvailable()) {
            String sql = String.format("CREATE SCHEMA %s", PipelineBaseE2EIT.SCHEMA_NAME);
            log.info("create schema sql: {}", sql);
            connection.createStatement().execute(sql);
        }
        String sql = getExtraSQLCommand().getCreateTableOrder(getSourceTableOrderName());
        log.info("create table sql: {}", sql);
        connection.createStatement().execute(sql);
    }
    
    private void startCDCClient() {
        ImportDataSourceParameter importDataSourceParam = new ImportDataSourceParameter(appendExtraParam(getActualJdbcUrlTemplate(DS_4, false, 0)), getUsername(), getPassword());
        StartCDCClientParameter parameter = new StartCDCClientParameter(importDataSourceParam);
        parameter.setAddress("localhost");
        parameter.setPort(getContainerComposer().getProxyCDCPort());
        parameter.setUsername(ProxyContainerConstants.USERNAME);
        parameter.setPassword(ProxyContainerConstants.PASSWORD);
        parameter.setDatabase("sharding_db");
        // TODO add full=false test case later
        parameter.setFull(true);
        String schema = getDatabaseType().isSchemaAvailable() ? "test" : "";
        parameter.setSchemaTables(Collections.singletonList(SchemaTable.newBuilder().setTable(getSourceTableOrderName()).setSchema(schema).build()));
        parameter.setDatabaseType(getDatabaseType().getType());
        CompletableFuture.runAsync(() -> new CDCClient(parameter).start(), executor).whenComplete((unused, throwable) -> {
            if (null != throwable) {
                log.error("cdc client sync failed, ", throwable);
            }
        });
    }
    
    private List<Map<String, Object>> listOrderRecords(final String tableNameWithSchema) throws SQLException {
        try (Connection connection = DriverManager.getConnection(getActualJdbcUrlTemplate(DS_4, false), getUsername(), getPassword())) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", tableNameWithSchema));
            return transformResultSetToList(resultSet);
        }
    }
    
    private String getOrderTableNameWithSchema() {
        if (getDatabaseType().isSchemaAvailable()) {
            return String.join(".", PipelineBaseE2EIT.SCHEMA_NAME, getSourceTableOrderName());
        } else {
            return getSourceTableOrderName();
        }
    }
}
