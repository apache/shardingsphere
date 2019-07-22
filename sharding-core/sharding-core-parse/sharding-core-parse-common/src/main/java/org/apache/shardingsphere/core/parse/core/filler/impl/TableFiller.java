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

package org.apache.shardingsphere.core.parse.core.filler.impl;

import org.apache.shardingsphere.core.parse.core.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;

/**
 * Table filler.
 *
 * @author duhongjun
 * @author zhangliang
 * @author panjuan
 */
public final class TableFiller implements SQLSegmentFiller<TableSegment> {
    
    @Override
    public void fill(final TableSegment sqlSegment, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            ((SelectStatement) sqlStatement).getTables().add(sqlSegment);
        } else if (sqlStatement instanceof InsertStatement) {
            ((InsertStatement) sqlStatement).setTable(sqlSegment);
        } else if (sqlStatement instanceof UpdateStatement) {
            ((UpdateStatement) sqlStatement).getTables().add(sqlSegment);
        } else if (sqlStatement instanceof DeleteStatement) {
            ((DeleteStatement) sqlStatement).getTables().add(sqlSegment);
        } else if (sqlStatement instanceof CreateTableStatement) {
            ((CreateTableStatement) sqlStatement).setTable(sqlSegment);
        } else if (sqlStatement instanceof AlterTableStatement) {
            ((AlterTableStatement) sqlStatement).setTable(sqlSegment);
        } else if (sqlStatement instanceof DropTableStatement) {
            ((DropTableStatement) sqlStatement).getTables().add(sqlSegment);
        } else if (sqlStatement instanceof CreateIndexStatement) {
            ((CreateIndexStatement) sqlStatement).setTable(sqlSegment);
        } else if (sqlStatement instanceof DropIndexStatement) {
            ((DropIndexStatement) sqlStatement).setTable(sqlSegment);
        }
    }
}
