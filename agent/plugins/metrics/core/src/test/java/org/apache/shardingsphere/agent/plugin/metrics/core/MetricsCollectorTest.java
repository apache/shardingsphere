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

package org.apache.shardingsphere.agent.plugin.metrics.core;

import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureMetricsCollector;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MetricsCollectorTest {
    
    @Test
    public void assertCreate() {
        FixtureMetricsCollector fixtureMetricsCollector = new FixtureMetricsCollector();
        fixtureMetricsCollector.inc();
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(1d));
        fixtureMetricsCollector.inc("a");
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(2d));
        fixtureMetricsCollector.dec();
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(1d));
        fixtureMetricsCollector.dec("c");
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(0d));
        fixtureMetricsCollector.observe(2);
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(2.0d));
        fixtureMetricsCollector.observe(3);
        assertThat(fixtureMetricsCollector.getFixtureValue(), is(3.0d));
    }
}
