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
import io.shardingsphere.dbtest.config.dataset.expected.ExpectedDataSetsRoot;
import io.shardingsphere.dbtest.config.dataset.init.DataSetColumnMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Data set assert.
 *
 * @author zhangliang
 */
@Slf4j
public final class DataSetAssert {
    
    /**
     * Assert data set.
     *
     * @param actual actual
     * @param expected expected
     */
    public static void assertDataSet(final DataSetDefinitions actual, final ExpectedDataSetsRoot expected) {
        assertData(actual.getDataList().get("data"), expected);
    }
    
    private static void assertData(final List<Map<String, String>> actual, final ExpectedDataSetsRoot expected) {
        assertThat(actual.size(), is(expected.getDataSetRows().size()));
        List<String> expectedColumnNames = Splitter.on(",").trimResults().splitToList(expected.getColumns().getValues());
        int count = 0;
        for (Map<String, String> each : actual) {
            List<String> expectedValues = Splitter.on(",").trimResults().splitToList(expected.getDataSetRows().get(count++).getValues());
            assertData(each, expectedValues, expectedColumnNames);
        }
    }
    
    private static void assertData(final Map<String, String> actual, final List<String> expectedValues, final List<String> expectedColumnNames) {
        assertThat(actual.size(), is(expectedValues.size()));
        assertThat(actual.size(), is(expectedColumnNames.size()));
        int count = 0;
        for (String each : expectedValues) {
            assertThat(actual.get(expectedColumnNames.get(count++)), is(each));
        }
    }
    
    /**
     * Assert data set.
     *
     * @param actual actual
     * @param expected expected
     */
    public static void assertDataSet(final DataSetDefinitions actual, final DataSetDefinitions expected) {
        assertMetadata(actual.getMetadataList(), expected.getMetadataList());
        assertData(actual.getDataList(), expected.getDataList());
    }
    
    private static void assertMetadata(final Map<String, List<DataSetColumnMetadata>> actual, final Map<String, List<DataSetColumnMetadata>> expected) {
        for (Map.Entry<String, List<DataSetColumnMetadata>> entry : expected.entrySet()) {
            List<DataSetColumnMetadata> expectedConfig = entry.getValue();
            List<DataSetColumnMetadata> actualConfig = actual.get(entry.getKey());
            assertNotNull(actualConfig);
            checkConfig(expectedConfig, actualConfig);
        }
    }
    
    private static void assertData(final Map<String, List<Map<String, String>>> actualDataList, final Map<String, List<Map<String, String>>> expectedDataList) {
        assertThat(actualDataList.size(), is(expectedDataList.size()));
        for (Map.Entry<String, List<Map<String, String>>> entry : expectedDataList.entrySet()) {
            List<Map<String, String>> data = entry.getValue();
            List<Map<String, String>> actualDatas = actualDataList.get(entry.getKey());
            assertEquals(actualDatas.size(), data.size());
            checkData(data, actualDatas);
        }
    }
    
    private static void checkData(final List<Map<String, String>> data, final List<Map<String, String>> actualDatas) {
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> expectData = data.get(i);
            Map<String, String> actualData = actualDatas.get(i);
            for (Map.Entry<String, String> entry : expectData.entrySet()) {
                if (!entry.getValue().equals(actualData.get(entry.getKey()))) {
                    String actualMsg = actualDatas.toString();
                    String expectMsg = data.toString();
                    fail("result set validation failed . describe : actual = " + actualMsg + " . expect = " + expectMsg);
                }
            }
        }
    }
    
    private static void checkConfig(final List<DataSetColumnMetadata> expectedConfig, final List<DataSetColumnMetadata> actualConfig) {
        for (DataSetColumnMetadata eachColumn : expectedConfig) {
            boolean flag = false;
            for (DataSetColumnMetadata each : actualConfig) {
                if (eachColumn.getName().equals(each.getName()) && eachColumn.getType().equals(each.getType())) {
                    flag = true;
                }
            }
            assertTrue(flag);
        }
    }
}
