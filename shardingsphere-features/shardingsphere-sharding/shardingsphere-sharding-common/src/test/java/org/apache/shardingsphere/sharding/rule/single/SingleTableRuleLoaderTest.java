package org.apache.shardingsphere.sharding.rule.single;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.val;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingleTableRuleLoaderTest {

    private static final String TABLE_TYPE = "TABLE";

    private static final String VIEW_TYPE = "VIEW";

    private static final String TABLE_NAME = "TABLE_NAME";

    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    @Mock
    private DatabaseType dbType;

    private DataSource initDataSource(String dataSourceName, Set<String> tables) throws SQLException {
        if (Strings.isNullOrEmpty(dataSourceName) || tables == null) {
            throw new IllegalArgumentException("dataSourceNam is empty or tables is null");
        }
        val dataSource = mock(DataSource.class);
        val conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn(dataSourceName);

        val resultSet = mock(ResultSet.class);
        val tableList = Lists.newArrayList(tables);
        if (tableList.size() == 0) {
            when(resultSet.next()).thenReturn(false);
        } else if (tableList.size() == 1) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0));
        } else {
            val subTableList = tableList.subList(1, tables.size());
            val subNextList = subTableList.stream()
                .map(item -> true)
                .collect(Collectors.toList());
            subNextList.add(false);
            val subTableArray = new String[subTableList.size()];
            val subNextArray = new Boolean[subNextList.size()];
            subTableList.toArray(subTableArray);
            subNextList.toArray(subNextArray);
            when(resultSet.next()).thenReturn(true, subNextArray);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0), subTableArray);
        }

        val metaData = mock(DatabaseMetaData.class);
        when(conn.getMetaData()).thenReturn(metaData);

        when(
            metaData.getTables(conn.getCatalog(), conn.getSchema(), null, new String[]{TABLE_TYPE, VIEW_TYPE})
        ).thenReturn(resultSet);
        return dataSource;
    }

    @Before
    public void init() throws SQLException {
        DataSource ds1 = initDataSource("ds1", Sets.newHashSet("employee", "dept", "salary"));
        DataSource ds2 = initDataSource("ds2", Sets.newHashSet("student", "teacher", "class", "salary"));
        dataSourceMap.put("ds1", ds1);
        dataSourceMap.put("ds2", ds2);
    }

    @Test
    public void assertLoad() throws SQLException {
        Map<String, SingleTableRule> singleTableRuleMap = SingleTableRuleLoader.load(dbType, dataSourceMap, Collections.emptyList());
        Set<String> tableSet = singleTableRuleMap.keySet();
        assertTrue(tableSet.contains("employee"));
        assertTrue(tableSet.contains("dept"));
        assertTrue(tableSet.contains("salary"));
        assertTrue(tableSet.contains("student"));
        assertTrue(tableSet.contains("teacher"));
        assertTrue(tableSet.contains("class"));
    }


}
