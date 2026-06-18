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

package org.apache.shardingsphere.sql.parser.statement.doris.dal;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.RuleNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Drop SQL block rule statement for Doris.
 */
@Getter
@Setter
public final class DorisDropSqlBlockRuleStatement extends DALStatement {
    
    private final Collection<RuleNameSegment> ruleNames = new LinkedList<>();
    
    public DorisDropSqlBlockRuleStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
}
