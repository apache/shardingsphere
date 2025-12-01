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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.prepare;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdSizeRegistry;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Firebird prepare transaction command executor.
 */
@RequiredArgsConstructor
public final class FirebirdPrepareStatementCommandExecutor implements CommandExecutor {
    
    private final FirebirdPrepareStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ReturningSegment returningSegment;
    
    @Override
    public Collection<DatabasePacket> execute() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(
                metaDataContexts.getMetaData(), connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        FirebirdServerPreparedStatement serverPreparedStatement = new FirebirdServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(getStatementId(), serverPreparedStatement);
        return createResponse(sqlStatementContext, metaDataContexts);
    }
    
    private int getStatementId() {
        return packet.isValidStatementHandle() ? packet.getStatementId() : FirebirdStatementIdGenerator.getInstance().getStatementId(connectionSession.getConnectionId());
    }
    
    private Collection<DatabasePacket> createResponse(final SQLStatementContext sqlStatementContext, final MetaDataContexts metaDataContexts) {
        FirebirdSQLInfoReturnValue statementType = getFirebirdStatementType(sqlStatementContext.getSqlStatement());
        FirebirdPrepareStatementReturnPacket returnPacket = new FirebirdPrepareStatementReturnPacket();
        while (packet.nextItem()) {
            switch (packet.getCurrentItem()) {
                case STMT_TYPE:
                    returnPacket.setType(statementType);
                    break;
                case SELECT:
                    if (statementType.isSelectDescribable()) {
                        processDescribe(sqlStatementContext, metaDataContexts, returnPacket.getDescribeSelect(), true);
                    } else {
                        skipDescribe();
                    }
                    break;
                case BIND:
                    if (statementType.isBindDescribable()) {
                        processDescribe(sqlStatementContext, metaDataContexts, returnPacket.getDescribeBind(), false);
                    } else {
                        skipDescribe();
                    }
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", packet.getCurrentItem());
            }
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(returnPacket));
    }
    
