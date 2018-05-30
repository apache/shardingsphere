/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.asserts;

import com.google.common.base.Splitter;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.config.dataset.DataSetColumnMetadata;
import io.shardingsphere.dbtest.config.dataset.DataSetMetadata;
import io.shardingsphere.dbtest.config.dataset.DataSetRow;
import io.shardingsphere.dbtest.config.dataset.DataSetsRoot;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data initializer.
 *
 * @author zhangliang
 */
public final class DataInitializer {
    
    private final DataSetsRoot dataSetsRoot;
    
    public DataInitializer(final String path) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(path)) {
            dataSetsRoot = (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Initialize data.
     * 
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     */
    public void initializeData(final Map<String, DataSource> dataSourceMap) throws SQLException, ParseException {
        clearData(dataSourceMap);
        Map<DataNode, List<DataSetRow>> dataNodeListMap = getDataSetRowMap();
        for (Entry<DataNode, List<DataSetRow>> entry : dataNodeListMap.entrySet()) {
            DataNode dataNode = entry.getKey();
            List<DataSetRow> dataSetRows = entry.getValue();
            DataSetMetadata dataSetMetadata = dataSetsRoot.findDataSetMetadata(dataNode);
            String sql = DatabaseUtil.analyzeSQL(dataNode.getTableName(), dataSetMetadata.getColumnMetadataList());
            List<Map<String, String>> valueMaps = new LinkedList<>();
            for (DataSetRow row : dataSetRows) {
                valueMaps.add(getValueMap(row, dataSetMetadata));
            }
            try (Connection connection = dataSourceMap.get(dataNode.getDataSourceName()).getConnection()) {
                DatabaseUtil.insertUsePreparedStatement(connection, sql, valueMaps, dataSetMetadata.getColumnMetadataList());
            }
        }
    }
    
    private Map<DataNode, List<DataSetRow>> getDataSetRowMap() {
        Map<DataNode, List<DataSetRow>> result = new LinkedHashMap<>();
        for (DataSetRow each : dataSetsRoot.getDataSetRows()) {
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!result.containsKey(dataNode)) {
                result.put(dataNode, new LinkedList<DataSetRow>());
            }
            result.get(dataNode).add(each);
        }
        return result;
    }
    
    private Map<String, String> getValueMap(final DataSetRow dataSetRow, final DataSetMetadata dataSetMetadata) {
        List<String> values = Splitter.on(',').trimResults().splitToList(dataSetRow.getValues());
        int count = 0;
        Map<String, String> result = new LinkedHashMap<>(values.size(), 1);
        for (DataSetColumnMetadata column : dataSetMetadata.getColumnMetadataList()) {
            result.put(column.getName(), values.get(count));
            count++;
        }
        return result;
    }
    
    /**
     * Clear data.
     * 
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     */
    public void clearData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        DataNodes dataNodes = new DataNodes();
        for (DataSetMetadata each : dataSetsRoot.getMetadataList()) {
            dataNodes.add(getDataNodes(each));
        }
        for (String each : dataNodes.getDataSourceNames()) {
            clearData(dataSourceMap, each, dataNodes);
        }
    }
    
    private void clearData(final Map<String, DataSource> dataSourceMap, final String dataSourceName, final DataNodes dataNodes) throws SQLException {
        try (Connection connection = dataSourceMap.get(dataSourceName).getConnection()) {
            for (String each : dataNodes.getTableNames(dataSourceName)) {
                DatabaseUtil.cleanAllUsePreparedStatement(connection, each);
            }
        }
    }
    
    private List<DataNode> getDataNodes(final DataSetMetadata dataSetMetadata) {
        List<DataNode> result = new LinkedList<>();
        for (String each : new InlineExpressionParser(dataSetMetadata.getDataNodes()).evaluate()) {
            DataNode dataNode = new DataNode(each);
            result.add(dataNode);
        }
        return result;
    }
}
