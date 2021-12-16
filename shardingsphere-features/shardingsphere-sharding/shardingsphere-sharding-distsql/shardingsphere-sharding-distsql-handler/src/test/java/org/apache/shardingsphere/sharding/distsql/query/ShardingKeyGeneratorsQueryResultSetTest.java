package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingKeyGeneratorsQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingKeyGeneratorsQueryResultSetTest {

    @Test
    public void assertGetRowData(){
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(createRuleConfigurations());
        ShardingKeyGeneratorsQueryResultSet resultSet = new ShardingKeyGeneratorsQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingKeyGeneratorsStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0),is("snowflake"));
        assertThat(actual.get(1), is("SNOWFLAKE"));
        assertThat(actual.get(2).toString(),is("{work-id=123}"));
    }

    private Collection<RuleConfiguration> createRuleConfigurations(){
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Properties props = new Properties();
        props.put("work-id", 123);
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", props));
        return Collections.singleton(result);
    }


}
