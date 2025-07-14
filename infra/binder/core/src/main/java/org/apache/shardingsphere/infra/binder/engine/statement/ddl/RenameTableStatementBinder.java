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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.infra.binder.engine.segment.ddl.column.RenameTableDefinitionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Rename table statement binder.
 */
public final class RenameTableStatementBinder implements SQLStatementBinder<RenameTableStatement> {
    
    @Override
    public RenameTableStatement bind(final RenameTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Collection<RenameTableDefinitionSegment> boundRenameTables = sqlStatement.getRenameTables().stream()
                .map(each -> RenameTableDefinitionSegmentBinder.bind(each, binderContext, LinkedHashMultimap.create())).collect(Collectors.toList());
        return copy(sqlStatement, boundRenameTables);
    }
    
    private static RenameTableStatement copy(final RenameTableStatement sqlStatement, final Collection<RenameTableDefinitionSegment> boundRenameTables) {
        RenameTableStatement result = new RenameTableStatement(sqlStatement.getDatabaseType(), boundRenameTables);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
