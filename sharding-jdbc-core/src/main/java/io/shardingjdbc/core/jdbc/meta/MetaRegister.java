package io.shardingjdbc.core.jdbc.meta;

import io.shardingjdbc.core.jdbc.meta.entity.ActualTableInformationList;
import io.shardingjdbc.core.jdbc.meta.entity.TableMeta;
import io.shardingjdbc.core.jdbc.meta.handler.ActualTableInformationListHandler;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import lombok.Getter;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * The meta meta of sharding tables.
 *
 * @author panjuan
 */

@Getter
public final class MetaRegister {
    
    private static MetaRegister INSTANCE;
    
    private final Map<String, TableMeta> logicTableStructureMap;
    
    private final Map<String, ActualTableInformationList> logicTableActualTablesMap;
    
    private MetaRegister(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        logicTableActualTablesMap = new HashMap<>();
        logicTableStructureMap = new HashMap<>(logicTableActualTablesMap.size());
    
        Collection<TableRule> tableRules = shardingRule.getTableRules();
        calculateLogicTableActualTablesMap(dataSourceMap, tableRules);
        calculateLogicTableStructureMap();
    }
    
    /**
     * To get instance of meta register.
     *
     * @param dataSourceMap The map of string and datasource.
     * @param shardingRule The sharding rule.
     * @return meta register.
     * @throws SQLException SQL exception.
     */
    public static synchronized MetaRegister getInstance(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        if (INSTANCE == null) {
            INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
        }
        return INSTANCE;
    }
    
    private void calculateLogicTableActualTablesMap(final Map<String, DataSource> dataSourceMap, final Collection<TableRule> tableRules)
        throws SQLException {
        for (TableRule tableRule : tableRules) {
            String logicTable = tableRule.getLogicTable();
            List<DataNode> actualDataNodes = tableRule.getActualDataNodes();
            ActualTableInformationList actualTableInformationList = new ActualTableInformationListHandler(actualDataNodes, dataSourceMap).calActualTableInformationList();
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
    
    /**
     * To get logic table meta by logic table name.
     *
     * @param logicTable logic table name.
     * @return table meta.
     */
    public TableMeta getLogicTableMeta(final String logicTable) {
        return logicTableStructureMap.get(logicTable);
    }
    
    /**
     * To get actual table information list by logic table name.
     *
     * @param logicTable logic table name.
     * @return actual table information list.
     */
    public ActualTableInformationList getActualTableInformationList(final String logicTable) {
        return logicTableActualTablesMap.get(logicTable);
    }
    
    /**
     * To refresh the meta register.
     *
     * @param dataSourceMap The map of string and datasource.
     * @param shardingRule sharding rule.
     * @throws SQLException SQL exception.
     */
    public void refresh(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        INSTANCE = new MetaRegister(dataSourceMap, shardingRule);
    }
    
}
