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

package org.apache.shardingsphere.sql.parser.binder.statement.dml;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.util.predicate.PredicateExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Delete statement context.
 */
@Getter
@ToString(callSuper = true)
public final class DeleteStatementContext extends CommonSQLStatementContext<DeleteStatement> implements TableAvailable, WhereAvailable {
    
    private final TablesContext tablesContext;
    
    public DeleteStatementContext(final DeleteStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTables());
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>(getSqlStatement().getTables());
        if (getSqlStatement().getWhere().isPresent()) {
            result.addAll(getAllTablesFromWhere(getSqlStatement().getWhere().get()));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getAllTablesFromWhere(final WhereSegment where) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(new PredicateExtractUtils(getSqlStatement().getTables().stream().map(t -> (TableSegment) t).collect(Collectors.toList()), predicate).extractTables());
            }
        }
        return result;
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return getSqlStatement().getWhere();
    }
}
