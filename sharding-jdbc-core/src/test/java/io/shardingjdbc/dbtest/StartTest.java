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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.common.ConfigRuntime;
import io.shardingjdbc.dbtest.common.FileUtils;
import io.shardingjdbc.dbtest.common.PathUtils;
import io.shardingjdbc.dbtest.config.AnalyzeConfig;
import io.shardingjdbc.dbtest.config.bean.AssertDefinition;
import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;
import io.shardingjdbc.dbtest.exception.DbTestException;
import io.shardingjdbc.dbtest.init.InItCreateSchema;

@RunWith(value = Parameterized.class)
public class StartTest {

    private String path;

    private String id;

    public StartTest(final String path, final String id) {
        this.path = path;
        this.id = id;
    }

    @Parameters
    public static Collection<String[]> getParams() {

        String assertPath = ConfigRuntime.getAssertPath();
        assertPath = PathUtils.getPath(assertPath);
        List<String> paths = FileUtils.getAllFilePaths(new File(assertPath), "assert-", "xml");
        List<String[]> result = new ArrayList<>();

        try {
            for (String each : paths) {
                AssertsDefinition assertsDefinition = AnalyzeConfig.analyze(each);
                List<AssertDefinition> asserts = assertsDefinition.getAsserts();
                List<String> ls = new ArrayList<>();
                for (AssertDefinition eachAssertDefinition : asserts) {
                    if (ls.contains(eachAssertDefinition.getId())) {
                        throw new DbTestException("ID can't be repeated");
                    }
                    result.add(new String[]{each, eachAssertDefinition.getId()});
                }
                AssertEngine.addAssertDefinition(each, assertsDefinition);
            }
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @BeforeClass
    public static void beforeClass() {
        if (ConfigRuntime.isInitialized()) {
            InItCreateSchema.createDatabase();
            InItCreateSchema.initTable();
        }
    }

    @Test
    public void test() {
        AssertEngine.runAssert(path, id);
    }

    @AfterClass
    public static void afterClass() {
        if (ConfigRuntime.isInitialized()) {
            InItCreateSchema.dropDatabase();
        }
    }

}