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

package org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.segment.impl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.SQLSegmentEncryptFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * Encrypt from where filler.
 *
 * @author duhongjun
 */
public class EncryptFromWhereFiller implements SQLSegmentEncryptFiller<FromWhereSegment> {

    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule,
                     final ShardingTableMetaData shardingTableMetaData) {
        new EncryptOrConditionFiller().fill(sqlSegment.getConditions(), sqlStatement, sql, encryptRule, shardingTableMetaData);
        int count = 0;
        while (count < sqlSegment.getParameterCount()) {
            sqlStatement.increaseParametersIndex();
            count++;
        }
    }

}
