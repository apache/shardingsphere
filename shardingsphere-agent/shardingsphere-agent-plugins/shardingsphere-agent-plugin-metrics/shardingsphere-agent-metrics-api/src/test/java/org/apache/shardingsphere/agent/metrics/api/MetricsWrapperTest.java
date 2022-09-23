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

package org.apache.shardingsphere.agent.metrics.api;

import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public final class MetricsWrapperTest {
    
    @Test
    public void assertCreate() {
        FixtureWrapper metricsWrapper = new FixtureWrapper();
        metricsWrapper.inc();
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0d));
        metricsWrapper.inc(1);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(2.0d));
        metricsWrapper.inc("a");
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(3.0d));
        metricsWrapper.inc(1, "b");
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(4.0d));
        metricsWrapper.inc();
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(5.0d));
        metricsWrapper.dec();
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(4.0d));
        metricsWrapper.inc(1);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(5.0d));
        metricsWrapper.dec(1);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(4.0d));
        metricsWrapper.dec(1);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(3.0d));
        metricsWrapper.dec("c");
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(2.0d));
        metricsWrapper.dec(1, "d");
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0d));
        metricsWrapper.observe(2);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(2.0d));
        metricsWrapper.observe(3);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(3.0d));
        metricsWrapper.delegate(1);
        assertThat(metricsWrapper.getFixtureValue(), org.hamcrest.Matchers.is(-1.0d));
    }
}
