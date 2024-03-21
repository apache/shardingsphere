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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class StandaloneContextManagerBuilderTest {
    
    private StandaloneContextManagerBuilder standaloneContextManagerBuilder;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        standaloneContextManagerBuilder = new StandaloneContextManagerBuilder();
    }
    
    @Test
    void testBuild() throws SQLException {
        ModeConfiguration modeConfig = new ModeConfiguration("Standalone",
                new StandalonePersistRepositoryConfiguration("FIXTURE", new Properties()));
        ContextManagerBuilderParameter parameter = new ContextManagerBuilderParameter(modeConfig,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                PropertiesBuilder.build(new PropertiesBuilder.Property("foo", "foo_value")),
                null, null, false);
        try (ContextManager manager = standaloneContextManagerBuilder.build(parameter)) {
            Assertions.assertNotNull(manager.getInstanceContext());
        }
    }
    
    @Test
    void testGetType() {
        Assertions.assertEquals(standaloneContextManagerBuilder.getType(), "Standalone");
    }
    
    @Test
    void testIsDefault() {
        Assertions.assertTrue(standaloneContextManagerBuilder.isDefault());
    }
}
