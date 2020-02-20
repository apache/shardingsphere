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

package org.apache.shardingsphere.sql.parser.core.filler.dal;

import org.apache.shardingsphere.sql.parser.core.filler.SQLSegmentFiller;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;

/**
 * From schema filler.
 */
public final class FromSchemaFiller implements SQLSegmentFiller<FromSchemaSegment> {
    
    @Override
    public void fill(final FromSchemaSegment sqlSegment, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof ShowTablesStatement) {
            ((ShowTablesStatement) sqlStatement).setFromSchema(sqlSegment);
        }
        if (sqlStatement instanceof ShowTableStatusStatement) {
            ((ShowTableStatusStatement) sqlStatement).setFromSchema(sqlSegment);
        }
        if (sqlStatement instanceof ShowColumnsStatement) {
            ((ShowColumnsStatement) sqlStatement).setFromSchema(sqlSegment);
        }
    }
}
