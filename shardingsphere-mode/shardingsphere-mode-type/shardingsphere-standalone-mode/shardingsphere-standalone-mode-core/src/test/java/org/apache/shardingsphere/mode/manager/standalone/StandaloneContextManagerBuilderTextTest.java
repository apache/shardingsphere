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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class StandaloneContextManagerBuilderTextTest {
    
    @Test
    public void assertBuild() throws SQLException {
        ContextManager actual = new StandaloneContextManagerBuilder().build(ContextManagerBuilderParameter.builder().modeConfig(new ModeConfiguration("Standalone", null, false))
            .schemaConfigs(Collections.singletonMap("foo_schema", 
                    new DataSourceProvidedSchemaConfiguration(Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.singleton(mock(RuleConfiguration.class)))))
            .globalRuleConfigs(Collections.singleton(mock(RuleConfiguration.class))).props(new Properties())
            .instanceDefinition(new InstanceDefinition(InstanceType.PROXY, 3307)).build());
        assertNotNull(actual.getMetaDataContexts().getMetaDataMap().get("foo_schema"));
        assertNotNull(actual.getMetaDataContexts().getExecutorEngine());
        assertTrue(actual.getMetaDataContexts().getMetaDataPersistService().isPresent());
        PersistRepository repository = actual.getMetaDataContexts().getMetaDataPersistService().get().getRepository();
        assertNotNull(repository.get(GlobalNode.getGlobalRuleNode()));
        assertNotNull(repository.get(SchemaMetaDataNode.getMetaDataDataSourcePath("foo_schema", "0")));
        assertNotNull(repository.get(SchemaMetaDataNode.getRulePath("foo_schema", "0")));
        assertNotNull(actual.getTransactionContexts().getEngines().get("foo_schema"));
    }
}
