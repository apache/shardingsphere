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
import io.shardingsphere.dbtest.config.bean.DatasetDefinition;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
     * Get dataset definition map.
     * 
     * @return dataset definition map
     */
    public Map<String, DatasetDefinition> getDatasetDefinitionMap() {
        Map<String, DatasetDefinition> result = new HashMap<>();
        for (DataSetMetadata each : dataSetsRoot.getMetadataList()) {
            for (String dataNodeStr : new InlineExpressionParser(each.getDataNodes()).evaluate()) {
                DataNode dataNode = new DataNode(dataNodeStr);
                if (!result.containsKey(dataNode.getDataSourceName())) {
                    result.put(dataNode.getDataSourceName(), new DatasetDefinition());
                }
                DatasetDefinition datasetDefinition = result.get(dataNode.getDataSourceName());
                datasetDefinition.getMetadatas().put(dataNode.getTableName(), each.getColumnMetadataList());
                datasetDefinition.getIndexMetadataList().put(dataNode.getTableName(), each.getIndexMetadataList());
                result.put(dataNode.getDataSourceName(), datasetDefinition);
            }
        }
        return result;
    }
    
    /**
     * Get initial data.
     * 
     * @param datasetDefinitionMap dataset definition map
     * @return map of table and SQL
     */
    public Map<String, String> getInitialData(final Map<String, DatasetDefinition> datasetDefinitionMap) {
        Map<String, String> result = new LinkedHashMap<>();
        for (DataSetRow each : dataSetsRoot.getDataSetRows()) {
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().containsKey(dataNode.getTableName())) {
                datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), new LinkedList<Map<String, String>>());
            }
            List<Map<String, String>> tableDataList = datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().get(dataNode.getTableName());
            List<String> values = Splitter.on(',').trimResults().splitToList(each.getValues());
            int count = 0;
            Map<String, String> map = new LinkedHashMap<>(values.size(), 1);
            for (DataSetColumnMetadata column : datasetDefinitionMap.get(dataNode.getDataSourceName()).getMetadatas().get(dataNode.getTableName())) {
                map.put(column.getName(), values.get(count));
                count++;
            }
            String sql = DatabaseUtil.analyzeSQL(dataNode.getTableName(), datasetDefinitionMap.get(dataNode.getDataSourceName()).getMetadatas().get(dataNode.getTableName()));
            result.put(dataNode.getTableName(), sql);
            if (!datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().containsKey(dataNode.getTableName())) {
                datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), new LinkedList<Map<String, String>>());
            }
            tableDataList.add(map);
            datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), tableDataList);
        }
        return result;
    }
    
    /**
     * Initialize data.
     * 
     * @param dataSourceMap data source map
     * @param tableAndSQLMap map of table and SQL
     * @param datasetDefinitionMap dataset definition map
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     */
    public void initializeData(
            final Map<String, DataSource> dataSourceMap, final Map<String, String> tableAndSQLMap, final Map<String, DatasetDefinition> datasetDefinitionMap) throws SQLException, ParseException {
        clearData(dataSourceMap, datasetDefinitionMap);
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            initializeData(entry.getValue(), tableAndSQLMap, datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private void initializeData(final DataSource dataSource, final Map<String, String> sqls, final DatasetDefinition datasetDefinition) throws SQLException, ParseException {
        for (Map.Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.insertUsePreparedStatement(connection, sqls.get(entry.getKey()), datasetDefinition.getDatas().get(entry.getKey()), datasetDefinition.getMetadatas().get(entry.getKey()));
            }
        }
    }
    
    /**
     * Clear data.
     * 
     * @param dataSourceMap data source map
     * @param datasetDefinitionMap dataset definition map
     * @throws SQLException SQL exception
     */
    public void clearData(final Map<String, DataSource> dataSourceMap, final Map<String, DatasetDefinition> datasetDefinitionMap) throws SQLException {
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            clearData(entry.getValue(), datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private void clearData(final DataSource dataSource, final DatasetDefinition datasetDefinition) throws SQLException {
        for (Map.Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.cleanAllUsePreparedStatement(connection, entry.getKey());
            }
        }
    }
}
