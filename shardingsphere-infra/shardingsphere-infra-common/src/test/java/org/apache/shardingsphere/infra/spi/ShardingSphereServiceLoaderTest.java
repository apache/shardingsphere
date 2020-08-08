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

package org.apache.shardingsphere.infra.spi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.spi.exception.ServiceLoaderInstantiationException;
import org.apache.shardingsphere.infra.spi.fixture.TypedSPIFixture;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereServiceLoaderTest {
    
    @Test
    public void assertNewServiceInstanceWhenIsNotExist() {
        ShardingSphereServiceLoader.register(Collection.class);
        Collection collection = ShardingSphereServiceLoader.newServiceInstances(Collection.class);
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void assertNewServiceInstanceWhenIsExist() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
        Collection collection = ShardingSphereServiceLoader.newServiceInstances(TypedSPIFixture.class);
        assertThat(collection.size(), is(1));
    }
    
    @Test
    public void assertRegisterTwice() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
        Collection actualFirstRegister = ShardingSphereServiceLoader.newServiceInstances(TypedSPIFixture.class);
        assertThat(actualFirstRegister.size(), is(1));
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
        Collection actualSecondRegister = ShardingSphereServiceLoader.newServiceInstances(TypedSPIFixture.class);
        assertThat(actualSecondRegister.size(), is(actualFirstRegister.size()));
    }
    
    @SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
    @Test(expected = ServiceLoaderInstantiationException.class)
    public void assertNewInstanceError() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        Field actualField = ShardingSphereServiceLoader.class.getDeclaredField("SERVICE_MAP");
        actualField.setAccessible(true);
        modifiersField.setInt(actualField, actualField.getModifiers() & ~Modifier.FINAL);
        Map<Class<?>, Collection<Class<?>>> actualServiceMap = new ConcurrentHashMap<>();
        actualServiceMap.put(TypedSPIFixture.class, Collections.singleton(TypedSPIFixture.class));
        actualField.set(null, actualServiceMap);
        ShardingSphereServiceLoader.newServiceInstances(TypedSPIFixture.class);
    }
}
