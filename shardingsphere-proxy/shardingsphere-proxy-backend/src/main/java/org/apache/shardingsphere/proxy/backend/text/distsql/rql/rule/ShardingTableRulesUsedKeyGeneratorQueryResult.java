package org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingTableRulesUsedKeyGeneratorStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ShardingTableRulesUsedKeyGeneratorQueryResult implements DistSQLResultSet {
    
    private static final String SHARDING = "sharding";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        ShardingSphereMetaData tempMetaData = metaData;
        ShowShardingTableRulesUsedKeyGeneratorStatement statement = (ShowShardingTableRulesUsedKeyGeneratorStatement) sqlStatement;
        String schemaName = "";
        if (statement.getSchema().isPresent()) {
            schemaName = statement.getSchema().get().getIdentifier().getValue();
            if (ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
                throw new SchemaNotExistedException(schemaName);
            }
            tempMetaData = ProxyContext.getInstance().getMetaData(schemaName);
        }
        List<Collection<Object>> result = new ArrayList<>();
        Collection<ShardingRuleConfiguration> shardingTableRules = tempMetaData.getRuleMetaData().findRuleConfiguration(ShardingRuleConfiguration.class);
        if (statement.getKeyGeneratorName().isPresent()) {
            shardingTableRules.forEach(shardingTableRule -> shardingTableRule.getTables().stream()
                    .filter(table -> null != table.getKeyGenerateStrategy() && statement.getKeyGeneratorName().get().equals(table.getKeyGenerateStrategy().getKeyGeneratorName()))
                    .forEach(table -> result.add(Arrays.asList(SHARDING, schemaName, table.getLogicTable()))));
        }
        data = result.iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "schema", "name");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowShardingTableRulesUsedKeyGeneratorStatement.class.getName();
    }
}
