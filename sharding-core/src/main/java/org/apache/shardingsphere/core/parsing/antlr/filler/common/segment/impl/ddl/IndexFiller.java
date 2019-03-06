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

package org.apache.shardingsphere.core.parsing.antlr.filler.common.segment.impl.ddl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.definition.index.IndexSegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.token.IndexToken;

/**
 * Index filler.
 *
 * @author duhongjun
 */
public final class IndexFiller implements SQLSegmentCommonFiller<IndexSegment> {
    
    @Override
    public void fill(final IndexSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        IndexToken indexToken = sqlSegment.getToken();
        if (!sqlStatement.getTables().isEmpty() && null == indexToken.getTableName()) {
            indexToken.setTableName(sqlStatement.getTables().getSingleTableName());
        } else {
            indexToken.setTableName("");
        }
        sqlStatement.getSQLTokens().add(sqlSegment.getToken());
    }
}
