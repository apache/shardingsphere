/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.routing.type.broadcast;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseBroadcastRoutingEngine.class)
public final class DatabaseBroadcastRoutingEngineTest {


    @Test
    public void assertRoute() throws NoSuchFieldException, IllegalAccessException {
        DatabaseBroadcastRoutingEngine databaseBroadcastRoutingEngine = PowerMockito.mock(DatabaseBroadcastRoutingEngine.class);
        Field field=DatabaseBroadcastRoutingEngine.class.getDeclaredField("shardingRule");
        field.setAccessible(true);
        field.set(databaseBroadcastRoutingEngine, createShardingRule());
        databaseBroadcastRoutingEngine.route();
    }

    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }

    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds0", "ds1");
    }

    private TableRuleConfiguration createTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
}