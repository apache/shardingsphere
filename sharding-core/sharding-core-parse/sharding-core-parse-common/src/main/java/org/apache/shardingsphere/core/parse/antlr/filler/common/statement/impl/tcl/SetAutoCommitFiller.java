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

package org.apache.shardingsphere.core.parse.antlr.filler.common.statement.impl.tcl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.tcl.SetAutoCommitSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;

/**
 * Set auto commit filler.
 *
 * @author zhangliang
 */
public final class SetAutoCommitFiller implements SQLSegmentCommonFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        SetAutoCommitSegment setAutoCommitSegment = (SetAutoCommitSegment) sqlSegment;
        ((SetAutoCommitStatement) sqlStatement).setAutoCommit(setAutoCommitSegment.isAutoCommit());
    }
}
