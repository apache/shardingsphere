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

package org.apache.shardingsphere.infra.callback;

import org.apache.shardingsphere.infra.callback.orchestration.DataSourceCallback;
import org.apache.shardingsphere.infra.callback.orchestration.MetaDataCallback;
import org.apache.shardingsphere.infra.callback.orchestration.RuleCallback;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class CallbackTest {
    
    private static final String TEST = "test";
    
    @Test
    public void assertRun() {
        MetaDataCallback.getInstance().run(TEST, new RuleSchemaMetaData(null, null));
        MetaDataCallback.getInstance().register((schemaName, ruleSchemaMetaData) -> {
            assertThat(schemaName, is(TEST));
            assertThat(ruleSchemaMetaData.getClass().getName(), is(RuleSchemaMetaData.class.getName()));
        });
        MetaDataCallback.getInstance().run(TEST, new RuleSchemaMetaData(null, null));
        
        DataSourceCallback.getInstance().run(TEST, new HashMap<>());
        DataSourceCallback.getInstance().register((schemaName, map) -> {
            assertThat(schemaName, is(TEST));
            assertThat(map.size(), is(1));
            map.forEach((k, v) -> assertThat(v.getClass().getName(), is(DataSourceConfiguration.class.getName())));
        });
        Map<String, DataSourceConfiguration> maps = new HashMap<>();
        DataSourceConfiguration configuration = new DataSourceConfiguration("test");
        maps.put(TEST, configuration);
        DataSourceCallback.getInstance().run(TEST, maps);
        
        RuleCallback.getInstance().run(TEST, new ArrayList<>());
        RuleCallback.getInstance().register((schemaName, ruleConfigurations) -> {
            assertThat(schemaName, is(TEST));
            assertFalse(ruleConfigurations.isEmpty());
            ruleConfigurations.forEach(each -> assertThat(each.getClass().getName(), is(TestRuleConfiguration.class.getName())));
        });
        Collection<RuleConfiguration> ruleConfigurations = new ArrayList<>();
        ruleConfigurations.add(new TestRuleConfiguration());
        RuleCallback.getInstance().run(TEST, ruleConfigurations);
    }
    
    static class TestRuleConfiguration implements RuleConfiguration {
    }
}
