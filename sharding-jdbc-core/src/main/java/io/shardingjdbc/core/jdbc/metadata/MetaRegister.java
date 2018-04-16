package io.shardingjdbc.core.jdbc.metadata;

import io.shardingjdbc.core.jdbc.metadata.entity.ActualTableInformation;
import io.shardingjdbc.core.jdbc.metadata.entity.ActualTableInformations;
import io.shardingjdbc.core.jdbc.metadata.entity.TableStructure;
import io.shardingjdbc.core.jdbc.metadata.handler.TableStructureHandler;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The meta metadata of sharding tables.
 *
 * @author panjuan
 */


@Getter
public final class MetaRegister {
    //TODO
    private static MetaRegister INSTANCE;
    
    private  Map<String, TableStructure> logicTableStructureMap;
    
    private  Map<String, ActualTableInformations> logicTableActualTablesMap;
    
    
    
    public static synchronized MetaRegister getInstance(Map<String, DataSource> dataSourceMap, ShardingRule shardingRule) throws SQLException {
        if ( INSTANCE == null) {
            INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
        }
        return INSTANCE;
    }
    
    private MetaRegister(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        Collection<TableRule> tableRules = shardingRule.getTableRules();
        generateLogicTableActualTablesMap(dataSourceMap, tableRules);
        generateLogicTableStructureMap();
    }
    
    private void generateLogicTableActualTablesMap(final Map<String, DataSource> dataSourceMap, final Collection<TableRule> tableRules)
    throws SQLException {
        logicTableActualTablesMap = new HashMap<>();
        
        for (TableRule tableRule : tableRules) {
            String logicTable = tableRule.getLogicTable();
            ActualTableInformations actualTableInformations = getActualTableInformations(dataSourceMap, tableRule);
            logicTableActualTablesMap.put(logicTable, actualTableInformations);
        }
        
    }
    
    private void generateLogicTableStructureMap() {
        logicTableStructureMap = new HashMap<>(logicTableActualTablesMap.size());
        
        for (Map.Entry<String, ActualTableInformations> entry : logicTableActualTablesMap.entrySet()) {
            
            if (entry.getValue().isAllTableStructuresSame()) {
                logicTableStructureMap.put(entry.getKey(), entry.getValue().getActualTableInformationList().get(0).getTableStructure());
            } else {
                throw new ShardingJdbcException("Cannot get uniformed table structure for %s.", entry.getKey());
            }
        
        }
        
    }
    
    public TableStructure getLogicTableStructure(String logicTable) {
        return logicTableStructureMap.get(logicTable);
    }
    
    public ActualTableInformations getActualTableInformations(String logicTable) {
        return logicTableActualTablesMap.get(logicTable);
    }
    
    public void refresh(Map<String, DataSource> dataSourceMap, ShardingRule shardingRule) throws SQLException {
        INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
    }
    
    private ActualTableInformations getActualTableInformations(Map<String, DataSource> dataSourceMap, TableRule tableRule) throws SQLException {
        final ActualTableInformations actualTableInformations = new ActualTableInformations();
        final List<DataNode> actualDataNodes = tableRule.getActualDataNodes();
        for (DataNode dataNode : actualDataNodes) {
            String dataSourceName = dataNode.getDataSourceName();
            DataSource dataSource = dataSourceMap.get(dataSourceName);
            String tableName = dataNode.getTableName();
            final TableStructure tableStructure = new TableStructureHandler(dataSource, tableName).getActualTableStructure();
            final ActualTableInformation actualTableInformation = new ActualTableInformation(dataSourceName, tableName, tableStructure);
            actualTableInformations.getActualTableInformationList().add(actualTableInformation);
        }
        return actualTableInformations;
    }
}
