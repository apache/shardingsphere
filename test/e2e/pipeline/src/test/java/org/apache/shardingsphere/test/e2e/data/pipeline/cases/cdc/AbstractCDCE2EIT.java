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
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.After;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public abstract class AbstractCDCE2EIT extends PipelineBaseE2EIT {
    
    private static final String REGISTER_STORAGE_UNIT_SQL = "REGISTER STORAGE UNIT ds_0 ( URL='${ds0}', USER='${user}', PASSWORD='${password}'),"
            + "ds_1 ( URL='${ds1}', USER='${user}', PASSWORD='${password}')";
    
    private static final String CREATE_SHARDING_RULE_SQL = "CREATE SHARDING TABLE RULE t_order("
            + "STORAGE_UNITS(ds_0,ds_1),"
            + "SHARDING_COLUMN=user_id,"
            + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='4')),"
            + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake'))"
            + ")";
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public AbstractCDCE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
        // if the time zone of the unit test and the time zone of the mirror do not match, CDC will get the wrong timestamp
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    protected void registerStorageUnit() throws SQLException {
        String registerStorageUnitTemplate = REGISTER_STORAGE_UNIT_SQL.replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds0}", appendExtraParam(getActualJdbcUrlTemplate(DS_0, true)))
                .replace("${ds1}", appendExtraParam(getActualJdbcUrlTemplate(DS_1, true)));
        addResource(registerStorageUnitTemplate);
    }
    
    protected void createOrderTableRule() throws SQLException {
        proxyExecuteWithLog(CREATE_SHARDING_RULE_SQL, 2);
    }
    
    protected void createSchema(final String schemaName) throws SQLException {
        if (!getDatabaseType().isSchemaAvailable()) {
            return;
        }
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            proxyExecuteWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName), 2);
            return;
        }
        if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            proxyExecuteWithLog(String.format("CREATE SCHEMA %s", schemaName), 2);
        }
    }
    
    protected void startCDCClient() {
        ImportDataSourceParameter importDataSourceParam = new ImportDataSourceParameter(appendExtraParam(getActualJdbcUrlTemplate(DS_4, false, 0)), getUsername(), getPassword());
        StartCDCClientParameter parameter = new StartCDCClientParameter(importDataSourceParam);
        parameter.setAddress("localhost");
        parameter.setPort(getContainerComposer().getProxyCDCPort());
        parameter.setUsername(PipelineEnvTypeEnum.DOCKER == ENV.getItEnvType() ? ProxyContainerConstants.USERNAME : "root");
        parameter.setPassword(PipelineEnvTypeEnum.DOCKER == ENV.getItEnvType() ? ProxyContainerConstants.PASSWORD : "root");
        parameter.setDatabase("sharding_db");
        parameter.setFull(true);
        String schema = "";
        if (getDatabaseType().isSchemaAvailable()) {
            schema = "test";
        }
        parameter.setSchemaTables(Collections.singletonList(SchemaTable.newBuilder().setTable(getSourceTableOrderName()).setSchema(schema).build()));
        parameter.setDatabaseType(getDatabaseType().getType());
        CompletableFuture.runAsync(() -> new CDCClient(parameter).start(), executor).whenComplete((unused, throwable) -> {
            if (null != throwable) {
                log.error("cdc client sync failed, ", throwable);
            }
            throw new RuntimeException(throwable);
        });
        ThreadUtil.sleep(5, TimeUnit.SECONDS);
    }
    
    protected List<Map<String, Object>> listOrderRecords(final String tableNameWithSchema) throws SQLException {
        try (Connection connection = DriverManager.getConnection(getActualJdbcUrlTemplate(DS_4, false), getUsername(), getPassword())) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", tableNameWithSchema));
            return transformResultSetToList(resultSet);
        }
    }
    
    protected void assertDataMatched(final List<Map<String, Object>> actualProxyRecords, final List<Map<String, Object>> actualImportedRecords) {
        for (int i = 0; i < actualProxyRecords.size(); i++) {
            Map<String, Object> proxyData = actualProxyRecords.get(i);
            Map<String, Object> importedData = actualImportedRecords.get(i);
            Object orderId = proxyData.get("order_id");
            assertThat(orderId, is(importedData.get("order_id")));
            assertThat(proxyData.get("user_id"), is(importedData.get("user_id")));
            assertThat(proxyData.get("status"), is(importedData.get("status")));
            assertThat(proxyData.get("t_float"), is(importedData.get("t_float")));
            assertThat(proxyData.get("t_double"), is(importedData.get("t_double")));
            assertThat(proxyData.get("t_json"), is(importedData.get("t_json")));
            // TODO the result of openGauss contain more precise value, eg.2023-02-28 21:46:30.828, but proxy result is 2023-02-28 13:46:30.828664, ignore nanos now
            assertThat(((Timestamp) proxyData.get("t_timestamp")).getTime(), is(((Timestamp) importedData.get("t_timestamp")).getTime()));
            assertThat(proxyData.get("t_char"), is(importedData.get("t_char")));
            assertThat(proxyData.get("t_text"), is(importedData.get("t_text")));
            // TODO the result of PostgreSQL contain more precise value, eg.11:59:37.979Z, but proxy result is 11:59:37
            assertThat(proxyData.get("t_time").toString(), is(importedData.get("t_time").toString()));
            assertThat(proxyData.get("t_date"), is(importedData.get("t_date")));
        }
    }
    
    @After
    public void cleanUp() {
        executor.shutdown();
    }
}
