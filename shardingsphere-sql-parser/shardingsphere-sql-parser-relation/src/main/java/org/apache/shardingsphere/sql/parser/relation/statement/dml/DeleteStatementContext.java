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

package org.apache.shardingsphere.sql.parser.relation.statement.dml;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.relation.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateExtractor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentsAvailable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Delete statement context.
 */
@Getter
@ToString(callSuper = true)
public final class DeleteStatementContext extends CommonSQLStatementContext implements TableSegmentsAvailable {
    
    public DeleteStatementContext(final DeleteStatement sqlStatement) {
        super(sqlStatement);
    }
    
    @Override
    public Collection<TableSegment> getAllTables() {
        DeleteStatement deleteStatement = (DeleteStatement) getSqlStatement();
        Collection<TableSegment> result = new LinkedList<>(deleteStatement.getTables());
        if (deleteStatement.getWhere().isPresent()) {
            result.addAll(getAllTablesFromWhere(deleteStatement.getWhere().get(), deleteStatement.getTables()));
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromWhere(final WhereSegment where, final Collection<TableSegment> tables) {
        Collection<TableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(new PredicateExtractor(tables, predicate).extractTables());
            }
        }
        return result;
    }
}
