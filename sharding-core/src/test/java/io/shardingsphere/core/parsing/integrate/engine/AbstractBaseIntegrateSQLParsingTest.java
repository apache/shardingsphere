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

package io.shardingsphere.core.parsing.integrate.engine;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.yaml.sharding.YamlShardingConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public abstract class AbstractBaseIntegrateSQLParsingTest {
    
    @Getter(AccessLevel.PROTECTED)
    private static ShardingRule shardingRule;
    
    @Getter(AccessLevel.PROTECTED)
    private static ShardingTableMetaData shardingTableMetaData;
    
    @BeforeClass
    public static void setUp() throws IOException {
        shardingRule = buildShardingRule();
        shardingTableMetaData = buildShardingTableMetaData();
    }
    
    private static ShardingRule buildShardingRule() throws IOException {
        URL url = AbstractBaseIntegrateSQLParsingTest.class.getClassLoader().getResource("yaml/parser-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found parser rule yaml configuration.");
        YamlShardingConfiguration yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(url.getFile()));
        return yamlShardingConfig.getShardingRule(yamlShardingConfig.getDataSources().keySet());
    }
    
    private static ShardingTableMetaData buildShardingTableMetaData() {
        ShardingTableMetaData result = Mockito.mock(ShardingTableMetaData.class);
        when(result.containsTable("t_order")).thenReturn(true);
        when(result.containsTable("t_order_item")).thenReturn(true);
        when(result.containsTable("t_place")).thenReturn(true);
        when(result.getAllColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id"));
        when(result.getAllColumnNames("t_order_item")).thenReturn(Arrays.asList("item_id", "order_id", "user_id", "status", "c_date"));
        when(result.getAllColumnNames("t_place")).thenReturn(Arrays.asList("user_new_id", "guid"));
        when(result.containsColumn("t_order_item", "item_id")).thenReturn(true);
        return result;
    }
}
