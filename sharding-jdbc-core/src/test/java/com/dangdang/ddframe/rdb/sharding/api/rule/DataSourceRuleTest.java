/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.fixture.TestDataSource;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class DataSourceRuleTest {
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>(3);
    
    private DataSourceRule dataSourceRule;
    
    @Before
    public void setUp() {
        dataSourceMap.put("ds0", new TestDataSource("ds0"));
        dataSourceMap.put("ds1", new TestDataSource("ds1"));
        dataSourceMap.put("ds2", new TestDataSource("ds2"));
        dataSourceRule = new DataSourceRule(dataSourceMap);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertNewDataSourceFailureWhenDataSourceMapIsEmpty() {
        new DataSourceRule(Collections.<String, DataSource>emptyMap());
    }
    
    @Test
    public void assertGetDataSource() {
        assertDataSource("ds0");
        assertDataSource("ds1");
        assertDataSource("ds2");
    }
    
    private void assertDataSource(final String dataSourceName) {
        assertThat(dataSourceRule.getDataSource(dataSourceName), is((DataSource) new TestDataSource(dataSourceName)));
    }
    
    @Test
    public void assertGetDefaultDataSourceWhenNotSet() {
        assertFalse(dataSourceRule.getDefaultDataSource().isPresent());
    }
    
    @Test
    public void assertGetDefaultDataSourceWithSingleDataSource() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds0", new TestDataSource("ds0"));
        dataSourceRule = new DataSourceRule(dataSourceMap);
        assertThat(dataSourceRule.getDefaultDataSource().get(), is(dataSourceRule.getDataSource("ds0")));
    }
    
    @Test
    public void assertGetDefaultDataSource() {
        dataSourceRule = new DataSourceRule(dataSourceMap, "ds0");
        assertThat(dataSourceRule.getDefaultDataSource().get(), is(dataSourceRule.getDataSource("ds0")));
    }
    
    @Test
    public void assertGetDataSourceNames() {
        assertThat(dataSourceRule.getDataSourceNames(), is((Collection<String>) Sets.newHashSet("ds0", "ds1", "ds2")));
    }
    
    @Test
    public void assertGetDataSources() {
        assertThat(dataSourceRule.getDataSources(), is(dataSourceMap.values()));
    }
}
