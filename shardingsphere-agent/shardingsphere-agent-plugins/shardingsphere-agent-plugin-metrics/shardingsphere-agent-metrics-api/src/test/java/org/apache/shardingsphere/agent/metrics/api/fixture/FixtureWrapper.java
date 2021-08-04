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

package org.apache.shardingsphere.agent.metrics.api.fixture;

import lombok.Getter;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;

/**
 * Fixed metric wrapper.
 */
@Getter
public final class FixtureWrapper implements MetricsWrapper {
    
    private Double fixtureValue = 0.0d;
    
    @Override
    public void inc(final double value) {
        fixtureValue += value;
    }
    
    @Override
    public void inc(final double value, final String... labels) {
        fixtureValue += value;
    }
    
    @Override
    public void dec(final double value) {
        fixtureValue -= value;
    }
    
    @Override
    public void dec(final double value, final String... labels) {
        fixtureValue -= value;
    }
    
    @Override
    public void observe(final double value) {
        fixtureValue = value;
    }
    
    @Override
    public void delegate(final Object object) {
        fixtureValue = -1.0;
    }
}
