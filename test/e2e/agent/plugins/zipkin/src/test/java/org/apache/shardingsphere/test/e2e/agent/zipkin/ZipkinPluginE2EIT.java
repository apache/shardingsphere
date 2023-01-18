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

package org.apache.shardingsphere.test.e2e.agent.zipkin;

import com.google.gson.internal.LinkedTreeMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.agent.common.BasePluginE2EIT;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ZipkinPluginE2EIT extends BasePluginE2EIT {
    
    private Properties props;
    
    private String url;
    
    private String serviceName;
    
    @Before
    public void before() {
        props = E2ETestEnvironment.getInstance().getProps();
        url = props.getProperty("zipkin.url");
        serviceName = props.getProperty("zipkin.servername");
    }
    
    @Test
    @SneakyThrows(IOException.class)
    public void assertProxyWithAgent() {
        super.assertProxyWithAgent();
        try {
            // TODO this needs to refactor, replace sleep with polling.
            Thread.sleep(Long.parseLong(props.getProperty("zipkin.waitMs", "60000")));
        } catch (final InterruptedException ignore) {
        }
        assertSpans();
        assertTraces();
        assertTraceContent();
    }
    
    @SneakyThrows(IOException.class)
    private void assertSpans() {
        String spansURL = url + "spans?serviceName=" + serviceName;
        List<?> spans = OkHttpUtils.getInstance().get(spansURL, List.class);
        assertThat(spans.size(), is(3));
        final String executeSQLSpan = "/shardingsphere/executesql/";
        final String parseSQLSpan = "/shardingsphere/parsesql/";
        final String rootInvokeSpan = "/shardingsphere/rootinvoke/";
        assertTrue(String.format("Zipkin span `%s` should exist.", executeSQLSpan), spans.contains(executeSQLSpan));
        assertTrue(String.format("Zipkin span `%s` should exist.", parseSQLSpan), spans.contains(parseSQLSpan));
        assertTrue(String.format("Zipkin span `%s` should exist.", rootInvokeSpan), spans.contains(rootInvokeSpan));
    }
    
    @SneakyThrows(IOException.class)
    private void assertTraces() {
        final String traceExecuteSqlUrl = url + "traces?spanName=/shardingsphere/executesql/";
        final String traceParseSqlUrl = url + "traces?spanName=/shardingsphere/parsesql/";
        final String traceRootInvokeURL = url + "traces?spanName=/shardingsphere/rootinvoke/";
        assertFalse(String.format("Zipkin trace `%s` should exist.", traceExecuteSqlUrl), OkHttpUtils.getInstance().get(traceExecuteSqlUrl, List.class).isEmpty());
        assertFalse(String.format("Zipkin trace `%s` should exist.", traceParseSqlUrl), OkHttpUtils.getInstance().get(traceParseSqlUrl, List.class).isEmpty());
        assertFalse(String.format("Zipkin trace `%s` should exist.", traceRootInvokeURL), OkHttpUtils.getInstance().get(traceRootInvokeURL, List.class).isEmpty());
    }
    
    @SneakyThrows(IOException.class)
    private void assertTraceContent() {
        Set<String> traceStatement = new LinkedHashSet<>();
        String traceURL = url + "traces?limit=1000";
        List<?> traceResult = OkHttpUtils.getInstance().get(traceURL, List.class);
        final String insertSQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (10, 10, 'INSERT_TEST')";
        final String insertRollBackSQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (10, 10, 'INSERT_TEST')";
        final String deleteSQL = "DELETE FROM t_order WHERE order_id=10";
        final String updateRollBackSQL = "UPDATE t_order SET status = 'ROLL_BACK' WHERE order_id =1000";
        final String selectSQL = "SELECT * FROM t_order";
        traceResult.forEach(each -> traceStatement.addAll(extractTraceTags((List<?>) each)));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", insertSQL), traceStatement.contains(insertSQL));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", insertRollBackSQL), traceStatement.contains(insertRollBackSQL));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", deleteSQL), traceStatement.contains(deleteSQL));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", updateRollBackSQL), traceStatement.contains(updateRollBackSQL));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", selectSQL), traceStatement.contains(selectSQL));
        assertTrue(String.format("Zipkin should trace the SQL : `%s`", insertSQL), traceStatement.contains(insertSQL));
    }
    
    private Set<String> extractTraceTags(final List<?> zipkinQueryResults) {
        Set<String> result = new LinkedHashSet<>();
        zipkinQueryResults.forEach(each -> {
            LinkedTreeMap<?, ?> trace = (LinkedTreeMap<?, ?>) each;
            LinkedTreeMap<?, ?> tag = (LinkedTreeMap<?, ?>) trace.get("tags");
            if (null != tag.get("db.statement")) {
                result.add(tag.get("db.statement").toString());
            }
        });
        return result;
    }
}
