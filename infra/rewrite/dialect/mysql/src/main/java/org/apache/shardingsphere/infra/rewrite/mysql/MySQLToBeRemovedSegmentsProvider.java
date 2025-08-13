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

package org.apache.shardingsphere.infra.rewrite.mysql;

import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.generic.DialectToBeRemovedSegmentsProvider;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Dialect to be removed segments provider.
 */
public final class MySQLToBeRemovedSegmentsProvider implements DialectToBeRemovedSegmentsProvider {
    
    @Override
    public Collection<SQLSegment> getToBeRemovedSQLSegments(final SQLStatement sqlStatement) {
        Collection<SQLSegment> result = new LinkedList<>();
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            ((MySQLShowTablesStatement) sqlStatement).getFromDatabase().ifPresent(result::add);
        }
        if (sqlStatement instanceof MySQLShowColumnsStatement) {
            ((MySQLShowColumnsStatement) sqlStatement).getFromDatabase().ifPresent(result::add);
        }
        if (sqlStatement instanceof MySQLShowIndexStatement) {
            ((MySQLShowIndexStatement) sqlStatement).getFromDatabase().ifPresent(result::add);
        }
        if (sqlStatement instanceof MySQLShowTableStatusStatement) {
            ((MySQLShowTableStatusStatement) sqlStatement).getFromDatabase().ifPresent(result::add);
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
