/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.opentracing.config;

import io.shardingsphere.opentracing.fixture.FooTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConfigurationLoaderTest {
    
    @Before
    public void setUp() {
        System.setProperty("shardingjdbc.opentracing.tracer.class", FooTracer.class.getName());
        System.setProperty("shardingjdbc.opentracing.tracer.sampleNumPM", "40");
    }
    
    @After
    public void tearDown() {
        System.getProperties().remove("shardingjdbc.opentracing.tracer.class");
        System.getProperties().remove("shardingjdbc.opentracing.tracer.sampleNumPM");
    }
    
    @Test
    public void assertLoadConfigFromProperty() {
        assertThat(new ConfigurationLoader().getTracerClassName(), is(FooTracer.class.getName()));
        assertThat(new ConfigurationLoader().getSampleNumPM(), is(40));
    }
    
}
