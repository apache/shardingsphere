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

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.bind.JAXBException;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.dbtest.config.bean.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.common.FileUtil;
import io.shardingjdbc.dbtest.common.PathUtil;
import io.shardingjdbc.dbtest.config.AnalyzeConfig;
import io.shardingjdbc.dbtest.exception.DbTestException;
import io.shardingjdbc.dbtest.init.InItCreateSchema;

@RunWith(value = Parameterized.class)
@AllArgsConstructor
public class StartTest {
    
    private String path;
    
    private String id;
    
    private static Properties config = new Properties();
    
    private static final List<String[]> RESULT_ASSERT = new ArrayList<>();
    
    static {
        try {
            config.load(StartTest.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * query value.
     *
     * @param key          key
     * @param defaultValue default Value
     * @return value
     */
    public static String getString(final String key, final String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * query Assert Path.
     *
     * @return Assert Path
     */
    public static String getAssertPath() {
        return getString("assert.path", null);
    }
    
    
    @Parameters
    public static Collection<String[]> getParameters() {
        String assertPath = getAssertPath();
        assertPath = PathUtil.getPath(assertPath);
        List<String> paths = FileUtil.getAllFilePaths(new File(assertPath), "assert-", "xml");
        
        try {
            for (String each : paths) {
                AssertsDefinition assertsDefinition = AnalyzeConfig.analyze(each);
                
                if (StringUtils.isNotBlank(assertsDefinition.getBaseConfig())) {
                    String[] dbs = StringUtils.split(assertsDefinition.getBaseConfig());
                    for (String db : dbs) {
                        InItCreateSchema.addDatabase(db);
                    }
                } else {
                    String[] dbs = new String[]{"db", "dbtbl", "jdbc", "master", "master_only", "nullable", "slave", "slave_only", "tbl"};
                    for (String db : dbs) {
                        InItCreateSchema.addDatabase(db);
                    }
                }
                
                List<AssertDQLDefinition> assertDQLs = assertsDefinition.getAssertDQL();
                collateData(RESULT_ASSERT, each, assertDQLs);
                
                List<AssertDMLDefinition> assertDMLs = assertsDefinition.getAssertDML();
                collateData(RESULT_ASSERT, each, assertDMLs);
                
                List<AssertDDLDefinition> assertDDLs = assertsDefinition.getAssertDDL();
                collateData(RESULT_ASSERT, each, assertDDLs);
                
                AssertEngine.addAssertDefinition(each, assertsDefinition);
            }
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
        return RESULT_ASSERT;
    }
    
    private static <T extends AssertDefinition> void collateData(final List<String[]> result, final String path, final List<T> asserts) {
        if (asserts == null) {
            return;
        }
        List<String> assertDefinitions = new ArrayList<>(asserts.size());
        for (AssertDefinition each : asserts) {
            if (assertDefinitions.contains(each.getId())) {
                throw new DbTestException("ID can't be repeated");
            }
            assertDefinitions.add(each.getId());
            result.add(new String[]{path, each.getId()});
        }
    }
    
    @BeforeClass
    public static void beforeClass() {
        
        if (AssertEngine.isInitialized()) {
            InItCreateSchema.createDatabase();
            InItCreateSchema.createTable();
            AssertEngine.setInitialized(false);
        }
    }
    
    @Test
    public void test() {
        AssertEngine.runAssert(path, id);
    }
    
    @AfterClass
    public static void afterClass() {
        if (AssertEngine.isClean()) {
            InItCreateSchema.dropDatabase();
            AssertEngine.setClean(false);
        }
    }
    
}