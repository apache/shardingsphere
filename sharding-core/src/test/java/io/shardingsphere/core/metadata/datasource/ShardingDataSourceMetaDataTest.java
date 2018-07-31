package io.shardingsphere.core.metadata.datasource;

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ShardingDataSourceMetaDataTest {
    
    private ShardingDataSourceMetaData actual;
    
    @Before
    public void setUp() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("master_${0..2}.t_order_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        MasterSlaveRuleConfiguration msConfig = new MasterSlaveRuleConfiguration("ms_0", "master_0", Arrays.asList("slave_0"));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(msConfig);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Lists.newArrayList("master_0", "master_1", "master_2", "slave_0", "slave_1", "slave_2"));
        Map<String, String> dataSourceURLs = new LinkedHashMap<String, String>() {{ put("master_0", "jdbc:mysql://127.0.0.1:3306/master_0");
        put("master_1", "jdbc:mysql://127.0.0.1:3306/master_1"); put("master_2", "jdbc:mysql://127.0.0.1:3307/master_2"); put("slave_0", "jdbc:mysql://127.0.0.2:3306/slave_0");}};
        actual = new ShardingDataSourceMetaData(dataSourceURLs, shardingRule, DatabaseType.MySQL);
    }
    
    @Test
    public void testGetAllInstanceDataSourceNames() {
        assertEquals(actual.getAllInstanceDataSourceNames(), Lists.newArrayList("master_0", "master_2"));
    }
    
    @Test
    public void testGetActualSchemaName() {
        assertEquals(actual.getActualSchemaName("master_0"), "master_0");
    }
}