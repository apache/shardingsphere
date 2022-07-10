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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

/**
 * Default session variable handler for PostgreSQL.
 */
@Slf4j
public final class DefaultPostgreSQLSessionVariableHandler implements PostgreSQLSessionVariableHandler {
    
    @Override
    public void handle(final ConnectionSession connectionSession, final SetStatement setStatement) {
        log.debug("Set statement {} was discarded.", setStatement.getVariableAssigns().stream().findFirst()
                .map(segment -> String.format("%s = %s", segment.getVariable().getVariable(), segment.getAssignValue())).orElseGet(setStatement::toString));
    }
}
