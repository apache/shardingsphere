package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.*;

public final class ShardingKeyGeneratorsQueryResultSet implements DistSQLResultSet {

    private static final String NAME = "name";

    private static final String TYPE = "type";

    private static final String PROPS = "props";

    private Iterator<Map.Entry<String, ShardingSphereAlgorithmConfiguration>> data =  Collections.emptyIterator();

    @Override
    public void init(ShardingSphereMetaData metaData, SQLStatement sqlStatement) {

        metaData.getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration)
                .map(each -> (ShardingRuleConfiguration) each)
                .forEach(each -> data = each.getKeyGenerators().entrySet().iterator());
    }

    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME,TYPE,PROPS);
    }

    @Override
    public boolean next() {
        return data.hasNext();
    }

    @Override
    public Collection<Object> getRowData() {
        Map.Entry<String, ShardingSphereAlgorithmConfiguration> entry = data.next();
        return Arrays.asList(entry.getKey(),entry.getValue().getType(),entry.getValue().getProps());
    }

    @Override
    public String getType() {
        return ShowShardingKeyGeneratorsStatement.class.getCanonicalName();
    }
}
