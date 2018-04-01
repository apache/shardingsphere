package io.shardingjdbc.core.merger.dal.show;

import io.shardingjdbc.core.constant.ShardingConstant;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public final class ShowDatabasesMergedResultTest {
    
    private ShowDatabasesMergedResult showDatabasesMergedResult;
    
    @Before
    public void setUp() {
        showDatabasesMergedResult = new ShowDatabasesMergedResult();
    }
    
    @Test
    public void assertNext() {
        assertTrue(showDatabasesMergedResult.next());
        assertFalse(showDatabasesMergedResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() {
        assertThat(showDatabasesMergedResult.getValue(1, Object.class).toString(), is(ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() {
        assertThat(showDatabasesMergedResult.getValue("label", Object.class).toString(), is(ShardingConstant.LOGIC_SCHEMA_NAME));
    
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCalendarValueWithColumnLabel() throws SQLException {
        showDatabasesMergedResult.getCalendarValue("label", Object.class, Calendar.getInstance());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        showDatabasesMergedResult.getInputStream(1, "Ascii");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        showDatabasesMergedResult.getInputStream("label", "Ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(showDatabasesMergedResult.wasNull());
    }
}
