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

package io.shardingjdbc.dbtest;

import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.config.bean.AssertDefinition;
import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;
import io.shardingjdbc.dbtest.env.DatabaseEnvironmentManager;
import io.shardingjdbc.dbtest.env.IntegrateTestEnvironment;
import lombok.RequiredArgsConstructor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class StartTest {
    
    private static final String INTEGRATION_RESOURCES_PATH = "asserts";
    
    private static final Collection<String> SHARDING_RULE_TYPES = new HashSet<>();
    
    private static boolean isInitialized = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private static boolean isCleaned = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private final String path;
    
    private final String id;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<String[]> getParameters() throws IOException, JAXBException, URISyntaxException {
        URL integrateResources = StartTest.class.getClassLoader().getResource(INTEGRATION_RESOURCES_PATH);
        assertNotNull(integrateResources);
        List<String[]> result = new LinkedList<>();
        for (String each : getAssertFiles(integrateResources)) {
            AssertsDefinition assertsDefinition = unmarshal(each);
            SHARDING_RULE_TYPES.addAll(Arrays.asList(assertsDefinition.getBaseConfig().split(",")));
            result.addAll(getParameters(each, assertsDefinition.getAssertDQL()));
            result.addAll(getParameters(each, assertsDefinition.getAssertDML()));
            result.addAll(getParameters(each, assertsDefinition.getAssertDDL()));
            AssertEngine.addAssertDefinition(each, assertsDefinition);
        }
        return result;
    }
    
    private static Collection<String[]> getParameters(final String path, final List<? extends AssertDefinition> assertDefinitions) {
        Collection<String[]> result = new LinkedList<>();
        for (AssertDefinition each : assertDefinitions) {
            result.add(new String[] {path, each.getId()});
        }
        return result;
    }
    
    private static List<String> getAssertFiles(final URL integrateResources) throws IOException, URISyntaxException {
        final List<String> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(integrateResources.toURI()), new SimpleFileVisitor<Path>() {
            
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
    
    private static AssertsDefinition unmarshal(final String assertFilePath) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(assertFilePath)) {
            return (AssertsDefinition) JAXBContext.newInstance(AssertsDefinition.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    @BeforeClass
    public static void setUp() throws JAXBException, IOException {
        if (isInitialized) {
            isInitialized = false;
        } else {
            for (String each : SHARDING_RULE_TYPES) {
                DatabaseEnvironmentManager.dropDatabase(each);
            }
        }
        for (String each : SHARDING_RULE_TYPES) {
            DatabaseEnvironmentManager.createDatabase(each);
        }
        for (String each : SHARDING_RULE_TYPES) {
            DatabaseEnvironmentManager.createTable(each);
        }
    }
    
    @AfterClass
    // TODO add tearDown for temporary, will remove when original integrate test removed.
    public static void tearDown() throws JAXBException, IOException {
        if (isCleaned) {
            for (String each : SHARDING_RULE_TYPES) {
                DatabaseEnvironmentManager.dropDatabase(each);
            }
            isCleaned = false;
        }
    }
    
    @Test
    public void test() throws JAXBException {
        AssertEngine.runAssert(path, id);
    }
}
