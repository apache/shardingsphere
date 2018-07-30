package io.shardingsphere.core.metadata.datasource;

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ShardingDataSourceMetaDataTest {
    
    private ShardingDataSourceMetaData actual;
    
    @Before
    public void setUp() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds_0", "ds_1"));
        Map<String, String> dataSourceURLs = new LinkedHashMap<String, String>() {{ put("ds_0", "jdbc:mysql://127.0.0.1:3306/db_0"); put("ds_1", "jdbc:mysql://127.0.0.1:3306/db_1");}};
        actual = new ShardingDataSourceMetaData(dataSourceURLs, shardingRule, DatabaseType.MySQL);
    }
    
    @Test
    public void testGetAllInstanceDataSourceNames() {
        assertEquals(actual.getAllInstanceDataSourceNames(), Lists.newArrayList("ds_0"));
    }
    
    @Test
    public void testGetActualSchemaName() {
        assertEquals(actual.getActualSchemaName("ds_0"), "db_0");
    }
}