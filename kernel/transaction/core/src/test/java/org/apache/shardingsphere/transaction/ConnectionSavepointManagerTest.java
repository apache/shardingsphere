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

package org.apache.shardingsphere.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public final class ConnectionSavepointManagerTest {

    private final ConnectionSavepointManager connectionSavepointManager = ConnectionSavepointManager.getInstance();

    @Test
    public void assertSetSavepoint() throws SQLException {
        Connection connection = mock(Connection.class);
        String savingPoint = "SavingPoint";
        connectionSavepointManager.setSavepoint(connection, savingPoint);
        verify(connection, times(1)).setSavepoint(savingPoint);
    }
}
