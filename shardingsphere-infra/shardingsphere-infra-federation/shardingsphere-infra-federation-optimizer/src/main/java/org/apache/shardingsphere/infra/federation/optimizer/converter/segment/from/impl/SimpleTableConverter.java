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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.from.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Simple table converter.
 */
public final class SimpleTableConverter implements SQLSegmentConverter<SimpleTableSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final SimpleTableSegment segment) {
        TableNameSegment tableName = segment.getTableName();
        List<String> names = new ArrayList<>();
        if (segment.getOwner().isPresent()) {
            names.add(segment.getOwner().get().getIdentifier().getValue());
        }
        names.add(tableName.getIdentifier().getValue());
        SqlNode tableNameSQLNode = new SqlIdentifier(names, SqlParserPos.ZERO);
        if (segment.getAlias().isPresent()) {
            SqlNode aliasSQLNode = new SqlIdentifier(segment.getAlias().get(), SqlParserPos.ZERO);
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, Arrays.asList(tableNameSQLNode, aliasSQLNode), SqlParserPos.ZERO));
        }
        return Optional.of(tableNameSQLNode);
    }
}
