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

import com.google.common.collect.Lists;
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
        assertSpans();
        assertTraces();
        assertTraceContent();
    }
    
    @SneakyThrows(IOException.class)
    private void assertSpans() {
        String spansURL = url + "spans?serviceName=" + serviceName;
        List<?> spans = OkHttpUtils.getInstance().get(spansURL, List.class);
        assertThat(spans.size(), is(3));
        List<String> spanList = Lists.newArrayList("/shardingsphere/executesql/", "/shardingsphere/parsesql/", "/shardingsphere/rootinvoke/");
        spanList.forEach(each -> assertTrue(String.format("Zipkin span `%s` should exist.", each), spans.contains(each)));
    }
    
    @SneakyThrows(IOException.class)
    private void assertTraces() {
        List<String> traceList = Lists.newArrayList(url + "traces?spanName=/shardingsphere/executesql/", url + "traces?spanName=/shardingsphere/parsesql/",
                url + "traces?spanName=/shardingsphere/rootinvoke/");
        for (String each : traceList) {
            assertFalse(String.format("Zipkin trace `%s` should exist.", each), OkHttpUtils.getInstance().get(each, List.class).isEmpty());
        }
    }
    
    @SneakyThrows(IOException.class)
    private void assertTraceContent() {
        Set<String> traceStatement = new LinkedHashSet<>();
        String traceURL = url + "traces?limit=1000";
        List<?> traceResult = OkHttpUtils.getInstance().get(traceURL, List.class);
        List<String> sqlList = Lists.newArrayList("INSERT INTO t_order (order_id, user_id, status) VALUES (10, 10, 'INSERT_TEST')",
                "INSERT INTO t_order (order_id, user_id, status) VALUES (10, 10, 'INSERT_TEST')",
                "DELETE FROM t_order WHERE order_id=10",
                "UPDATE t_order SET status = 'ROLL_BACK' WHERE order_id =1000",
                "SELECT * FROM t_order");
        traceResult.forEach(each -> traceStatement.addAll(extractTraceTags((List<?>) each)));
        sqlList.forEach(each -> assertTrue(String.format("Zipkin trace should contain `%s`.", each), traceStatement.contains(each)));
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
