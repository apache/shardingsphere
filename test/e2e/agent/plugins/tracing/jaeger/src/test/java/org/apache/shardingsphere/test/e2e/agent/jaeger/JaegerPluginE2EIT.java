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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.BasePluginE2EIT;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.jaeger.asserts.SpanAssert;
import org.apache.shardingsphere.test.e2e.agent.jaeger.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.e2e.agent.jaeger.cases.SpanTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

@Slf4j
@RunWith(Parameterized.class)
public final class JaegerPluginE2EIT extends BasePluginE2EIT {
    
    private final SpanTestCase spanTestCase;
    
    private Properties props;
    
    private String url;
    
    public JaegerPluginE2EIT(final SpanTestCase spanTestCase) {
        this.spanTestCase = spanTestCase;
    }
    
    @Parameters
    public static Collection<SpanTestCase> getTestParameters() {
        return IntegrationTestCasesLoader.getInstance().loadIntegrationTestCases();
    }
    
    @Before
    public void before() {
        props = E2ETestEnvironment.getInstance().getProps();
        url = props.getProperty("jaeger.url");
    }
    
    @SneakyThrows(IOException.class)
    @Test
    public void assertProxyWithAgent() {
        super.assertProxyWithAgent();
        SpanAssert.assertIs(url, spanTestCase);
    }
}
