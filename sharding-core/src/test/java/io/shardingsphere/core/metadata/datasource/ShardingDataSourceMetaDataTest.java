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
    
    private ShardingDataSourceMetaData masterSlaveShardingDataSourceMetaData;
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    @Before
    public void setUp() {
        Map<String, String> masterSlaveShardingDataSourceURLs = new LinkedHashMap<String, String>() {{ put("master_0", "jdbc:mysql://127.0.0.1:3306/master_0");
        put("master_1", "jdbc:mysql://127.0.0.1:3306/master_1"); put("master_2", "jdbc:mysql://127.0.0.1:3307/master_2"); put("slave_0", "jdbc:mysql://127.0.0.2:3306/slave_0");
            put("slave_1", "jdbc:mysql://127.0.0.2:3306/slave_1"); put("slave_2", "jdbc:mysql://127.0.0.2:3307/slave_2");}};
        masterSlaveShardingDataSourceMetaData = new ShardingDataSourceMetaData(masterSlaveShardingDataSourceURLs, getMasterSlaveShardingRule(), DatabaseType.MySQL);
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<String, String>() {{ put("ds_0", "jdbc:mysql://127.0.0.1:3306/db_0"); put("ds_1", "jdbc:mysql://127.0.0.1:3306/db_1"); }};
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(shardingDataSourceURLs, getShardingRule(), DatabaseType.MySQL);
    }
    
    private ShardingRule getMasterSlaveShardingRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ms_${0..2}.t_order_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        MasterSlaveRuleConfiguration msConfig_0 = new MasterSlaveRuleConfiguration("ms_0", "master_0", Arrays.asList("slave_0"));
        MasterSlaveRuleConfiguration msConfig_1 = new MasterSlaveRuleConfiguration("ms_1", "master_1", Arrays.asList("slave_1"));
        MasterSlaveRuleConfiguration msConfig_2 = new MasterSlaveRuleConfiguration("ms_2", "master_2", Arrays.asList("slave_2"));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getMasterSlaveRuleConfigs().addAll(Lists.newArrayList(msConfig_0, msConfig_1, msConfig_2));
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("master_0", "master_1", "master_2", "slave_0", "slave_1", "slave_2"));
    }
    
    private ShardingRule getShardingRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds_0", "ds_1"));
    }
    
    @Test
    public void testGetAllInstanceDataSourceNames() {
        assertEquals(masterSlaveShardingDataSourceMetaData.getAllInstanceDataSourceNames(), Lists.newArrayList("ms_0", "ms_2"));
        assertEquals(shardingDataSourceMetaData.getAllInstanceDataSourceNames(), Lists.newArrayList("ds_0"));
    }
    
    @Test
    public void testGetActualSchemaName() {
        assertEquals(masterSlaveShardingDataSourceMetaData.getActualSchemaName("ms_0"), "master_0");
        assertEquals(shardingDataSourceMetaData.getActualSchemaName("ds_0"), "db_0");
    }
}