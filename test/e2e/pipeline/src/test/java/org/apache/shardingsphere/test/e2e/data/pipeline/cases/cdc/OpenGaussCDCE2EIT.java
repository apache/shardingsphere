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
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.task.CDCIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * CDC general E2E IT.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class OpenGaussCDCE2EIT extends AbstractCDCE2EIT {
    
    public OpenGaussCDCE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        OpenGaussDatabaseType databaseType = new OpenGaussDatabaseType();
        for (String each : PipelineBaseE2EIT.ENV.listStorageContainerImages(databaseType)) {
            result.add(new PipelineTestParameter(databaseType, each, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @Test
    public void assertCDCDataImportSuccess() throws SQLException, InterruptedException {
        initEnvironment(getDatabaseType(), new CDCJobType());
        registerStorageUnit();
        createOrderTableRule();
        createSchema(PipelineBaseE2EIT.SCHEMA_NAME);
        proxyExecuteWithLog(getExtraSQLCommand().getCreateTableOrder(getSourceTableOrderName()), 0);
        Pair<List<Object[]>, List<Object[]>> dataPair = PipelineCaseHelper.generateFullInsertData(getDatabaseType(), 20);
        log.info("init data begin: {}", LocalDateTime.now());
        DataSourceExecuteUtil.execute(getProxyDataSource(), getExtraSQLCommand().getFullInsertOrder(getSourceTableOrderName()), dataPair.getLeft());
        log.info("init data end: {}", LocalDateTime.now());
        initImporterTable();
        startCDCClient();
        startIncrementTask(new CDCIncrementTask(getProxyDataSource(), String.join(".", PipelineBaseE2EIT.SCHEMA_NAME, getSourceTableOrderName()), 0, 20, 3));
        getIncreaseTaskThread().join(20000);
        String tableNameWithSchema = String.join(".", PipelineBaseE2EIT.SCHEMA_NAME, "t_order");
        List<Map<String, Object>> actualProxyList;
        try (Connection connection = getProxyDataSource().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("SELECT * FROM %s ORDER BY order_id ASC", tableNameWithSchema));
            actualProxyList = transformResultSetToList(resultSet);
        }
        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).until(() -> listOrderRecords(tableNameWithSchema).size() == actualProxyList.size());
        List<Map<String, Object>> actualImportedList = listOrderRecords(tableNameWithSchema);
        assertThat(actualProxyList.size(), is(actualImportedList.size()));
        assertDataMatched(actualProxyList, actualImportedList);
    }
    
    private void initImporterTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(getActualJdbcUrlTemplate(DS_4, false), getUsername(), getPassword())) {
            connection.createStatement().execute(String.format("CREATE SCHEMA %s", PipelineBaseE2EIT.SCHEMA_NAME));
            connection.createStatement().execute(getExtraSQLCommand().getCreateTableOrder(getSourceTableOrderName()));
        }
    }
}
