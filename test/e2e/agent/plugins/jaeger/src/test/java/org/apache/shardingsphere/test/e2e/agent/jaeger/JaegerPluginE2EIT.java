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

package org.apache.shardingsphere.test.e2e.agent.jaeger;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.agent.common.BasePluginE2EIT;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.jaeger.result.JaegerTraceResult;
import org.apache.shardingsphere.test.e2e.agent.jaeger.result.JaegerTraceResult.JaegerTraceResultData;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

public final class JaegerPluginE2EIT extends BasePluginE2EIT {
    
    private Properties props;
    
    private String url;
    
    private String serviceName;
    
    @Before
    public void before() {
        props = E2ETestEnvironment.getInstance().getProps();
        url = props.getProperty("jaeger.url");
        serviceName = props.getProperty("jaeger.servername");
    }
    
    @SneakyThrows(IOException.class)
    @Test
    public void assertProxyWithAgent() {
        super.assertProxyWithAgent();
        try {
            Thread.sleep(Long.parseLong(props.getProperty("jaeger.waitMs", "60000")));
        } catch (final InterruptedException ignore) {
        }
        assertTraces();
    }
    
    @SneakyThrows(IOException.class)
    private void assertTraces() {
        String traceURL = url + "traces?service=" + serviceName;
        JaegerTraceResult jaegerTraceResult = OkHttpUtils.getInstance().get(traceURL, JaegerTraceResult.class);
        assertFalse("Jaeger should have tracing data.", jaegerTraceResult.getData().isEmpty());
        for (JaegerTraceResultData each : jaegerTraceResult.getData()) {
            String traceIdURL = url + "traces/" + each.getTraceID();
            assertFalse(String.format("Jaeger trace '%s' should has corresponding data", traceIdURL), OkHttpUtils.getInstance().get(traceIdURL, JaegerTraceResult.class).getData().isEmpty());
        }
    }
}
