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

package org.apache.shardingsphere.proxy.backend.hbase.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert statement checker.
 */
public final class HeterogeneousInsertStatementChecker extends CommonHeterogeneousSQLStatementChecker {
    
    private final InsertStatement sqlStatement;
    
    public HeterogeneousInsertStatementChecker(final InsertStatement sqlStatement) {
        super(sqlStatement);
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    public void execute() {
        checkIsExistsRowKeyInInsertColumns();
        checkIsExistsSubQuery();
        checkValueIsExpected();
        checkOnDuplicateKey();
    }
    
    private void checkIsExistsRowKeyInInsertColumns() {
        List<String> columns = sqlStatement.getColumns().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        Preconditions.checkArgument(!columns.isEmpty(), "The inserted column must be explicitly specified.");
        Preconditions.checkArgument(ALLOW_KEYS.stream().anyMatch(each -> each.equalsIgnoreCase(columns.get(0))), "First column must be rowKey.");
        boolean isExists = columns.subList(1, columns.size()).stream().anyMatch(ALLOW_KEYS::contains);
        Preconditions.checkArgument(!isExists, "Cannot contain multiple rowKeys.");
    }
    
    private void checkIsExistsSubQuery() {
        Preconditions.checkArgument(!sqlStatement.getInsertSelect().isPresent(), "Do not supported `insert into...select...`");
    }
    
    private void checkValueIsExpected() {
        Collection<InsertValuesSegment> values = sqlStatement.getValues();
        for (InsertValuesSegment insertValuesSegment : values) {
            boolean isAllMatch = insertValuesSegment.getValues().stream().allMatch(this::isAllowExpressionSegment);
            Preconditions.checkArgument(isAllMatch, "Value must is literal or parameter marker.");
        }
    }
    
    private void checkOnDuplicateKey() {
        Preconditions.checkArgument(!((MySQLInsertStatement) sqlStatement).getOnDuplicateKeyColumns().isPresent(), "Do not supported ON DUPLICATE KEY UPDATE");
    }
}
