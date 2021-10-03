package org.apache.shardingsphere.scaling.core.common.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceWrapperTest  {

  private static final String CLIENT_USERNAME = "username";

  private static final String CLIENT_PASSWORD = "password";

  private static final int LOGIN_TIMEOUT = 15;

  @Mock(extraInterfaces = AutoCloseable.class)
  private DataSource dataSource;

  @Mock
  private Connection connection;

  @Mock
  private PrintWriter printWriter;

  @Mock
  private Logger parentLogger;

  @Before
  public void setUp() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(dataSource.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD)).thenReturn(connection);
    when(dataSource.getLogWriter()).thenReturn(printWriter);
    when(dataSource.getLoginTimeout()).thenReturn(LOGIN_TIMEOUT);
    when(dataSource.isWrapperFor(any())).thenReturn(Boolean.TRUE);
    when(dataSource.getParentLogger()).thenReturn(parentLogger);
  }

  @Test
  public void assertGetConnection() throws SQLException {
    DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(dataSource);
    assertThat(dataSourceWrapper.getConnection(), is(connection));
    assertThat(dataSourceWrapper.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD), is(connection));
    assertGetLogWriter(dataSourceWrapper.getLogWriter());
    assertGetLoginTimeout(dataSourceWrapper.getLoginTimeout());
    assertIsWrappedFor(dataSourceWrapper.isWrapperFor(any()));
    assertGetParentLogger(dataSourceWrapper.getParentLogger());
  }

  private void assertGetLogWriter(final PrintWriter actual) {
    assertThat(actual, is(printWriter));
  }

  private void assertGetLoginTimeout(final int actual) {
    assertThat(actual, is(LOGIN_TIMEOUT));
  }

  private void assertIsWrappedFor(final boolean actual) {
    assertThat(actual, is(Boolean.TRUE));
  }

  private void assertGetParentLogger(final Logger actual) {
    assertThat(actual, is(parentLogger));
  }

  @Test(expected = SQLException.class)
  public void assertSetLoginTimeoutFailure() throws SQLException {
    doThrow(new SQLException("")).when(dataSource).setLoginTimeout(LOGIN_TIMEOUT);
    new DataSourceWrapper(dataSource).setLoginTimeout(LOGIN_TIMEOUT);
  }

  @Test(expected = SQLException.class)
  public void assertSetLogWriterFailure() throws SQLException {
    doThrow(new SQLException("")).when(dataSource).setLogWriter(printWriter);
    new DataSourceWrapper(dataSource).setLogWriter(printWriter);
  }

  @Test(expected = SQLException.class)
  public void assertCloseFailure() throws Exception {
    doThrow(new Exception("")).when((AutoCloseable) dataSource).close();
    new DataSourceWrapper(dataSource).close();
  }
}