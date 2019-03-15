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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.segment.impl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.DeleteFromWhereSegment;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Map.Entry;

/**
 * Encrypt delete from where filler.
 *
 * @author duhongjun
 */
public class EncryptDeleteFromWhereFiller extends EncryptFromWhereFiller {
    
    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        super.fill(sqlSegment, sqlStatement, sql, encryptRule, shardingTableMetaData);
        DeleteFromWhereSegment deleteFromWhereSegment = (DeleteFromWhereSegment) sqlSegment;
        DMLStatement dmlStatement = (DMLStatement) sqlStatement;
        dmlStatement.setDeleteStatement(true);
        for (Entry<String, String> each : sqlSegment.getTableAliases().entrySet()) {
            dmlStatement.getUpdateTableAlias().put(each.getKey(), each.getValue());
        }
        dmlStatement.setWhereStartIndex(deleteFromWhereSegment.getWhereStartIndex());
        dmlStatement.setWhereStopIndex(deleteFromWhereSegment.getWhereStopIndex());
        dmlStatement.setWhereParameterStartIndex(deleteFromWhereSegment.getWhereParameterStartIndex());
        dmlStatement.setWhereParameterEndIndex(deleteFromWhereSegment.getWhereParameterEndIndex());
    }
}
