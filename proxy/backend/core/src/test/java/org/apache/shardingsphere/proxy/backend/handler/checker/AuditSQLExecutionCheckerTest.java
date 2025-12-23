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

package org.apache.shardingsphere.proxy.backend.handler.checker;

import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class AuditSQLExecutionCheckerTest {
    
    @Test
    void assertCheck() {
        QueryContext queryContext = mock(QueryContext.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        try (MockedStatic<SQLAuditEngine> sqlAuditEngine = mockStatic(SQLAuditEngine.class)) {
            new AuditSQLExecutionChecker().check(mock(), queryContext, database);
            sqlAuditEngine.verify(() -> SQLAuditEngine.audit(queryContext, database));
            sqlAuditEngine.verifyNoMoreInteractions();
        }
    }
}
