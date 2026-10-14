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

package org.apache.shardingsphere.test.e2e.sql.it.distsql.ral;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.json.JsonEngine;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioCommonPath;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.sql.env.SQLE2EEnvironmentEngine;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EIT;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SQLE2EITSettings(SQLCommandType.RAL)
@Setter
class RALE2EIT implements SQLE2EIT {
    
    private SQLE2EEnvironmentEngine environmentEngine;
    
    @ParameterizedTest(name = "{0}", allowZeroInvocations = true)
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException, IOException {
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        Throwable primaryException = null;
        try {
            init(context);
            assertExecute(context, testParam);
            // CHECKSTYLE:OFF
        } catch (final SQLException | IOException | RuntimeException | AssertionError ex) {
            // CHECKSTYLE:ON
            primaryException = ex;
            throw ex;
        } finally {
            try {
                tearDown(context);
                // CHECKSTYLE:OFF
            } catch (final SQLException | RuntimeException ex) {
                // CHECKSTYLE:ON
                if (null == primaryException) {
                    throw ex;
                }
                primaryException.addSuppressed(ex);
            }
        }
    }
    
    private void assertExecute(final SQLE2EITContext context, final AssertionTestParameter testParam) throws SQLException, IOException {
        try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                assertResultSet(context, statement, testParam);
            }
        }
    }
    
    private void init(final SQLE2EITContext context) throws SQLException {
        if (null != context.getAssertion().getInitialSQL()) {
            try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
                executeInitSQLs(context, connection);
            }
        }
    }
    
    private void executeInitSQLs(final SQLE2EITContext context, final Connection connection) throws SQLException {
        if (null == context.getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(context.getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void tearDown(final SQLE2EITContext context) throws SQLException {
        if (null != context.getAssertion().getDestroySQL()) {
            try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
                executeDestroySQLs(context, connection);
            }
        }
    }
    
    private void executeDestroySQLs(final SQLE2EITContext context, final Connection connection) throws SQLException {
        if (null == context.getAssertion().getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(context.getAssertion().getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void assertResultSet(final SQLE2EITContext context, final Statement statement, final AssertionTestParameter testParam) throws SQLException, IOException {
        if (null == context.getAssertion().getAssertionSQL()) {
            assertResultSet(context, statement, context.getSQL(), testParam);
        } else {
            statement.execute(context.getSQL());
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
            assertResultSet(context, statement, context.getAssertion().getAssertionSQL().getSql(), testParam);
        }
    }
    
    private void assertResultSet(final SQLE2EITContext context, final Statement statement, final String sql, final AssertionTestParameter testParam) throws SQLException, IOException {
        statement.execute(sql);
        try (ResultSet resultSet = statement.getResultSet()) {
            assertResultSet(context, resultSet, testParam);
        }
    }
    
    private void assertResultSet(final SQLE2EITContext context, final ResultSet resultSet, final AssertionTestParameter testParam) throws SQLException, IOException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(context), context, testParam);
        if ("jdbc".equals(testParam.getAdapter()) && ("show_storage_units.xml".equals(context.getAssertion().getExpectedDataFile())
                || "show_sharding_table_rule.xml".equals(context.getAssertion().getExpectedDataFile()) || "show_tables.xml".equals(context.getAssertion().getExpectedDataFile()))) {
            assertThat(getActualRows(resultSet), containsInAnyOrder(getExpectedJdbcRows(context, testParam).toArray()));
        } else {
            assertRows(resultSet, getIgnoreAssertColumns(context), context.getDataSet().getRows());
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected,
                                final SQLE2EITContext context, final AssertionTestParameter testParam) throws SQLException, IOException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            if ("jdbc".equals(testParam.getAdapter()) && "show_tables.xml".equals(context.getAssertion().getExpectedDataFile())) {
                Collection<String> columnLabels = getDataSourceConfigurations(testParam).values().stream()
                        .map(dataSource -> URI.create(dataSource.get("url").toString().substring("jdbc:".length())))
                        .map(uri -> "tables_in_" + uri.getPath().substring(1).toLowerCase()).collect(Collectors.toList());
                assertThat(columnLabels, hasItem(actual.getColumnLabel(index++).toLowerCase()));
            } else {
                assertThat(actual.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
            }
        }
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SQLE2EITContext context) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private Collection<List<Object>> getActualRows(final ResultSet actual) throws SQLException {
        Collection<List<Object>> result = new LinkedList<>();
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            result.add(getActualRow(actual, actualMetaData));
        }
        return result;
    }
    
    private List<Object> getActualRow(final ResultSet actual, final ResultSetMetaData actualMetaData) throws SQLException {
        List<Object> result = new ArrayList<>(actualMetaData.getColumnCount());
        for (int columnIndex = 1; columnIndex <= actualMetaData.getColumnCount(); columnIndex++) {
            String actualValue = String.valueOf(actual.getObject(columnIndex)).trim();
            assertThat(String.valueOf(actual.getObject(actualMetaData.getColumnLabel(columnIndex))).trim(), is(actualValue));
            result.add("other_attributes".equals(actualMetaData.getColumnLabel(columnIndex)) ? JsonEngine.unmarshal(actualValue, JsonNode.class) : actualValue);
        }
        return result;
    }
    
    private Collection<List<Object>> getExpectedJdbcRows(final SQLE2EITContext context, final AssertionTestParameter testParam) throws IOException {
        Collection<List<Object>> result = context.getDataSet().getRows().stream().map(each -> new ArrayList<Object>(each.splitValues("|"))).collect(Collectors.toList());
        if ("show_storage_units.xml".equals(context.getAssertion().getExpectedDataFile())) {
            Map<String, Map<String, Object>> dataSources = getDataSourceConfigurations(testParam);
            URI uri = URI.create(((HikariDataSource) environmentEngine.getActualDataSourceMap().values().iterator().next()).getJdbcUrl().substring("jdbc:".length()));
            for (List<Object> each : result) {
                Map<String, Object> dataSource = dataSources.get(each.get(0));
                each.set(2, uri.getHost());
                each.set(3, String.valueOf(uri.getPort()));
                each.set(8, dataSource.get("maxPoolSize").toString());
                each.set(9, dataSource.get("minPoolSize").toString());
                each.set(11, getExpectedStorageUnitAttributes(each.get(11).toString(), dataSource));
            }
        }
        return result;
    }
    
    private Map<String, Map<String, Object>> getDataSourceConfigurations(final AssertionTestParameter testParam) throws IOException {
        File configurationFile = new File(new ScenarioCommonPath(testParam.getScenario()).getRuleConfigurationFile(testParam.getDatabaseType()));
        return YamlEngine.unmarshal(configurationFile, YamlRootConfiguration.class).getDataSources();
    }
    
    private ObjectNode getExpectedStorageUnitAttributes(final String attributes, final Map<String, Object> dataSource) {
        ObjectNode result = JsonEngine.unmarshal(attributes, ObjectNode.class);
        ObjectNode queryProperties = result.putObject("queryProperties");
        URI uri = URI.create(dataSource.get("url").toString().substring("jdbc:".length()));
        for (String each : Splitter.on('&').split(uri.getQuery())) {
            List<String> property = Splitter.on('=').limit(2).splitToList(each);
            queryProperties.put(property.get(0), property.get(1));
        }
        return result;
    }
    
    private Collection<String> getIgnoreAssertColumns(final SQLE2EITContext context) {
        Collection<String> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns().stream().filter(DataSetColumn::isIgnoreAssertData).map(DataSetColumn::getName).collect(Collectors.toList()));
        }
        return result;
    }
    
    private void assertRows(final ResultSet actual, final Collection<String> notAssertionColumns, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected data set rows.");
            assertRow(actual, notAssertionColumns, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected data set rows.", rowCount, is(expected.size()));
    }
    
    private void assertRow(final ResultSet actual, final Collection<String> notAssertionColumns, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (!notAssertionColumns.contains(columnLabel)) {
                assertObjectValue(actual, columnIndex, columnLabel, each);
            }
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)).trim(), is(expected));
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RAL).isEmpty();
    }
}
