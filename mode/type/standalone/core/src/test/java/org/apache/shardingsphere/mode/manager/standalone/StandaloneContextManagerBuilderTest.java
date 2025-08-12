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
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class StandaloneContextManagerBuilderTest {
    
    @Test
    void assertBuildWithType() throws SQLException {
        assertBuild(TypedSPILoader.getService(ContextManagerBuilder.class, "Standalone"));
    }
    
    @Test
    void assertBuildWithDefault() throws SQLException {
        assertBuild(TypedSPILoader.getService(ContextManagerBuilder.class, null));
    }
    
    @SuppressWarnings("resource")
    void assertBuild(final ContextManagerBuilder builder) throws SQLException {
        InstanceMetaData instanceMetaData = new JDBCInstanceMetaData("foo", "foo_db");
        ContextManager actual = builder.build(new ContextManagerBuilderParameter(createModeConfiguration(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), instanceMetaData), mock(EventBusContext.class));
        assertThat(actual.getComputeNodeInstanceContext().getInstance().getMetaData(), is(instanceMetaData));
    }
    
    private static ModeConfiguration createModeConfiguration() {
        return new ModeConfiguration("STANDALONE", new StandalonePersistRepositoryConfiguration("FIXTURE", new Properties()));
    }
}
