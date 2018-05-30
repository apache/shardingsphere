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

import com.google.common.base.Preconditions;
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
import java.util.Map.Entry;

/**
 * Data initializer.
 *
 * @author zhangliang
 */
public final class DataInitializer {
    
    private final Map<String, DatasetDefinition> datasetDefinitionMap;
    
    private final Map<String, String> tableAndSQLMap;
    
    public DataInitializer(final String path) throws IOException, JAXBException {
        DataSetsRoot dataSetsRoot;
        try (FileReader reader = new FileReader(path)) {
            dataSetsRoot = (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        datasetDefinitionMap = loadDatasetDefinitionMap(dataSetsRoot);
        tableAndSQLMap = loadInitialData(dataSetsRoot);
        Preconditions.checkState(!datasetDefinitionMap.isEmpty(), "Use case cannot be parsed.");
        Preconditions.checkState(!tableAndSQLMap.isEmpty(), "Use case cannot initialize the data.");
    }
    
    private Map<String, DatasetDefinition> loadDatasetDefinitionMap(final DataSetsRoot dataSetsRoot) {
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
    
    private Map<String, String> loadInitialData(final DataSetsRoot dataSetsRoot) {
        Map<String, String> result = new LinkedHashMap<>();
        for (DataSetRow each : dataSetsRoot.getDataSetRows()) {
            DataNode dataNode = new DataNode(each.getDataNode());
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
            datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().get(dataNode.getTableName()).add(map);
        }
        return result;
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
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            initializeData(entry.getValue(), datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private void initializeData(final DataSource dataSource, final DatasetDefinition datasetDefinition) throws SQLException, ParseException {
        for (Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.insertUsePreparedStatement(
                        connection, tableAndSQLMap.get(entry.getKey()), datasetDefinition.getDatas().get(entry.getKey()), datasetDefinition.getMetadatas().get(entry.getKey()));
            }
        }
    }
    
    /**
     * Clear data.
     * 
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     */
    public void clearData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            clearData(entry.getValue(), datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private void clearData(final DataSource dataSource, final DatasetDefinition datasetDefinition) throws SQLException {
        for (Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.cleanAllUsePreparedStatement(connection, entry.getKey());
            }
        }
    }
}
