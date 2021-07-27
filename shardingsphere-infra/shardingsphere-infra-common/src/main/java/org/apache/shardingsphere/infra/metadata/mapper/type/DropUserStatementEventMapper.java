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

package org.apache.shardingsphere.infra.metadata.mapper.type;

import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.DropUserStatementEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DropUserStatement;

import java.util.Collection;

/**
 * Drop user statement event mapper.
 */
public final class DropUserStatementEventMapper implements SQLStatementEventMapper {
    
    @Override
    public DropUserStatementEvent map(final SQLStatement sqlStatement) {
        return new DropUserStatementEvent(getUsers((DropUserStatement) sqlStatement));
    }
    
    private Collection<String> getUsers(final DropUserStatement sqlStatement) {
        return sqlStatement.getUsers();
    }
}
