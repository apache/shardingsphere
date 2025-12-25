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
package org.apache.shardingsphere.mode.manager.builder;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ContextManagerBuilderParameterTest {
    
    @Test
    void assertGetDefaultModeConfiguration() {
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null);
        assertThat(param.getModeConfiguration().getType(), is("Standalone"));
        assertNull(param.getModeConfiguration().getRepository());
    }
    
    @Test
    void assertGetModeConfiguration() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class));
        ContextManagerBuilderParameter param =
                new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null);
        assertThat(param.getModeConfiguration().getType(), is("Cluster"));
        assertNotNull(param.getModeConfiguration().getRepository());
    }
}
