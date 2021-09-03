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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.agent.test.common.BasePluginIT;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.OkHttpUtils;
import org.apache.shardingsphere.integration.agent.test.opentelemetry.result.TracingResult;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Slf4j
public final class OpenTelemetryPluginIT extends BasePluginIT {
    
    private static final String SS_ROOTINVOKE = "/shardingsphere/rootinvoke/";
    
    private static final String SS_PARSESQL = "/shardingsphere/parsesql/";
    
    private static final String SS_EXECUTESQL = "/shardingsphere/executesql/";

    @Test
    public void assertProxyWithAgent() {
        super.assertProxyWithAgent();
        Properties engineEnvProps = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
        try {
            Thread.sleep(Long.parseLong(engineEnvProps.getProperty("opentelemetry.waitMs", "60000")));
        } catch (final InterruptedException ignore) {
        }
        String url = engineEnvProps.getProperty("opentelemetry.zipkin.url") + engineEnvProps.getProperty("opentelemetry.servername");
        String response = null;
        try {
            response = OkHttpUtils.getInstance().get(url);
        } catch (final IOException ex) {
            log.info("http get zipkin is error :", ex);
        }
        assertNotNull(response);
        JsonArray array = new JsonParser().parse(response).getAsJsonArray().get(0).getAsJsonArray();
        Gson gson = new Gson();
        Collection<TracingResult> traces = new ArrayList<>();
        array.forEach(element -> traces.add(gson.fromJson(element, TracingResult.class)));
        assertTraces(traces);
    }
    
    private void assertTraces(final Collection<TracingResult> traces) {
        traces.forEach(tracingResult -> {
            assertNotNull(tracingResult.getTraceId());
            assertNotNull(tracingResult.getId());
            String name = tracingResult.getName();
            assertNotNull(name);
            assertNotNull(tracingResult.getTimestamp());
            assertNotNull(tracingResult.getDuration());
            Map<String, String> localEndPoint = tracingResult.getLocalEndpoint();
            assertNotNull(localEndPoint);
            assertThat(localEndPoint.get("serviceName"), is("shardingsphere-agent"));
            assertNotNull(localEndPoint.get("ipv4"));
            Map<String, String> tags = tracingResult.getTags();
            switch (name) {
                case SS_ROOTINVOKE:
                    assertRootInvokeTags(tags);
                    break;
                case SS_PARSESQL:
                    assertParseSqlTags(tags);
                    break;
                case SS_EXECUTESQL:
                    assertExecuteSqlTags(tags);
                    break;
                default:
                    fail();
            }
        });
    }
    
    private void assertRootInvokeTags(final Map<String, String> tags) {
        assertThat(tags.get("component"), is("ShardingSphere"));
        assertThat(tags.get("otel.library.name"), is("shardingsphere-agent"));
    }
    
    private void assertParseSqlTags(final Map<String, String> tags) {
        assertThat(tags.get("component"), is("ShardingSphere"));
        assertNotNull(tags.get("db.statement"));
        assertThat(tags.get("db.type"), is("shardingsphere-proxy"));
        assertThat(tags.get("otel.library.name"), is("shardingsphere-agent"));
    }
    
    private void assertExecuteSqlTags(final Map<String, String> tags) {
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
