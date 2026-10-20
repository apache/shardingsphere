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

package org.apache.shardingsphere.test.e2e.sql.it.distsql.rql;

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
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SQLE2EITSettings(SQLCommandType.RQL)
@Setter
class RQLE2EIT implements SQLE2EIT {
    
    private SQLE2EEnvironmentEngine environmentEngine;
    
    @ParameterizedTest(name = "{0}", allowZeroInvocations = true)
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException, IOException {
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        assertExecute(context, testParam);
    }
    
    private void assertExecute(final SQLE2EITContext context, final AssertionTestParameter testParam) throws SQLException, IOException {
        try (
                Connection connection = environmentEngine.getTargetDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(context.getSQL());
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(context, resultSet, testParam);
            }
        }
    }
    
    private void assertResultSet(final SQLE2EITContext context, final ResultSet resultSet, final AssertionTestParameter testParam) throws SQLException, IOException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(context));
        if ("jdbc".equals(testParam.getAdapter()) && ("show_storage_units.xml".equals(context.getAssertion().getExpectedDataFile())
                || "show_storage_units_jdbc.xml".equals(context.getAssertion().getExpectedDataFile()))) {
            assertThat(getActualRows(resultSet), containsInAnyOrder(getExpectedJdbcRows(context, testParam).toArray()));
        } else {
            assertRows(resultSet, context.getDataSet().getRows());
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected) throws SQLException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            assertThat(actual.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
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
            String actualValue = String.valueOf(actual.getObject(columnIndex));
            assertThat(String.valueOf(actual.getObject(actualMetaData.getColumnLabel(columnIndex))), is(actualValue));
            result.add(12 == columnIndex ? JsonEngine.unmarshal(actualValue, JsonNode.class) : actualValue);
        }
        return result;
    }
    
    private Collection<List<Object>> getExpectedJdbcRows(final SQLE2EITContext context, final AssertionTestParameter testParam) throws IOException {
        Map<String, Map<String, Object>> dataSources = YamlEngine.unmarshal(
                new File(new ScenarioCommonPath(testParam.getScenario()).getRuleConfigurationFile(testParam.getDatabaseType())), YamlRootConfiguration.class).getDataSources();
        Collection<List<Object>> result = new ArrayList<>(context.getDataSet().getRows().size());
        URI uri = URI.create(((HikariDataSource) environmentEngine.getActualDataSourceMap().values().iterator().next()).getJdbcUrl().substring("jdbc:".length()));
        for (DataSetRow each : context.getDataSet().getRows()) {
            List<Object> row = new ArrayList<>(each.splitValues("|"));
            row.set(2, uri.getHost());
            row.set(3, String.valueOf(uri.getPort()));
            if ("show_storage_units.xml".equals(context.getAssertion().getExpectedDataFile())) {
                Map<String, Object> dataSourceProps = dataSources.get(row.get(0).toString());
                row.set(8, dataSourceProps.get("maxPoolSize").toString());
                row.set(9, dataSourceProps.get("minPoolSize").toString());
                row.set(11, getExpectedAttributes(row.get(11).toString(), dataSourceProps.get("url").toString()));
            } else {
                row.set(11, JsonEngine.unmarshal(row.get(11).toString(), JsonNode.class));
            }
            result.add(row);
        }
        return result;
    }
    
    private JsonNode getExpectedAttributes(final String value, final String jdbcUrl) {
        ObjectNode result = (ObjectNode) JsonEngine.unmarshal(value, JsonNode.class);
        ObjectNode queryProperties = result.putObject("queryProperties");
        for (String each : Splitter.on("&").split(URI.create(jdbcUrl.substring("jdbc:".length())).getQuery())) {
            String[] property = each.split("=", 2);
            queryProperties.put(property[0], property[1]);
        }
        return result;
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected data set rows.");
            assertRow(actual, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected data set rows.", rowCount, is(expected.size()));
    }
    
    private void assertRow(final ResultSet actual, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            assertObjectValue(actual, columnIndex, columnLabel, each);
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)), is(expected));
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RQL).isEmpty();
    }
}
