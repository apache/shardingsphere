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

package org.apache.shardingsphere.spi;

import org.apache.shardingsphere.spi.fixture.TypeBasedSPIFixture;
import org.apache.shardingsphere.spi.fixture.TypeBasedSPIFixtureImpl;
import org.apache.shardingsphere.spi.fixture.TypeBasedSPIFixtureServiceLoader;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TypeBasedSPIServiceLoaderTest {
    
    private TypeBasedSPIFixtureServiceLoader serviceLoader = new TypeBasedSPIFixtureServiceLoader();
    
    @Test
    public void assertNewServiceSuccess() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        TypeBasedSPIFixture actual = serviceLoader.newService("FIXTURE", properties);
        assertThat(actual, instanceOf(TypeBasedSPIFixtureImpl.class));
        assertThat(actual.getProperties().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertNewServiceByDefault() {
        TypeBasedSPIFixture actual = serviceLoader.newService();
        assertThat(actual, instanceOf(TypeBasedSPIFixtureImpl.class));
        assertTrue(actual.getProperties().isEmpty());
    }
    
    @Test(expected = RuntimeException.class)
    public void assertNewServiceFailure() {
        serviceLoader.newService("INVALID", new Properties());
    }
}
