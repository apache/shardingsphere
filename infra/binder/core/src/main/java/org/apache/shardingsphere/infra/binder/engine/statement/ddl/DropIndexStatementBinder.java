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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;

/**
 * Drop index statement binder.
 */
public final class DropIndexStatementBinder implements SQLStatementBinder<DropIndexStatement> {
    
    @Override
    public DropIndexStatement bind(final DropIndexStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        if (!sqlStatement.getSimpleTable().isPresent()) {
            return sqlStatement;
        }
        return copy(sqlStatement, SimpleTableSegmentBinder.bind(sqlStatement.getSimpleTable().get(), binderContext, LinkedHashMultimap.create()));
    }
    
    private DropIndexStatement copy(final DropIndexStatement sqlStatement, final SimpleTableSegment boundTable) {
        DropIndexStatement result = new DropIndexStatement(sqlStatement.getDatabaseType());
        result.setSimpleTable(boundTable);
        result.getIndexes().addAll(sqlStatement.getIndexes());
        sqlStatement.getAlgorithmType().ifPresent(result::setAlgorithmType);
        sqlStatement.getLockTable().ifPresent(result::setLockTable);
        result.setIfExists(sqlStatement.isIfExists());
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
