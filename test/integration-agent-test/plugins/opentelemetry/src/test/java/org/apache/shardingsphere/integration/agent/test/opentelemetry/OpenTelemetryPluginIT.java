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

package org.apache.shardingsphere.integration.agent.test.opentelemetry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.shardingsphere.integration.agent.test.common.BasePluginIT;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.OkHttpUtils;
import org.apache.shardingsphere.integration.agent.test.opentelemetry.result.TracingResult;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public final class OpenTelemetryPluginIT extends BasePluginIT {
    
    private static final String ROOT_INVOKE = "/shardingsphere/rootinvoke/";
    
    private static final String PARSE_SQL = "/shardingsphere/parsesql/";
    
    private static final String EXECUTE_SQL = "/shardingsphere/executesql/";
    
    @Test
    public void assertProxyWithAgent() throws IOException {
        super.assertProxyWithAgent();
        Properties props = IntegrationTestEnvironment.getInstance().getProps();
        try {
            Thread.sleep(Long.parseLong(props.getProperty("opentelemetry.waitMs", "60000")));
        } catch (final InterruptedException ignore) {
        }
        String url = props.getProperty("opentelemetry.zipkin.url") + props.getProperty("opentelemetry.servername");
        JsonArray array = JsonParser.parseString(OkHttpUtils.getInstance().get(url)).getAsJsonArray().get(0).getAsJsonArray();
        Gson gson = new Gson();
        Collection<TracingResult> traces = new LinkedList<>();
        array.forEach(each -> traces.add(gson.fromJson(each, TracingResult.class)));
        traces.forEach(this::assertTrace);
    }
    
    private void assertTrace(final TracingResult tracingResult) {
        assertNotNull(tracingResult.getTraceId());
        assertNotNull(tracingResult.getId());
        assertNotNull(tracingResult.getTimestamp());
        assertNotNull(tracingResult.getDuration());
        Map<String, String> localEndPoint = tracingResult.getLocalEndpoint();
        assertThat(localEndPoint.get("serviceName"), is("shardingsphere"));
        assertNotNull(localEndPoint.get("ipv4"));
        Map<String, String> tags = tracingResult.getTags();
        switch (tracingResult.getName()) {
            case ROOT_INVOKE:
                assertRootInvokeTags(tags);
                break;
            case PARSE_SQL:
                assertParseSQLTags(tags);
                break;
            case EXECUTE_SQL:
                assertExecuteSQLTags(tags);
                break;
            default:
                fail();
        }
    }
    
    private void assertRootInvokeTags(final Map<String, String> tags) {
        assertThat(tags.get("component"), is("ShardingSphere"));
        assertThat(tags.get("otel.library.name"), is("shardingsphere-agent"));
    }
    
    private void assertParseSQLTags(final Map<String, String> tags) {
        assertThat(tags.get("component"), is("ShardingSphere"));
        assertNotNull(tags.get("db.statement"));
        assertThat(tags.get("db.type"), is("shardingsphere-proxy"));
        assertThat(tags.get("otel.library.name"), is("shardingsphere-agent"));
    }
    
    private void assertExecuteSQLTags(final Map<String, String> tags) {
        assertThat(tags.get("component"), is("ShardingSphere"));
        assertNotNull(tags.get("db.bind_vars"));
        assertNotNull(tags.get("db.instance"));
        assertNotNull(tags.get("db.statement"));
        assertThat(tags.get("db.type"), is("shardingsphere-proxy"));
        assertThat(tags.get("otel.library.name"), is("shardingsphere-agent"));
        assertNotNull(tags.get("peer.hostname"));
        assertNotNull(tags.get("peer.port"));
    }
}
