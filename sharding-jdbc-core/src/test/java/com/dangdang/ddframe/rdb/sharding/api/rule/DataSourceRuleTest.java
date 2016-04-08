package com.dangdang.ddframe.rdb.sharding.api.rule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.api.rule.fixture.TestDataSource;
import com.google.common.collect.Sets;

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
    
    @Test(expected = NullPointerException.class)
    public void assertNewDataSourceFailureWhenDataSourceMapIsNull() {
        new DataSourceRule(null);
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
    public void assertGetDataSourceNames() {
        assertThat(dataSourceRule.getDataSourceNames(), is((Collection<String>) Sets.newHashSet("ds0", "ds1", "ds2")));
    }
    
    @Test
    public void assertGetDataSources() {
        assertThat(dataSourceRule.getDataSources(), is(dataSourceMap.values()));
    }
}
