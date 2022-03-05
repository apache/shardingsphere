package org.apache.shardingsphere.data.pipeline.core.metadata.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class PipelineTableMetaDataTest {
    private PipelineTableMetaData pipelineTableMetaData;
    private Map<String, PipelineColumnMetaData> columnMetaDataMap;

    @Before
    public void setUp() {
        PipelineColumnMetaData pipelineColumnMetaData = new PipelineColumnMetaData(0, "test", Types.INTEGER, "INTEGER", true);
        columnMetaDataMap.put("test_column", pipelineColumnMetaData);
        pipelineTableMetaData = new PipelineTableMetaData(null, columnMetaDataMap);
    }

    @Test
    public void assertGetColumnMetaDataGivenColumnIndex() {
        List<String> pipelineTableColumns = pipelineTableMetaData.getColumnNames();
        PipelineColumnMetaData actual = columnMetaDataMap.get(pipelineTableColumns.get(0));
        assertThat(actual.getOrdinalPosition(), is(0));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }

    @Test
    public void assertGetColumnMetaDataGivenColumnName() {
        PipelineColumnMetaData actual = columnMetaDataMap.get("test_column");
        assertThat(actual.getOrdinalPosition(), is(0));
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertTrue(actual.isPrimaryKey());
    }

    @Test
    public void assertIsPrimaryKey() {
        assertTrue(pipelineTableMetaData.isPrimaryKey(0));
        assertFalse(pipelineTableMetaData.isPrimaryKey(1));
    }
}