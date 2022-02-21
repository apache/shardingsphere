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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simple table converter.
 */
public final class SimpleTableConverter implements SQLSegmentConverter<SimpleTableSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final SimpleTableSegment segment) {
        TableNameSegment tableName = segment.getTableName();
        List<String> names = new ArrayList<>();
        if (segment.getOwner().isPresent()) {
            names.add(segment.getOwner().get().getIdentifier().getValue());
        }
        names.add(tableName.getIdentifier().getValue());
        SqlNode tableNameSQLNode = new SqlIdentifier(names, SqlParserPos.ZERO);
        if (segment.getAlias().isPresent()) {
            SqlNode aliasSQLNode = new SqlIdentifier(segment.getAlias().get(), SqlParserPos.ZERO);
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[] {tableNameSQLNode, aliasSQLNode}, SqlParserPos.ZERO));
        }
        return Optional.of(tableNameSQLNode);
    }
    
    @Override
    public Optional<SimpleTableSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlBasicCall) {
            SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlNode;
            if (sqlBasicCall.getOperator().equals(SqlStdOperatorTable.AS)) {
                String name = sqlBasicCall.getOperandList().get(0).toString();
                SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(getStartIndex(sqlNode), getStopIndex(sqlNode), new IdentifierValue(name)));
                SqlNode alias = sqlBasicCall.getOperandList().get(1);
                tableSegment.setAlias(new AliasSegment(getStartIndex(alias), getStopIndex(alias), new IdentifierValue(alias.toString())));
                return Optional.of(tableSegment);
            }
        }
        if (sqlNode instanceof SqlIdentifier) {
            List<String> names = ((SqlIdentifier) sqlNode).names;
            SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(getStartIndex(sqlNode), getStopIndex(sqlNode), new IdentifierValue(names.get(names.size() - 1))));
            if (2 == names.size()) {
                simpleTableSegment.setOwner(new OwnerSegment(getStartIndex(sqlNode), getStartIndex(sqlNode) + names.get(0).length() - 1, new IdentifierValue(names.get(0))));
            }
            return Optional.of(simpleTableSegment);
        }
        return Optional.empty();
    }
}
