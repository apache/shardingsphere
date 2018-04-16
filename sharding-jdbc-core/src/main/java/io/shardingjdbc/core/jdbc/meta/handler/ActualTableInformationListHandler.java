package io.shardingjdbc.core.jdbc.meta.handler;

import io.shardingjdbc.core.jdbc.meta.entity.ActualTableInformation;
import io.shardingjdbc.core.jdbc.meta.entity.ActualTableInformationList;
import io.shardingjdbc.core.jdbc.meta.entity.TableMeta;
import io.shardingjdbc.core.rule.DataNode;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The actual table information list handler.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public class ActualTableInformationListHandler {
    
    private final List<DataNode> actualDataNodes;
    
    private final Map<String, DataSource> dataSourceMap;
    
    /**
     * To calculate the information of all actual tables.
     *
     * @return The list of actual table information.
     * @throws SQLException SQL exception.
     */
    public ActualTableInformationList calActualTableInformationList() throws SQLException {
        List<ActualTableInformation> actualTableInformationList = new ArrayList<>();
        
        for (DataNode dataNode : actualDataNodes) {
            DataSource dataSource = dataSourceMap.get(dataNode.getDataSourceName());
            ActualTableInformation actualTableInformation = calActualTableInformation(dataNode, dataSource);
            actualTableInformationList.add(actualTableInformation);
        }
        return new ActualTableInformationList(actualTableInformationList);
    }
    
    private ActualTableInformation calActualTableInformation(final DataNode dataNode, final DataSource dataSource) throws SQLException {
        String dataSourceName = dataNode.getDataSourceName();
        String tableName = dataNode.getTableName();
        TableMeta tableMeta = new TableMetaHandler(dataSource, tableName).getActualTableMeta();
        return new ActualTableInformation(dataSourceName, tableName, tableMeta);
    }
    
}
