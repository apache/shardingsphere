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

package org.apache.shardingsphere.infra.url.core;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereURLLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
public class ShardingSphereURLLoadEngineTest {
    
    @Test
    void assertLoadContent() {
        String content = "foo_driver_fixture_db=2\nstorage_unit_count=2\n";
        ShardingSphereURL url = mock(ShardingSphereURL.class);
        ShardingSphereURLLoader urlLoader = mock(ShardingSphereURLLoader.class);
        
        when(url.getSourceType()).thenReturn("classpath:");
        when(url.getQueryProps()).thenReturn(new Properties());
        when(urlLoader.load(any(), any())).thenReturn(content);
        
        MockedStatic<TypedSPILoader> typedSPILoaderMockedStatic = mockStatic(TypedSPILoader.class);
        typedSPILoaderMockedStatic.when(() -> TypedSPILoader.getService(ShardingSphereURLLoader.class, "classpath:")).thenReturn(urlLoader);
        ShardingSphereURLLoadEngine shardingSphereURLLoadEngine = new ShardingSphereURLLoadEngine(url);
        
        assertThat(shardingSphereURLLoadEngine.loadContent(), is(content.getBytes()));
    }
}
