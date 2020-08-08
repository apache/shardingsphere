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

package org.apache.shardingsphere.infra.spi.singleton;

import org.apache.shardingsphere.infra.spi.fixture.TypedSPIFixture;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SingletonServiceLoaderTest {
    
    @Test
    public void assertGetSingletonServiceLoader() {
        SingletonServiceLoader<TypedSPIFixture> actualFirstServiceLoader = SingletonServiceLoader.getServiceLoader(TypedSPIFixture.class);
        assertNotNull(actualFirstServiceLoader);
        SingletonServiceLoader<TypedSPIFixture> actualSecondServiceLoader = SingletonServiceLoader.getServiceLoader(TypedSPIFixture.class);
        assertNotNull(actualSecondServiceLoader);
        assertThat(actualFirstServiceLoader, is(actualSecondServiceLoader));
    }
    
    @Test
    public void assertNewServiceInstanceWhenIsNotExist() {
        Optional<TypedSPIFixture> actual = SingletonServiceLoader.getServiceLoader(TypedSPIFixture.class).newServiceInstances();
        assertTrue(actual.isPresent());
    }
}
