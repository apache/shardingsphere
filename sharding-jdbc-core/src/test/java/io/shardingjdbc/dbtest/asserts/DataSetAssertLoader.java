/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.asserts;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.dbtest.config.bean.DataSetAssert;
import io.shardingjdbc.dbtest.config.bean.DataSetsAssert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data set assert loader.
 *
 * @author zhangliang
 */
@Slf4j
public final class DataSetAssertLoader {
    
    private static final DataSetAssertLoader INSTANCE = new DataSetAssertLoader();
    
    @Getter
    private final Collection<String> shardingRuleTypes;
    
    private final Map<String, DataSetAssert> dataSetAssertMap;
    
    private DataSetAssertLoader() {
        shardingRuleTypes = new HashSet<>();
        try {
            dataSetAssertMap = loadDataSetAssert();
        } catch (final IOException | URISyntaxException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static DataSetAssertLoader getInstance() {
        return INSTANCE;
    }
    
    private Map<String, DataSetAssert> loadDataSetAssert() throws IOException, URISyntaxException, JAXBException {
        URL url = DataSetAssertLoader.class.getClassLoader().getResource("asserts/");
        Preconditions.checkNotNull(url, "Cannot found integrate test cases.");
        List<String> files = getFiles(url);
        Preconditions.checkNotNull(files, "Cannot found integrate test cases.");
        Map<String, DataSetAssert> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (String each : files) {
            result.putAll(loadDataSetAssert(each));
        }
        return result;
    }
    
    private Map<String, DataSetAssert> loadDataSetAssert(final String file) throws IOException, JAXBException {
        DataSetsAssert assertsDefinition = unmarshal(file);
        Map<String, DataSetAssert> result = new HashMap<>(assertsDefinition.getDqlDataSetAsserts().size() + assertsDefinition.getDmlDataSetAsserts().size() + assertsDefinition.getDdlDataSetAsserts().size(), 1);
        shardingRuleTypes.addAll(Arrays.asList(assertsDefinition.getShardingRuleTypes().split(",")));
        result.putAll(loadDataSetAssert(file, assertsDefinition.getDqlDataSetAsserts(), assertsDefinition.getShardingRuleTypes(), assertsDefinition.getDatabaseTypes()));
        result.putAll(loadDataSetAssert(file, assertsDefinition.getDmlDataSetAsserts(), assertsDefinition.getShardingRuleTypes(), assertsDefinition.getDatabaseTypes()));
        result.putAll(loadDataSetAssert(file, assertsDefinition.getDdlDataSetAsserts(), assertsDefinition.getShardingRuleTypes(), assertsDefinition.getDatabaseTypes()));
        return result;
    }
    
    private Map<String, DataSetAssert> loadDataSetAssert(final String file, final List<? extends DataSetAssert> assertDefinitions, final String defaultShardingRuleType, final String defaultDatabaseTypes) {
        Map<String, DataSetAssert> result = new HashMap<>(assertDefinitions.size(), 1);
        for (DataSetAssert each : assertDefinitions) {
            result.put(each.getId(), each);
            each.setPath(file);
            if (Strings.isNullOrEmpty(each.getShardingRuleTypes())) {
                each.setShardingRuleTypes(defaultShardingRuleType);
            } else {
                shardingRuleTypes.addAll(Arrays.asList(each.getShardingRuleTypes().split(",")));
            }
            if (Strings.isNullOrEmpty(each.getDatabaseTypes())) {
                each.setDatabaseTypes(defaultDatabaseTypes);
            }
        }
        return result;
    }
    
    private static List<String> getFiles(final URL url) throws IOException, URISyntaxException {
        final List<String> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().startsWith("assert-") && file.getFileName().toString().endsWith(".xml")) {
                    result.add(file.toFile().getPath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private static DataSetsAssert unmarshal(final String assertFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(assertFilePath)) {
            return (DataSetsAssert) JAXBContext.newInstance(DataSetsAssert.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Get data set assert.
     * 
     * @param sqlCaseId SQL case ID
     * @return data set assert
     */
    public DataSetAssert getDataSetAssert(final String sqlCaseId) {
        // TODO resume when transfer finished
//        Preconditions.checkState(dataSetAssertMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!dataSetAssertMap.containsKey(sqlCaseId)) {
            log.warn("Have not finish case `{}`", sqlCaseId);
        }
        return dataSetAssertMap.get(sqlCaseId);
    }
}
