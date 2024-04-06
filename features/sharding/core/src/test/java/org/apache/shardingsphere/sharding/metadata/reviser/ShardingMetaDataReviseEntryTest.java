package org.apache.shardingsphere.sharding.metadata.reviser;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.sharding.metadata.reviser.schema.ShardingSchemaTableAggregationReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.table.ShardingTableNameReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingMetaDataReviseEntryTest {

    private ShardingMetaDataReviseEntry shardingMetaDataReviseEntry
            = new ShardingMetaDataReviseEntry();

    @Test
    void getSchemaTableAggregationReviser() {
        ConfigurationProperties configurationProperties
                = mock(ConfigurationProperties.class);

        when(configurationProperties.getValue(any()))
                .thenReturn(Boolean.TRUE);

        Optional<ShardingSchemaTableAggregationReviser> result =
                shardingMetaDataReviseEntry.getSchemaTableAggregationReviser(configurationProperties);

        assertTrue(result.isPresent());
    }

    @Test
    void getTableNameReviser() {
        Optional<ShardingTableNameReviser> result = shardingMetaDataReviseEntry.getTableNameReviser();
        assertTrue(result.isPresent());
    }

    @Test
    void getColumnGeneratedReviser() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findTableRuleByActualTable(anyString()))
                .thenReturn(Optional.of(new TableRule(getDataSourceNames(), "table-name")));

        assertTrue(shardingMetaDataReviseEntry.getColumnGeneratedReviser(shardingRule, "table-name").isPresent());
    }

    @Test
    void getIndexReviser() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findTableRuleByActualTable(anyString()))
                .thenReturn(Optional.of(new TableRule(getDataSourceNames(), "table-name")));

        assertTrue(shardingMetaDataReviseEntry.getIndexReviser(shardingRule, "table-name").isPresent());
    }

    @Test
    void getConstraintReviser() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findTableRuleByActualTable(anyString()))
                .thenReturn(Optional.of(new TableRule(getDataSourceNames(), "table-name")));

        assertTrue(shardingMetaDataReviseEntry.getConstraintReviser(shardingRule, "table-name").isPresent());
    }

    @Test
    void getOrder() {
        assertEquals(0, shardingMetaDataReviseEntry.getOrder());
    }

    @Test
    void getTypeClass() {
        assertEquals(ShardingRule.class, shardingMetaDataReviseEntry.getTypeClass());
    }

    private Collection<String> getDataSourceNames() {
        ArrayList<String> dataSourceNamesList  = new ArrayList<>();
        dataSourceNamesList.add("apache");
        dataSourceNamesList.add("graphana");
        dataSourceNamesList.add("node");
        return dataSourceNamesList;
    }
}