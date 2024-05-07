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

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class QuietlyCloserTest {
    
    @Test
    void assertClose() throws Exception {
        AutoCloseable mockAutoCloseable = mock(AutoCloseable.class);
        doThrow(new SQLException("test")).when(mockAutoCloseable).close();
        QuietlyCloser.close(mockAutoCloseable);
        assertDoesNotThrow(() -> verify(mockAutoCloseable, times(1)).close());
    }
    
    @Test
    void assertCloseWithNullResource() {
        assertDoesNotThrow(() -> QuietlyCloser.close(null));
    }
}
