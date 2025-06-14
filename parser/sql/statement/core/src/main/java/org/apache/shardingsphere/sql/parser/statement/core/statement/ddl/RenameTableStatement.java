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

package org.apache.shardingsphere.sql.parser.statement.core.statement.ddl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.TableAvailable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rename table statement.
 */
@RequiredArgsConstructor
@Getter
public final class RenameTableStatement extends AbstractSQLStatement implements DDLStatement, TableAvailable {
    
    private final Collection<RenameTableDefinitionSegment> renameTables;
    
    @Override
    public Collection<SimpleTableSegment> getTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (RenameTableDefinitionSegment each : renameTables) {
            result.add(each.getTable());
            result.add(each.getRenameTable());
        }
        return result;
    }
}
