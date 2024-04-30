/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.util.close;

import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class DataSourcesCloserTest {
    
    @Test
    void assertClose() throws Exception {
        DataSource dataSource0 = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        DataSource dataSource1 = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        doThrow(new SQLException("test")).when((AutoCloseable) dataSource1).close();
        DataSource dataSource2 = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        assertThrows(SQLWrapperException.class, () -> DataSourcesCloser.close(Arrays.asList(dataSource0, dataSource1, dataSource2)));
        verify((AutoCloseable) dataSource0, times(1)).close();
        verify((AutoCloseable) dataSource1, times(1)).close();
        verify((AutoCloseable) dataSource2, times(1)).close();
    }
}
