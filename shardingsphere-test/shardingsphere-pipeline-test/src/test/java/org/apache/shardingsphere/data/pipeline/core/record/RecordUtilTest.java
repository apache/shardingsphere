package org.apache.shardingsphere.data.pipeline.core.record;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public final class RecordUtilTest {
    @Test
    public void assertExtractPrimaryColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        List<Column> actual = RecordUtil.extractPrimaryColumns(dataRecord);
        assertThat(actual.size(), is(2));
    }

    @Test
    public void assertExtractUpdatedColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        List<Column> actual = RecordUtil.extractUpdatedColumns(dataRecord);
        assertThat(actual.size(), is(3));
    }

    @Test
    public void assertExtractConditionalColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        List<Column> actual = RecordUtil.extractConditionColumns(dataRecord, Collections.singleton("c1"));
        assertThat(actual.size(), is(3));
    }

    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, true));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
