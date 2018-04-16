package io.shardingjdbc.core.jdbc.meta;

import io.shardingjdbc.core.jdbc.meta.entity.ActualTableInformation;
import io.shardingjdbc.core.jdbc.meta.entity.ActualTableInformationList;
import io.shardingjdbc.core.jdbc.meta.entity.TableMeta;
import io.shardingjdbc.core.jdbc.meta.handler.TableMetaHandler;
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
 * The meta meta of sharding tables.
 *
 * @author panjuan
 */


@Getter
public final class MetaRegister {
    
    private static MetaRegister INSTANCE;
    
    private  final Map<String, TableMeta> logicTableStructureMap;
    
    private  final Map<String, ActualTableInformationList> logicTableActualTablesMap;
    
    public static synchronized MetaRegister getInstance(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        if ( INSTANCE == null) {
            INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
        }
        return INSTANCE;
    }
    
    private MetaRegister(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        logicTableActualTablesMap = new HashMap<>();
        logicTableStructureMap = new HashMap<>(logicTableActualTablesMap.size());
    
        Collection<TableRule> tableRules = shardingRule.getTableRules();
        calculateLogicTableActualTablesMap(dataSourceMap, tableRules);
        calculateLogicTableStructureMap();
    }
    
    private void calculateLogicTableActualTablesMap(final Map<String, DataSource> dataSourceMap, final Collection<TableRule> tableRules)
    throws SQLException {
        
        for (TableRule tableRule : tableRules) {
            String logicTable = tableRule.getLogicTable();
            ActualTableInformationList actualTableInformationList = getActualTableInformations(dataSourceMap, tableRule);
            logicTableActualTablesMap.put(logicTable, actualTableInformationList);
        }
        
    }
    
    private void calculateLogicTableStructureMap() {
        
        
        for (Map.Entry<String, ActualTableInformationList> entry : logicTableActualTablesMap.entrySet()) {
            
            if (entry.getValue().isAllTableMetaSame()) {
                logicTableStructureMap.put(entry.getKey(), entry.getValue().getActualTableInformationList().get(0).getTableMeta());
            } else {
                throw new ShardingJdbcException("Cannot get uniformed table structure for %s.", entry.getKey());
            }
        
        }
        
    }
    
    public TableMeta getLogicTableStructure(String logicTable) {
        return logicTableStructureMap.get(logicTable);
    }
    
    public ActualTableInformationList getActualTableInformations(String logicTable) {
        return logicTableActualTablesMap.get(logicTable);
    }
    
    public void refresh(Map<String, DataSource> dataSourceMap, ShardingRule shardingRule) throws SQLException {
        INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
    }
    
    private ActualTableInformationList getActualTableInformations(Map<String, DataSource> dataSourceMap, TableRule tableRule) throws SQLException {
        ActualTableInformationList actualTableInformationList = new ActualTableInformationList();
        List<DataNode> actualDataNodes = tableRule.getActualDataNodes();
        for (DataNode dataNode : actualDataNodes) {
            String dataSourceName = dataNode.getDataSourceName();
            DataSource dataSource = dataSourceMap.get(dataSourceName);
            String tableName = dataNode.getTableName();
            TableMeta tableMeta = new TableMetaHandler(dataSource, tableName).getActualTableMeta();
            ActualTableInformation actualTableInformation = new ActualTableInformation(dataSourceName, tableName, tableMeta);
            actualTableInformationList.getActualTableInformationList().add(actualTableInformation);
        }
        return actualTableInformationList;
    }
}
