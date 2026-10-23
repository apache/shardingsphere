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

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.PrivilegeObjectSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
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
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class SQLStatementObjectExtractor {
    
    private static final Set<String> NAMESPACE_PRIVILEGE_OBJECT_TYPES = Set.of("DATABASE", "SCHEMA");
    
    Collection<SQLStatementObjectName> extract(final SQLStatement sqlStatement) {
        Collection<SQLStatementObjectName> result = new LinkedHashSet<>();
        extractDirectTargets(sqlStatement, result);
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSQLStatement(sqlStatement);
        addTableSegments(tableExtractor.getTableContext(), result);
        sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).ifPresent(attribute -> addTables(attribute.getTables(), result));
        extractMergeTables(sqlStatement, result);
        extractCommonTableExpressionTables(sqlStatement, result);
        removeCommonTableExpressionAliases(sqlStatement, result);
        extractDCLTargets(sqlStatement, result);
        extractQualifiedFunctions(sqlStatement, result);
        return result;
    }
    
    private void extractDCLTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        Collection<PrivilegeObjectSegment> privilegeObjects;
        if (sqlStatement instanceof GrantStatement grant) {
            privilegeObjects = grant.getPrivilegeObjects();
        } else if (sqlStatement instanceof RevokeStatement revoke) {
            privilegeObjects = revoke.getPrivilegeObjects();
        } else {
            return;
        }
        for (PrivilegeObjectSegment each : privilegeObjects) {
            if (!each.getIdentifiers().isEmpty()) {
                result.add(NAMESPACE_PRIVILEGE_OBJECT_TYPES.contains(each.getObjectType())
                        ? SQLStatementObjectName.fromNamespace(each.getIdentifiers())
                        : SQLStatementObjectName.from(each.getIdentifiers()));
            }
        }
    }
    
    private void extractQualifiedFunctions(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        new SQLStatementTreeWalker(each -> {
        }, expression -> {
            if (expression instanceof FunctionSegment function) {
                if (null != function.getOwner()) {
                    result.add(SQLStatementObjectName.from(Optional.of(function.getOwner()), new IdentifierValue(function.getFunctionName())));
                }
            }
        }).walk(sqlStatement);
    }
    
    private void extractDirectTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof InsertStatement insert) {
            insert.getTable().ifPresent(optional -> addTable(optional, result));
        } else if (sqlStatement instanceof UpdateStatement update) {
            addTableSegment(update.getTable(), result);
        } else if (sqlStatement instanceof DeleteStatement delete) {
            addTableSegment(delete.getTable(), result);
        } else if (sqlStatement instanceof MergeStatement merge) {
            addTableSegment(merge.getTarget(), result);
        } else if (sqlStatement instanceof CreateTableStatement createTableStatement) {
            addTable(createTableStatement.getTable(), result);
            createTableStatement.getLikeTable().ifPresent(table -> addTable(table, result));
            createTableStatement.getSelectStatement().ifPresent(select -> addSelectTables(select, result));
        } else if (sqlStatement instanceof AlterTableStatement alterTableStatement) {
            addTable(alterTableStatement.getTable(), result);
        } else if (sqlStatement instanceof CreateViewStatement createViewStatement) {
            addTable(createViewStatement.getView(), result);
        } else if (sqlStatement instanceof AlterViewStatement alterViewStatement) {
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
        if (sqlStatement instanceof CreateIndexStatement createIndexStatement) {
            addIndex(createIndexStatement.getIndex(), result);
        } else if (sqlStatement instanceof AlterIndexStatement alterIndexStatement) {
            alterIndexStatement.getIndex().ifPresent(index -> addIndex(index, result));
            alterIndexStatement.getRenameIndex().ifPresent(index -> addIndex(index, result));
        } else if (sqlStatement instanceof DropIndexStatement dropIndexStatement) {
            for (IndexSegment each : dropIndexStatement.getIndexes()) {
                addIndex(each, result);
            }
        }
    }
    
    private void extractDatabaseTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateDatabaseStatement createDatabaseStatement) {
            addNamespaceName(createDatabaseStatement.getDatabaseName(), result);
        } else if (sqlStatement instanceof AlterDatabaseStatement alterDatabaseStatement) {
            addNamespaceName(alterDatabaseStatement.getDatabaseName(), result);
            alterDatabaseStatement.getRenameDatabaseName().ifPresent(name -> addNamespaceName(name, result));
        } else if (sqlStatement instanceof DropDatabaseStatement dropDatabaseStatement) {
            addNamespaceName(dropDatabaseStatement.getDatabaseName(), result);
        }
    }
    
    private void extractSchemaTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateSchemaStatement createSchemaStatement) {
            createSchemaStatement.getSchemaName().ifPresent(schema -> addNamespaceIdentifier(schema, result));
        } else if (sqlStatement instanceof AlterSchemaStatement alterSchemaStatement) {
            addNamespaceIdentifier(alterSchemaStatement.getSchemaName(), result);
            alterSchemaStatement.getRenameSchema().ifPresent(schema -> addNamespaceIdentifier(schema, result));
        } else if (sqlStatement instanceof DropSchemaStatement dropSchemaStatement) {
            for (IdentifierValue each : dropSchemaStatement.getSchemaNames()) {
                addNamespaceIdentifier(each, result);
            }
        }
    }
    
    private void extractSequenceTargets(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof CreateSequenceStatement createSequenceStatement) {
            addName(createSequenceStatement.getSequenceName(), result);
        } else if (sqlStatement instanceof AlterSequenceStatement alterSequenceStatement) {
            addName(alterSequenceStatement.getSequenceName(), result);
        } else if (sqlStatement instanceof DropSequenceStatement dropSequenceStatement) {
            for (Object each : dropSequenceStatement.getSequenceNames()) {
                if (each instanceof String sequenceName) {
                    addName(sequenceName, result);
                } else if (each instanceof SimpleTableSegment simpleTable) {
                    addTable(simpleTable, result);
                } else {
                    throw new MCPUnsupportedSQLStatementException();
                }
            }
        }
    }
    
    private void extractMergeTables(final SQLStatement sqlStatement, final Collection<SQLStatementObjectName> result) {
        if (sqlStatement instanceof MergeStatement merge) {
            addTableSegment(merge.getSource(), result);
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
        if (sqlStatement instanceof CreateTableStatement createTableStatement) {
            createTableStatement.getSelectStatement().ifPresent(each -> collectCommonTableExpressionAliases(each, result));
        } else if (sqlStatement instanceof CreateViewStatement createViewStatement) {
            collectCommonTableExpressionAliases(createViewStatement.getSelect(), result);
        } else if (sqlStatement instanceof AlterViewStatement alterViewStatement) {
            alterViewStatement.getSelect().ifPresent(each -> collectCommonTableExpressionAliases(each, result));
        }
    }
    
    private Optional<WithSegment> findWithSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement select) {
            return select.getWith();
        }
        if (sqlStatement instanceof InsertStatement insert) {
            return insert.getWith();
        }
        if (sqlStatement instanceof UpdateStatement update) {
            return update.getWith();
        }
        if (sqlStatement instanceof DeleteStatement delete) {
            return delete.getWith();
        }
        return sqlStatement instanceof MergeStatement merge ? merge.getWith() : Optional.empty();
    }
    
    private void addTableSegment(final TableSegment tableSegment, final Collection<SQLStatementObjectName> result) {
        if (tableSegment instanceof SimpleTableSegment simpleTable) {
            addTable(simpleTable, result);
        } else if (tableSegment instanceof JoinTableSegment joinTable) {
            addTableSegment(joinTable.getLeft(), result);
            addTableSegment(joinTable.getRight(), result);
        } else if (tableSegment instanceof SubqueryTableSegment subqueryTable) {
            TableExtractor extractor = new TableExtractor();
            extractor.extractTablesFromSelect(subqueryTable.getSubquery().getSelect());
            addTableSegments(extractor.getTableContext(), result);
        } else if (tableSegment instanceof DeleteMultiTableSegment deleteMultiTable) {
            addTables(deleteMultiTable.getActualDeleteTables(), result);
            addTableSegment(deleteMultiTable.getRelationTable(), result);
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
        if (null == table) {
            return;
        }
        result.add(SQLStatementObjectName.from(table.getOwner(), table.getTableName().getIdentifier()));
    }
    
    private void addIndex(final IndexSegment index, final Collection<SQLStatementObjectName> result) {
        result.add(SQLStatementObjectName.from(index.getOwner(), index.getIndexName().getIdentifier()));
    }
    
    private void addName(final String name, final Collection<SQLStatementObjectName> result) {
        if (null != name && !name.isEmpty()) {
            result.add(SQLStatementObjectName.fromNormalizedName(name));
        }
    }
    
    private void addNamespaceName(final String name, final Collection<SQLStatementObjectName> result) {
        if (null != name && !name.isEmpty()) {
            result.add(SQLStatementObjectName.fromNormalizedNamespaceName(name));
        }
    }
    
    private void addNamespaceIdentifier(final IdentifierValue identifier, final Collection<SQLStatementObjectName> result) {
        if (null != identifier) {
            result.add(SQLStatementObjectName.fromNamespace(identifier));
        }
    }
}
