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
import io.shardingsphere.dbtest.config.bean.IntegrateTestCase;
import io.shardingsphere.dbtest.config.bean.IntegrateTestCases;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Integrate test cases loader.
 *
 * @author zhangliang
 */
@Slf4j
public final class IntegrateTestCasesLoader {
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    @Getter
    private final Collection<String> shardingRuleTypes;
    
    private final Map<String, IntegrateTestCase> integrateTestCaseMap;
    
    private IntegrateTestCasesLoader() {
        shardingRuleTypes = new HashSet<>();
        try {
            integrateTestCaseMap = loadIntegrateTestCases();
        } catch (final IOException | URISyntaxException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases() throws IOException, URISyntaxException, JAXBException {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("asserts/");
        Preconditions.checkNotNull(url, "Cannot found integrate test cases.");
        List<String> files = getFiles(url);
        Preconditions.checkNotNull(files, "Cannot found integrate test cases.");
        Map<String, IntegrateTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (String each : files) {
            result.putAll(loadIntegrateTestCases(each));
        }
        return result;
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases(final String file) throws IOException, JAXBException {
        IntegrateTestCases integrateTestCases = unmarshal(file);
        Map<String, IntegrateTestCase> result = new HashMap<>(
                integrateTestCases.getDqlIntegrateTestCases().size() + integrateTestCases.getDmlIntegrateTestCases().size() + integrateTestCases.getDdlIntegrateTestCases().size(), 1);
        result.putAll(loadIntegrateTestCases(file, integrateTestCases.getDqlIntegrateTestCases()));
        result.putAll(loadIntegrateTestCases(file, integrateTestCases.getDmlIntegrateTestCases()));
        result.putAll(loadIntegrateTestCases(file, integrateTestCases.getDdlIntegrateTestCases()));
        return result;
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases(final String file, final List<? extends IntegrateTestCase> integrateTestCases) {
        Map<String, IntegrateTestCase> result = new HashMap<>(integrateTestCases.size(), 1);
        for (IntegrateTestCase each : integrateTestCases) {
            result.put(each.getSqlCaseId(), each);
            each.setPath(file);
            shardingRuleTypes.addAll(each.getShardingRuleTypes());
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
    
    private static IntegrateTestCases unmarshal(final String assertFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(assertFilePath)) {
            return (IntegrateTestCases) JAXBContext.newInstance(IntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    /**
     * Get data set assert.
     * 
     * @param sqlCaseId SQL case ID
     * @return data set assert
     */
    public IntegrateTestCase getIntegrateTestCase(final String sqlCaseId) {
        // TODO resume when transfer finished
//        Preconditions.checkState(integrateTestCaseMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!integrateTestCaseMap.containsKey(sqlCaseId)) {
            log.warn("Have not finish case `{}`", sqlCaseId);
        }
        return integrateTestCaseMap.get(sqlCaseId);
    }
    
    /**
     * Count all data set test cases.
     * 
     * @return count of all data set test cases
     */
    public int countAllDataSetTestCases() {
        return integrateTestCaseMap.size();
    }
}
