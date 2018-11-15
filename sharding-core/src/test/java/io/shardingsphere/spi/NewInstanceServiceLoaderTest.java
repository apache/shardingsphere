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

package io.shardingsphere.spi;

import io.shardingsphere.spi.parsing.ParsingHook;
import io.shardingsphere.spi.transaction.xa.DataSourceMapConverter;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NewInstanceServiceLoaderTest {
    
    @Test
    public void assertLoadService() {
        Collection<DataSourceMapConverter> collections = NewInstanceServiceLoader.load(DataSourceMapConverter.class);
        assertThat(collections.size(), is(1));
        assertThat(collections.iterator().next(), instanceOf(DataSourceMapConverter.class));
    }
    
    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void assertRegisterService() {
        NewInstanceServiceLoader.register(DataSourceMapConverter.class);
        Field field = NewInstanceServiceLoader.class.getDeclaredField("SERVICE_MAP");
        field.setAccessible(true);
        Map<Class, Collection<Class<?>>> map = (Map<Class, Collection<Class<?>>>) field.get(null);
        assertThat(map.get(DataSourceMapConverter.class).size(), is(1));
    }
    
    @Test
    public void assertNewServiceInstance() {
        NewInstanceServiceLoader.register(DataSourceMapConverter.class);
        Collection<DataSourceMapConverter> instances = NewInstanceServiceLoader.newServiceInstances(DataSourceMapConverter.class);
        assertThat(instances.size(), is(1));
    }
    
    @Test
    public void assertNewServiceInstanceNotExist() {
        NewInstanceServiceLoader.register(ParsingHook.class);
        Collection collection = NewInstanceServiceLoader.newServiceInstances(ParsingHook.class);
        assertThat(collection.size(), is(0));
    }
}
