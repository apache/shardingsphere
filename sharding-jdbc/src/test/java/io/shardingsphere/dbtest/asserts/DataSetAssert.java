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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
}
