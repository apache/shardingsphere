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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURL;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereLocalFileURLLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ShardingSphereURLLoadEngineTest {
    
    @Test
    void assertLoadContent() {
        final String lineSeparator = System.lineSeparator();
        String content = "foo_driver_fixture_db=2" + lineSeparator + "storage_unit_count=2" + lineSeparator;
        ShardingSphereLocalFileURLLoader urlLoader = mock(ShardingSphereLocalFileURLLoader.class);
        when(urlLoader.load(any(), any())).thenReturn(content);
        try (MockedStatic<TypedSPILoader> typedSPILoaderMockedStatic = mockStatic(TypedSPILoader.class)) {
            typedSPILoaderMockedStatic.when(() -> TypedSPILoader.findService(ShardingSphereLocalFileURLLoader.class, "classpath:")).thenReturn(Optional.of(urlLoader));
            ShardingSphereURLLoadEngine loadEngine = new ShardingSphereURLLoadEngine(ShardingSphereURL.parse("classpath:xxx"));
            assertThat(loadEngine.loadContent(), is(content.getBytes()));
        }
    }
}
