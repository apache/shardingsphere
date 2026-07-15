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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementObjectExtractor {
    
    private static final Set<String> DCL_OBJECT_TYPE_KEYWORDS = Set.of("TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA", "FUNCTION", "PROCEDURE");
    
    private final SQLStatementScanner scanner;
    
    Collection<SQLStatementObjectName> extract(final SQLStatement sqlStatement, final String sql) {
        Collection<SQLStatementObjectName> result = new LinkedHashSet<>();
        extractDirectTargets(sqlStatement, result);
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSQLStatement(sqlStatement);
        addTableSegments(tableExtractor.getTableContext(), result);
        sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).ifPresent(attribute -> addTables(attribute.getTables(), result));
        extractMergeTables(sqlStatement, result);
        extractCommonTableExpressionTables(sqlStatement, result);
        removeCommonTableExpressionAliases(sqlStatement, result);
        List<SQLStatementToken> tokens = scanner.tokenize(sql);
        extractDCLTarget(sqlStatement, tokens, result);
        extractQualifiedFunctions(tokens, result);
        return result;
    }
    
    private void extractDCLTarget(final SQLStatement sqlStatement, final List<SQLStatementToken> tokens, final Collection<SQLStatementObjectName> result) {
        if (!(sqlStatement instanceof GrantStatement) && !(sqlStatement instanceof RevokeStatement)) {
            return;
        }
        for (int index = 0; index < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), "ON")) {
                int objectStartIndex = skipDCLObjectType(tokens, index + 1);
                addObjectName(tokens, objectStartIndex, findObjectNameEnd(tokens, objectStartIndex), result);
                return;
            }
        }
    }
    
    private int skipDCLObjectType(final List<SQLStatementToken> tokens, final int startIndex) {
        return startIndex < tokens.size() && DCL_OBJECT_TYPE_KEYWORDS.contains(tokens.get(startIndex).upperText()) ? startIndex + 1 : startIndex;
    }
    
    private void extractQualifiedFunctions(final List<SQLStatementToken> tokens, final Collection<SQLStatementObjectName> result) {
        int index = 0;
        while (index < tokens.size()) {
            int objectNameEnd = findObjectNameEnd(tokens, index);
            if (objectNameEnd - index > 1 && objectNameEnd < tokens.size() && "(".equals(tokens.get(objectNameEnd).text())) {
                addObjectName(tokens, index, objectNameEnd, result);
                index = objectNameEnd + 1;
            } else {
                index++;
            }
        }
    }
    
    private int findObjectNameEnd(final List<SQLStatementToken> tokens, final int startIndex) {
        if (startIndex >= tokens.size() || !isObjectNameToken(tokens.get(startIndex))) {
            return startIndex;
        }
        int result = startIndex + 1;
        while (result + 1 < tokens.size() && ".".equals(tokens.get(result).text()) && isObjectNameToken(tokens.get(result + 1))) {
            result += 2;
        }
        return result;
    }
    
    private boolean isObjectNameToken(final SQLStatementToken token) {
        return token.identifier() || "*".equals(token.text());
    }
    
    private void addObjectName(final List<SQLStatementToken> tokens, final int startIndex, final int stopIndex, final Collection<SQLStatementObjectName> result) {
        if (startIndex >= stopIndex) {
            return;
        }
        List<IdentifierValue> identifiers = new LinkedList<>();
        for (int index = startIndex; index < stopIndex; index += 2) {
            identifiers.add(new IdentifierValue(tokens.get(index).text()));
        }
        result.add(SQLStatementObjectName.from(identifiers));
    }
    
    private void extractDirectTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof InsertStatement) {
            ((InsertStatement) sqlStatement).getTable().ifPresent(optional -> addTable(optional, result));
        } else if (sqlStatement instanceof UpdateStatement) {
            addTableSegment(((UpdateStatement) sqlStatement).getTable(), result);
        } else if (sqlStatement instanceof DeleteStatement) {
            addTableSegment(((DeleteStatement) sqlStatement).getTable(), result);
        } else if (sqlStatement instanceof MergeStatement) {
            addTableSegment(((MergeStatement) sqlStatement).getTarget(), result);
        } else if (sqlStatement instanceof CreateTableStatement) {
            CreateTableStatement createTableStatement = (CreateTableStatement) sqlStatement;
            addTable(createTableStatement.getTable(), result);
            createTableStatement.getLikeTable().ifPresent(table -> addTable(table, result));
            createTableStatement.getSelectStatement().ifPresent(select -> addSelectTables(select, result));
        } else if (sqlStatement instanceof AlterTableStatement) {
            addTable(((AlterTableStatement) sqlStatement).getTable(), result);
        } else if (sqlStatement instanceof CreateViewStatement) {
            addTable(((CreateViewStatement) sqlStatement).getView(), result);
        } else if (sqlStatement instanceof AlterViewStatement) {
            AlterViewStatement alterViewStatement = (AlterViewStatement) sqlStatement;
            addTable(alterViewStatement.getView(), result);
            alterViewStatement.getRenameView().ifPresent(view -> addTable(view, result));
            alterViewStatement.getSelect().ifPresent(select -> addSelectTables(select, result));
        }
        extractIndexTargets(sqlStatement, result);
        extractDatabaseTargets(sqlStatement, result);
        extractSchemaTargets(sqlStatement, result);
        extractSequenceTargets(sqlStatement, result);
    }
    
    private void extractIndexTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateIndexStatement) {
            addIndex(((CreateIndexStatement) sqlStatement).getIndex(), result);
        } else if (sqlStatement instanceof AlterIndexStatement) {
            ((AlterIndexStatement) sqlStatement).getIndex().ifPresent(index -> addIndex(index, result));
            ((AlterIndexStatement) sqlStatement).getRenameIndex().ifPresent(index -> addIndex(index, result));
        } else if (sqlStatement instanceof DropIndexStatement) {
            for (IndexSegment each : ((DropIndexStatement) sqlStatement).getIndexes()) {
                addIndex(each, result);
            }
        }
    }
    
    private void extractDatabaseTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateDatabaseStatement) {
            addName(((CreateDatabaseStatement) sqlStatement).getDatabaseName(), result);
        } else if (sqlStatement instanceof AlterDatabaseStatement) {
            addName(((AlterDatabaseStatement) sqlStatement).getDatabaseName(), result);
            ((AlterDatabaseStatement) sqlStatement).getRenameDatabaseName().ifPresent(name -> addName(name, result));
        } else if (sqlStatement instanceof DropDatabaseStatement) {
            addName(((DropDatabaseStatement) sqlStatement).getDatabaseName(), result);
        }
    }
    
    private void extractSchemaTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateSchemaStatement) {
            ((CreateSchemaStatement) sqlStatement).getSchemaName().ifPresent(schema -> addIdentifier(schema, result));
        } else if (sqlStatement instanceof AlterSchemaStatement) {
            addIdentifier(((AlterSchemaStatement) sqlStatement).getSchemaName(), result);
            ((AlterSchemaStatement) sqlStatement).getRenameSchema().ifPresent(schema -> addIdentifier(schema, result));
        } else if (sqlStatement instanceof DropSchemaStatement) {
            for (IdentifierValue each : ((DropSchemaStatement) sqlStatement).getSchemaNames()) {
                addIdentifier(each, result);
            }
        }
    }
    
    private void extractSequenceTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateSequenceStatement) {
            addName(((CreateSequenceStatement) sqlStatement).getSequenceName(), result);
        } else if (sqlStatement instanceof AlterSequenceStatement) {
            addName(((AlterSequenceStatement) sqlStatement).getSequenceName(), result);
        } else if (sqlStatement instanceof DropSequenceStatement) {
            for (String each : ((DropSequenceStatement) sqlStatement).getSequenceNames()) {
                addName(each, result);
            }
        }
    }
    
    private void extractMergeTables(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof MergeStatement) {
            addTableSegment(((MergeStatement) sqlStatement).getSource(), result);
        }
    }
    
    private void extractCommonTableExpressionTables(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        findWithSegment(sqlStatement).ifPresent(with -> {
            for (CommonTableExpressionSegment each : with.getCommonTableExpressions()) {
                TableExtractor extractor = new TableExtractor();
                extractor.extractTablesFromSelect(each.getSubquery().getSelect());
                addTableSegments(extractor.getTableContext(), result);
            }
        });
    }
    
    private void addSelectTables(final SelectStatement selectStatement, final Collection<SQLStatementObjectName> result) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(selectStatement);
        addTableSegments(extractor.getTableContext(), result);
    }
    
    private void removeCommonTableExpressionAliases(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> objectNames) {
        Set<String> aliases = new HashSet<>();
        collectCommonTableExpressionAliases(sqlStatement, aliases);
        objectNames.removeIf(each -> !each.isQualified() && aliases.contains(each.getObjectName().toUpperCase(Locale.ENGLISH)));
    }
    
    private void collectCommonTableExpressionAliases(final SQLStatement sqlStatement, final Collection<String> result) {
        findWithSegment(sqlStatement).ifPresent(with -> {
            for (CommonTableExpressionSegment each : with.getCommonTableExpressions()) {
                each.getAliasName().ifPresent(alias -> result.add(alias.toUpperCase(Locale.ENGLISH)));
                collectCommonTableExpressionAliases(each.getSubquery().getSelect(), result);
            }
        });
    }
    
    private Optional<WithSegment> findWithSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return ((UpdateStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof DeleteStatement) {
            return ((DeleteStatement) sqlStatement).getWith();
        }
        return sqlStatement instanceof MergeStatement ? ((MergeStatement) sqlStatement).getWith() : Optional.empty();
    }
    
    private void addTableSegment(final TableSegment tableSegment, final Collection<SQLStatementObjectName> result) {
        if (tableSegment instanceof SimpleTableSegment) {
            addTable((SimpleTableSegment) tableSegment, result);
        } else if (tableSegment instanceof JoinTableSegment) {
            addTableSegment(((JoinTableSegment) tableSegment).getLeft(), result);
            addTableSegment(((JoinTableSegment) tableSegment).getRight(), result);
        } else if (tableSegment instanceof SubqueryTableSegment) {
            TableExtractor extractor = new TableExtractor();
            extractor.extractTablesFromSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
            addTableSegments(extractor.getTableContext(), result);
        } else if (tableSegment instanceof DeleteMultiTableSegment) {
            addTables(((DeleteMultiTableSegment) tableSegment).getActualDeleteTables(), result);
            addTableSegment(((DeleteMultiTableSegment) tableSegment).getRelationTable(), result);
        }
    }
    
    private void addTableSegments(final Collection<TableSegment> tableSegments, final Collection<SQLStatementObjectName> result) {
        for (TableSegment each : tableSegments) {
            addTableSegment(each, result);
        }
    }
    
    private void addTables(final Collection<SimpleTableSegment> tables, final Collection<SQLStatementObjectName> result) {
        for (SimpleTableSegment each : tables) {
            addTable(each, result);
        }
    }
    
    private void addTable(final SimpleTableSegment table, final Collection<SQLStatementObjectName> result) {
        if (null != table) {
            result.add(SQLStatementObjectName.from(table.getOwner(), table.getTableName().getIdentifier()));
        }
    }
    
    private void addIndex(final IndexSegment index, final Collection<SQLStatementObjectName> result) {
        result.add(SQLStatementObjectName.from(index.getOwner(), index.getIndexName().getIdentifier()));
    }
    
    private void addName(final String name, final Collection<SQLStatementObjectName> result) {
        if (null != name && !name.isEmpty()) {
            result.add(SQLStatementObjectName.fromNormalizedName(name));
        }
    }
    
    private void addIdentifier(final IdentifierValue identifier, final Collection<SQLStatementObjectName> result) {
        if (null != identifier) {
            result.add(SQLStatementObjectName.from(Optional.empty(), identifier));
        }
    }
}
