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

package org.apache.shardingsphere.spring.namespace.orchestration;

import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/namespace/metricsNamespace.xml")
public final class MetricsNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertMetricsConfiguration() {
        MetricsConfiguration metricsConfiguration = applicationContext.getBean("metrics-config", MetricsConfiguration.class);
        assertNotNull(metricsConfiguration);
        assertThat(metricsConfiguration.getMetricsName(), is("prometheus"));
        assertThat(metricsConfiguration.getHost(), is("127.0.0.1"));
        assertThat(metricsConfiguration.getPort(), is(9191));
        assertTrue(metricsConfiguration.getEnable());
        assertFalse(metricsConfiguration.getAsync());
        assertThat(metricsConfiguration.getThreadCount(), is(10));
        assertThat(metricsConfiguration.getProps().getProperty("test"), is("test"));
    }
}