    private FirebirdSQLInfoReturnValue getFirebirdStatementType(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return FirebirdSQLInfoReturnValue.SELECT;
        }
        if (statement instanceof InsertStatement) {
            if (((InsertStatement) statement).getReturning().isPresent()) {
                returningSegment = ((InsertStatement) statement).getReturning().orElse(null);
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
            }
            return FirebirdSQLInfoReturnValue.INSERT;
        }
        if (statement instanceof UpdateStatement) {
            if (((UpdateStatement) statement).getReturning().isPresent()) {
                returningSegment = ((UpdateStatement) statement).getReturning().orElse(null);
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
            }
            return FirebirdSQLInfoReturnValue.UPDATE;
        }
        if (statement instanceof DeleteStatement) {
            if (((DeleteStatement) statement).getReturning().isPresent()) {
                returningSegment = ((DeleteStatement) statement).getReturning().orElse(null);
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
            }
            return FirebirdSQLInfoReturnValue.DELETE;
        }
        if (statement instanceof DDLStatement) {
            return FirebirdSQLInfoReturnValue.DDL;
        }
        if (statement instanceof BeginTransactionStatement) {
            return FirebirdSQLInfoReturnValue.START_TRANS;
        }
        if (statement instanceof CommitStatement) {
            return FirebirdSQLInfoReturnValue.COMMIT;
        }
        if (statement instanceof RollbackStatement) {
            return FirebirdSQLInfoReturnValue.ROLLBACK;
        }
        if (statement instanceof SavepointStatement) {
            return FirebirdSQLInfoReturnValue.SAVEPOINT;
        }
        return null;
    }
    
    private void skipDescribe() {
        while (packet.getCurrentItem() != FirebirdSQLInfoPacketType.DESCRIBE_END) {
            packet.nextItem();
        }
    }
    
    private void processDescribe(final SQLStatementContext sqlStatementContext, final MetaDataContexts metaDataContexts, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                 final boolean returnAll) {
        // TODO add exception if the first item is not DESCRIBE_VARS
        packet.nextItem();
        Collection<FirebirdSQLInfoPacketType> requestedItems = new LinkedList<>();
        while (packet.nextItem()) {
            requestedItems.add(packet.getCurrentItem());
            if (packet.getCurrentItem() == FirebirdSQLInfoPacketType.DESCRIBE_END) {
                if (returnAll) {
                    processReturnValues(sqlStatementContext, metaDataContexts, describeColumns, requestedItems);
                } else {
                    processParameters(sqlStatementContext, metaDataContexts, describeColumns, requestedItems);
                }
                return;
            }
        }
    }
    
    private void processReturnValues(final SQLStatementContext sqlStatementContext, final MetaDataContexts metaDataContexts, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                     final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(databaseName);
        ShardingSphereSchema schema = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName);
        Collection<Projection> projections = getProjections(sqlStatementContext, schema);
        int columnCount = 0;
        for (Projection each : projections) {
            if (each instanceof ColumnProjection) {
                String tableName = ((ColumnProjection) each).getOriginalTable().getValue();
                ShardingSphereTable table = schema.getTable(tableName.isEmpty() ? getTableNames(sqlStatementContext).iterator().next() : tableName);
                if (table == null) {
                    table = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema("system_tables")
                            .getTable(tableName.isEmpty() ? getTableNames(sqlStatementContext).iterator().next() : tableName);
                }
                ShardingSphereColumn column = table.getColumn(((ColumnProjection) each).getOriginalColumn().getValue());
                processColumn(describeColumns, requestedItems, table, column, ((ColumnProjection) each).getOwner().orElse(null), each.getAlias().orElse(null), ++columnCount);
            } else if (each instanceof ExpressionProjection) {
                processExpressionProjection((ExpressionProjection) each, describeColumns, requestedItems, ++columnCount);
            } else if (each instanceof AggregationProjection) {
                String functionName = ((AggregationProjection) each).getType().name();
                processCustomColumn(null, functionName, each.getAlias().orElse(null), getFunctionType(functionName), describeColumns, requestedItems, ++columnCount);
            } else if (each instanceof SubqueryProjection) {
                SubqueryProjection subquery = (SubqueryProjection) each;
                processCustomColumn(null, subquery.getColumnName(), subquery.getAlias().orElse(null), Types.INTEGER, describeColumns, requestedItems, ++columnCount);
            }
        }
    }
    
    private Collection<Projection> getProjections(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            SelectStatementContext selectStatement = (SelectStatementContext) sqlStatementContext;
            Collection<Projection> expandProjections = selectStatement.getProjectionsContext().getExpandProjections();
            Map<Integer, SelectStatementContext> subquery = selectStatement.getSubqueryContexts();
            if (subquery.isEmpty()) {
                return expandProjections;
            }
            // workaround for SubqueryTableBindUtils transform Expression and Aggregation projections to Column projection
            TableSegment from = ((SelectStatement) sqlStatementContext.getSqlStatement()).getFrom().orElse(null);
            if (!(from instanceof SubqueryTableSegment)) {
                return expandProjections;
            }
            Collection<Projection> subqueryProjections = new LinkedList<>();
            for (SelectStatementContext value : subquery.values()) {
                subqueryProjections.addAll(value.getProjectionsContext().getExpandProjections());
            }
            return subqueryProjections;
        }
        if (returningSegment == null) {
            return Collections.emptyList();
        }
        ProjectionEngine projectionEngine = new ProjectionEngine(sqlStatementContext.getSqlStatement().getDatabaseType());
        Collection<Projection> result = new LinkedList<>();
        Collection<ProjectionSegment> projections = returningSegment.getProjections().getProjections();
        for (ProjectionSegment each : projections) {
            Projection projection = projectionEngine.createProjection(each).orElse(null);
            if (projection instanceof ShorthandProjection) {
                result.addAll(processShorthandProjection(sqlStatementContext, schema, (ShorthandProjection) projection));
            } else if (!(projection instanceof DerivedProjection) && null != projection) {
                result.add(projection);
            }
        }
        return result;
    }
    
    private Collection<Projection> processShorthandProjection(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final ShorthandProjection projection) {
        if (!projection.getActualColumns().isEmpty()) {
            return projection.getActualColumns();
        }
        Collection<Projection> result = new LinkedList<>();
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        if (null != tablesContext) {
            for (String tableName : tablesContext.getTableNames()) {
                ShardingSphereTable table = schema.getTable(tableName);
                table.getAllColumns().forEach(each -> {
                    ColumnSegmentBoundInfo info = new ColumnSegmentBoundInfo(null, new IdentifierValue(table.getName()), new IdentifierValue(each.getName()), TableSourceType.PHYSICAL_TABLE);
                    result.add(new ColumnProjection(null, new IdentifierValue(each.getName()), null, sqlStatementContext.getSqlStatement().getDatabaseType(), null, null, info));
                });
            }
        }
        return result;
    }
    
    private void processParameters(final SQLStatementContext sqlStatementContext, final MetaDataContexts metaDataContexts, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                   final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        if (sqlStatementContext instanceof InsertStatementContext) {
            processInsertStatement((InsertStatementContext) sqlStatementContext, metaDataContexts, describeColumns, requestedItems);
            return;
        }
        Collection<ColumnSegment> affectedColumns = findAffectedColumns(sqlStatementContext);
        int parametersCount = sqlStatementContext.getSqlStatement().getParameterMarkers().size();
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(databaseName);
        int columnCount = 0;
        for (ColumnSegment columnSegment : affectedColumns) {
            ShardingSphereTable table = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(columnSegment.getColumnBoundInfo().getOriginalTable().getValue());
            ShardingSphereColumn column = table.getColumn(columnSegment.getColumnBoundInfo().getOriginalColumn().getValue());
            processColumn(describeColumns, requestedItems, table, column, columnSegment.getOwner().map(OwnerSegment::getIdentifier).orElse(null), columnSegment.getIdentifier(), ++columnCount);
        }
        for (int i = 0; i < parametersCount - affectedColumns.size(); i++) {
            processCustomColumn(null, null, null, 12, describeColumns, requestedItems, ++columnCount);
        }
    }
    
    private void processInsertStatement(final InsertStatementContext sqlStatementContext, final MetaDataContexts metaDataContexts, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                        final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        Collection<String> tableNames = getTableNames(sqlStatementContext);
        Collection<String> affectedColumns = new LinkedList<>();
        for (InsertValueContext context : sqlStatementContext.getInsertValueContexts()) {
            affectedColumns.addAll(processInsertValueContext(sqlStatementContext, context));
        }
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(databaseName);
        int columnCount = 0;
        for (String tableName : tableNames) {
            ShardingSphereTable table = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
            for (String columnName : affectedColumns) {
                ShardingSphereColumn column = table.getColumn(columnName);
                processColumn(describeColumns, requestedItems, table, column, null, null, ++columnCount);
            }
        }
    }
    
    private Collection<String> processInsertValueContext(final InsertStatementContext context, final InsertValueContext valueContext) {
        Collection<String> result = new LinkedList<>();
        for (int i = 0; i < valueContext.getValueExpressions().size(); i++) {
            ExpressionSegment expression = valueContext.getValueExpressions().get(i);
            if (expression instanceof ParameterMarkerExpressionSegment) {
                result.add(context.getColumnNames().get(i));
            }
        }
        return result;
    }
    
    private Collection<String> getTableNames(final SQLStatementContext sqlStatementContext) {
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        return null == tablesContext ? Collections.emptyList() : tablesContext.getTableNames();
    }
    
    private Collection<ColumnSegment> findAffectedColumns(final SQLStatementContext sqlStatementContext) {
        Collection<ColumnSegment> result = new LinkedList<>();
        if (sqlStatementContext instanceof UpdateStatementContext) {
            for (ColumnAssignmentSegment segment : ((UpdateStatementContext) sqlStatementContext).getSqlStatement().getSetAssignment().getAssignments()) {
                if (segment.getValue() instanceof ParameterMarkerExpressionSegment) {
                    result.add(segment.getColumns().get(0));
                }
            }
        }
        if (sqlStatementContext instanceof WhereContextAvailable) {
            Collection<WhereSegment> whereSegments = ((WhereContextAvailable) sqlStatementContext).getWhereSegments();
            for (WhereSegment each : whereSegments) {
                processExpr(each.getExpr(), result);
            }
        }
        return result;
    }
    
    private boolean processExpr(final ExpressionSegment expr, final Collection<ColumnSegment> affectedColumns) {
        if (!(expr instanceof BinaryOperationExpression)) {
            return expr instanceof ParameterMarkerExpressionSegment;
        }
        BinaryOperationExpression binary = (BinaryOperationExpression) expr;
        processExpr(binary.getLeft(), affectedColumns);
        boolean rightIsParam = processExpr(binary.getRight(), affectedColumns);
        if (rightIsParam && binary.getLeft() instanceof ColumnSegment) {
            affectedColumns.add((ColumnSegment) binary.getLeft());
        }
        return false;
    }
    
    private void processExpressionProjection(final ExpressionProjection expr, final Collection<FirebirdReturnColumnPacket> describeColumns, final Collection<FirebirdSQLInfoPacketType> requestedItems,
                                             final int columnCount) {
        final ExpressionSegment exprSegment = expr.getExpressionSegment().getExpr();
        if (exprSegment instanceof FunctionSegment) {
            String functionName = ((FunctionSegment) exprSegment).getFunctionName();
            processCustomColumn(null, functionName, expr.getAlias().orElse(null), getFunctionType(functionName), describeColumns, requestedItems, columnCount);
        } else if (exprSegment instanceof BinaryOperationExpression) {
            String operationName = getOperationName(((BinaryOperationExpression) exprSegment).getOperator());
            int operationType = getOperationType(((BinaryOperationExpression) exprSegment).getOperator());
            processCustomColumn(null, operationName, expr.getAlias().orElse(null), operationType, describeColumns, requestedItems, columnCount);
        } else if (exprSegment instanceof LiteralExpressionSegment) {
            Object value = ((LiteralExpressionSegment) exprSegment).getLiterals();
            int type = Types.NULL;
            if (value instanceof String) {
                type = Types.VARCHAR;
            } else if (value instanceof Integer) {
                type = Types.INTEGER;
            } else if (value instanceof Long) {
                type = Types.BIGINT;
            } else if (value instanceof Number) {
                type = Types.NUMERIC;
            }
            processCustomColumn(null, null, expr.getAlias().orElse(null), type, describeColumns, requestedItems, columnCount);
        }
    }
    
    private int getFunctionType(final String functionName) {
        // TODO add proper coalesce and other conditional functions return types
        switch (functionName) {
            case "substring":
            case "current_role":
            case "current_user":
            case "coalesce":
                return Types.VARCHAR;
            case "gen_id":
                return Types.BIGINT;
            case "current_timestamp":
                return Types.TIMESTAMP;
            default:
                return Types.INTEGER;
        }
    }
    
    private String getOperationName(final String operationType) {
        switch (operationType) {
            case "||":
                return "CONCATENATION";
            default:
                return operationType;
        }
    }
    
    private int getOperationType(final String operationType) {
        switch (operationType) {
            case "||":
                return 12;
            default:
                return 4;
        }
    }
    
    private void processCustomColumn(final String tableName, final String columnName, final IdentifierValue columnAlias, final int dataType,
                                     final Collection<FirebirdReturnColumnPacket> describeColumns, final Collection<FirebirdSQLInfoPacketType> requestedItems, final int columnCount) {
        ShardingSphereTable table = new ShardingSphereTable(tableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereColumn column = new ShardingSphereColumn(columnName, dataType, false, false, true, true, false, false);
        processColumn(describeColumns, requestedItems, table, column, null, columnAlias, columnCount);
    }
    
    private void processColumn(final Collection<FirebirdReturnColumnPacket> describeColumns, final Collection<FirebirdSQLInfoPacketType> requestedItems, final ShardingSphereTable table,
                               final ShardingSphereColumn column, final IdentifierValue tableAlias, final IdentifierValue columnAlias, final int idx) {
        String tableAliasString = null == tableAlias ? table.getName() : tableAlias.getValue();
        String columnAliasString = null == columnAlias ? column.getName() : columnAlias.getValue();
        String owner = connectionSession.getConnectionContext().getGrantee().getUsername();
        Integer columnLength = resolveColumnLength(table, column);
        describeColumns.add(new FirebirdReturnColumnPacket(requestedItems, idx, table, column, tableAliasString, columnAliasString, owner, columnLength));
    }
    
    private Integer resolveColumnLength(final ShardingSphereTable table, final ShardingSphereColumn column) {
        if (null == table || null == column) {
            return null;
        }
        OptionalInt columnSize = FirebirdSizeRegistry.findColumnSize(connectionSession.getCurrentDatabaseName(), table.getName(), column.getName());
        return columnSize.isPresent() ? columnSize.getAsInt() : null;
    }
}
