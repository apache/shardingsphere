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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.StartTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Firebird prepare transaction command executor
 */
@RequiredArgsConstructor
public final class FirebirdPrepareStatementCommandExecutor implements CommandExecutor {

    private final FirebirdPrepareStatementPacket packet;
    private final ConnectionSession connectionSession;

    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(),
                connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement, Collections.emptyList());
        int statementId = getStatementId();
        FirebirdServerPreparedStatement serverPreparedStatement = new FirebirdServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        return createResponse(sqlStatementContext, metaDataContexts);
    }

    private int getStatementId() {
        if (packet.isValidStatementHandle()) {
            return packet.getStatementId();
        }
        int transactionId = FirebirdTransactionIdGenerator.getInstance().getTransactionId(connectionSession.getConnectionId());
        return FirebirdStatementIdGenerator.getInstance().getStatementId(transactionId);
    }
    
    private Collection<DatabasePacket> createResponse(final SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts) {
        ByteBuf data = packet.getPayload().getByteBuf().alloc().buffer();
        int statementType = getFirebirdStatementType(sqlStatementContext.getSqlStatement());
        while (packet.nextItem()) {
            switch (packet.getCurrentItem()) {
                case STMT_TYPE:
                    writeInt(FirebirdSQLInfoPacketType.STMT_TYPE, statementType, data);
                    break;
                case SELECT:
                    writeCode(FirebirdSQLInfoPacketType.SELECT, data);
                    if (FirebirdSQLInfoReturnValue.isSelectDescribable(statementType)) {
                        processDescribe(sqlStatementContext, metaDataContexts, data, true);
                    } else {
                        skipDescribe(data);
                    }
                    break;
                case BIND:
                    writeCode(FirebirdSQLInfoPacketType.BIND, data);
                    if (FirebirdSQLInfoReturnValue.isBindDescribable(statementType)) {
                        processDescribe(sqlStatementContext, metaDataContexts, data, false);
                    } else {
                        skipDescribe(data);
                    }
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", packet.getCurrentItem());
            }
        }
        writeCode(FirebirdCommonInfoPacketType.END, data);
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(data));
    }
    
    private void writeCode(FirebirdInfoPacketType code, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
    }
    
    private void writeInt(FirebirdInfoPacketType code, int value, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
        buffer.writeShortLE(4);
        buffer.writeIntLE(value);
    }
    
    private void writeString(FirebirdInfoPacketType code, String value, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
        byte[] valueBytes = value != null ? value.getBytes(packet.getPayload().getCharset()) : new byte[0];
        buffer.writeShortLE(valueBytes.length);
        buffer.writeBytes(valueBytes);
    }

    private int getFirebirdStatementType(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return FirebirdSQLInfoReturnValue.SELECT.getCode();
        }
        if (statement instanceof InsertStatement) {
            if (((InsertStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.INSERT.getCode();
        }
        if (statement instanceof UpdateStatement) {
            if (((UpdateStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.UPDATE.getCode();
        }
        if (statement instanceof DeleteStatement) {
            if (((DeleteStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.DELETE.getCode();
        }
        if (statement instanceof DDLStatement) {
            return FirebirdSQLInfoReturnValue.DDL.getCode();
        }
        if (statement instanceof StartTransactionStatement) {
            return FirebirdSQLInfoReturnValue.START_TRANS.getCode();
        }
        if (statement instanceof CommitStatement) {
            return FirebirdSQLInfoReturnValue.COMMIT.getCode();
        }
        if (statement instanceof RollbackStatement) {
            return FirebirdSQLInfoReturnValue.ROLLBACK.getCode();
        }
        if (statement instanceof SavepointStatement) {
            return FirebirdSQLInfoReturnValue.SAVEPOINT.getCode();
        }
        return 0;
    }
    
    private void skipDescribe(ByteBuf buffer) {
        while (packet.getCurrentItem() != FirebirdSQLInfoPacketType.DESCRIBE_END) {
            packet.nextItem();
        }
        writeInt(FirebirdSQLInfoPacketType.DESCRIBE_VARS, 0, buffer);
    }

    private void processDescribe(SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts, ByteBuf buffer, boolean returnAll) {
        //TODO add exception if the first item is not DESCRIBE_VARS
        packet.nextItem();
        List<FirebirdSQLInfoPacketType> requestedItems = new ArrayList<>(11);
        while (packet.nextItem()) {
            requestedItems.add(packet.getCurrentItem());
            if (packet.getCurrentItem() == FirebirdSQLInfoPacketType.DESCRIBE_END) {
                ByteBuf describeBuffer = packet.getPayload().getByteBuf().alloc().buffer();
                int count = returnAll ?
                        processReturnValues(sqlStatementContext, metaDataContexts, describeBuffer, requestedItems) :
                        processParameters(sqlStatementContext, metaDataContexts, describeBuffer, requestedItems);
                writeInt(FirebirdSQLInfoPacketType.DESCRIBE_VARS, count, buffer);
                buffer.writeBytes(describeBuffer, describeBuffer.readableBytes());
                return;
            }
        }
    }
    
    private int processReturnValues(SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts, ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems) {
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(databaseName);
        ShardingSphereSchema schema = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName);
        List<Projection> projections = getProjections(sqlStatementContext, schema);
        int columnCount = 0;
        for (Projection each : projections) {
            if (each instanceof ColumnProjection) {
                ShardingSphereTable table = schema.getTable(((ColumnProjection) each).getOriginalTable().getValue());
                ShardingSphereColumn column = table.getColumn(((ColumnProjection) each).getOriginalColumn().getValue());
                processColumn(buffer, requestedItems, table, column, (ColumnProjection) each, ++columnCount);
            }
            if (each instanceof ExpressionProjection) {
                processExpressionProjection(((ExpressionProjection) each).getExpressionSegment().getExpr(), each, buffer, requestedItems, ++columnCount);
            }
            if (each instanceof AggregationProjection) {
                String functionName = ((AggregationProjection) each).getType().name();
                processCustomColumn(null, functionName , getFunctionType(functionName), each, buffer, requestedItems, ++columnCount);
            }
        }
        return columnCount;
    }
    
    private int processParameters(SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts, ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems) {
        List<String> tableNames = new ArrayList<>();
        if (sqlStatementContext instanceof TableAvailable) {
            tableNames.addAll(((TableAvailable) sqlStatementContext).getTablesContext().getTableNames());
        }
        List<String> affectedColumns = new ArrayList<>();
        if (sqlStatementContext instanceof InsertStatementContext) {
            for (InsertValueContext context : ((InsertStatementContext) sqlStatementContext).getInsertValueContexts()) {
                affectedColumns.addAll(processInsertValueContext((InsertStatementContext) sqlStatementContext, context));
            }
        }
        affectedColumns.addAll(findWhereParametersColumns(sqlStatementContext));
        int parametersCount = ((AbstractSQLStatement) sqlStatementContext.getSqlStatement()).getParameterMarkerSegments().size();
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(databaseName);
        int columnCount = 0;
        for (String tableName : tableNames) {
            ShardingSphereTable table = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
            for (String columnName : affectedColumns) {
                ShardingSphereColumn column = table.getColumn(columnName);
                processColumn(buffer, requestedItems, table, column, null, ++columnCount);
            }
        }
        for (int i = 0; i < parametersCount - affectedColumns.size(); i++) {
            processCustomColumn(null, null, 12, null, buffer, requestedItems, ++columnCount);
        }
        return columnCount;
    }
    
    private List<String> processInsertValueContext(InsertStatementContext context, InsertValueContext valueContext) {
        List<String> affectedColumns = new ArrayList<>();
        for (int i = 0; i < valueContext.getValueExpressions().size(); i++) {
            ExpressionSegment expression = valueContext.getValueExpressions().get(i);
            if (expression instanceof ParameterMarkerExpressionSegment) {
                affectedColumns.add(context.getColumnNames().get(i));
            }
        }
        return affectedColumns;
    }
    
    @SuppressWarnings("unchecked")
    private List<Projection> getProjections(SQLStatementContext sqlStatementContext, ShardingSphereSchema schema) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections();
        }
        ProjectionEngine projectionEngine = new ProjectionEngine(sqlStatementContext.getDatabaseType());
        Optional<ReturningSegment> returningSegment = (Optional<ReturningSegment>) ReflectionUtils.getFieldValueByGetMethod(sqlStatementContext.getSqlStatement(), "returningSegment").orElse(Optional.empty());
        List<Projection> result = new ArrayList<>();
        Collection<ProjectionSegment> projections = returningSegment.map(returning -> returning.getProjections().getProjections()).orElse(Collections.emptyList());
        for (ProjectionSegment each : projections) {
            Projection projection = projectionEngine.createProjection(each).orElse(null);
            if (projection instanceof ShorthandProjection) {
                result.addAll(processShorthandProjection(sqlStatementContext, schema, (ShorthandProjection) projection));
            } else if (!(projection instanceof DerivedProjection)) {
                result.add(projection);
            }
        }
        return result;
    }
    
    private Collection<Projection> processShorthandProjection(SQLStatementContext sqlStatementContext, ShardingSphereSchema schema, ShorthandProjection projection) {
        if (!projection.getActualColumns().isEmpty()) {
            return projection.getActualColumns();
        }
        Collection<Projection> result = new ArrayList<>();
        if (sqlStatementContext instanceof TableAvailable) {
            TablesContext tablesContext = ((TableAvailable) sqlStatementContext).getTablesContext();
            for (String tableName : tablesContext.getTableNames()) {
                ShardingSphereTable table = schema.getTable(tableName);
                table.getAllColumns().forEach(each -> {
                    ColumnSegmentBoundInfo info = new ColumnSegmentBoundInfo(null, new IdentifierValue(table.getName()), new IdentifierValue(each.getName()));
                    result.add(new ColumnProjection(null, new IdentifierValue(each.getName()), null, sqlStatementContext.getDatabaseType(), null, null, info));
                });
            }
        }
        return result;
    }

    private List<String> findWhereParametersColumns(SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        List<String> affectedColumns = new ArrayList<>();
        Collection<WhereSegment> whereSegments = ((WhereAvailable) sqlStatementContext).getWhereSegments();
        for (WhereSegment each : whereSegments) {
            processExpr(each.getExpr(), affectedColumns);
        }
        return affectedColumns;
    }
    
    private boolean processExpr(ExpressionSegment expr, List<String> affectedColumns) {
        if (expr instanceof BinaryOperationExpression) {
            processExpr(((BinaryOperationExpression) expr).getLeft(), affectedColumns);
            boolean isParameter = processExpr(((BinaryOperationExpression) expr).getRight(), affectedColumns);
            if (isParameter) {
                ExpressionSegment left = ((BinaryOperationExpression) expr).getLeft();
                //TODO consider behaviour of other segment types
                if (left instanceof ColumnSegment) {
                    affectedColumns.add(((ColumnSegment) left).getIdentifier().getValue());
                }
            }
        }
        return expr instanceof ParameterMarkerExpressionSegment;
    }
    
    private void processExpressionProjection(ExpressionSegment expr, Projection projection, ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems, int columnCount) {
        if (expr instanceof FunctionSegment) {
            String functionName = ((FunctionSegment) expr).getFunctionName();
            processCustomColumn(null, functionName, getFunctionType(functionName), projection, buffer, requestedItems, columnCount);
        }
        if (expr instanceof BinaryOperationExpression) {
            String operationName = getOperationName(((BinaryOperationExpression) expr).getOperator());
            int operationType = getOperationType(((BinaryOperationExpression) expr).getOperator());
            processCustomColumn(null, operationName, operationType, projection, buffer, requestedItems, columnCount);
        }
    }
    
    private int getFunctionType(String functionName) {
        switch (functionName) {
            case "substring":
            case "current_role":
            case "current_user":
                return 12;
            default:
                return 4;
        }
    }
    
    private String getOperationName(String operationType) {
        switch (operationType) {
            case "||":
                return "CONCATENATION";
            default:
                return operationType;
        }
    }
    
    private int getOperationType(String operationType) {
        switch (operationType) {
            case "||":
                return 12;
            default:
                return 4;
        }
    }
    
    private void processCustomColumn(String tableName, String columnName, int dataType, Projection projection, ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems, int columnCount) {
        ShardingSphereTable table = new ShardingSphereTable(tableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereColumn column = new ShardingSphereColumn(columnName, dataType, false, false, true, true, false, false);
        processColumn(buffer, requestedItems, table, column, projection, columnCount);
    }
    
    private void processColumn(ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems, ShardingSphereTable table, ShardingSphereColumn column, Projection projection, int idx) {
        //SQLDA_SEQ uses 1-based index
        for (FirebirdSQLInfoPacketType requestedItem : requestedItems) {
            switch (requestedItem) {
                case SQLDA_SEQ:
                    writeInt(FirebirdSQLInfoPacketType.SQLDA_SEQ, idx, buffer);
                    break;
                case TYPE:
                    writeInt(FirebirdSQLInfoPacketType.TYPE, FirebirdBinaryColumnType.valueOfJDBCType(column.getDataType()).getValue() + 1, buffer);
                    break;
                case SUB_TYPE:
                    writeInt(FirebirdSQLInfoPacketType.SUB_TYPE, 0, buffer);
                    break;
                case SCALE:
                    writeInt(FirebirdSQLInfoPacketType.SCALE, 0, buffer);
                    break;
                case LENGTH:
                    writeInt(FirebirdSQLInfoPacketType.LENGTH, FirebirdBinaryColumnType.valueOfJDBCType(column.getDataType()).getLength(), buffer);
                    break;
                case FIELD:
                    writeString(FirebirdSQLInfoPacketType.FIELD, column.getName(), buffer);
                    break;
                case ALIAS:
                    Optional<IdentifierValue> alias = projection != null ? projection.getAlias() : Optional.empty();
                    writeString(FirebirdSQLInfoPacketType.ALIAS, alias.map(IdentifierValue::getValue).orElse(column.getName()), buffer);
                    break;
                case RELATION:
                    writeString(FirebirdSQLInfoPacketType.RELATION, table.getName(), buffer);
                    break;
                case RELATION_ALIAS:
                    Optional<IdentifierValue> owner = projection != null ? projection.getAlias() : Optional.empty();
                    writeString(FirebirdSQLInfoPacketType.RELATION_ALIAS, owner.map(IdentifierValue::getValue).orElse(""), buffer);
                    break;
                case OWNER:
                    writeString(FirebirdSQLInfoPacketType.OWNER, connectionSession.getConnectionContext().getGrantee().getUsername(), buffer);
                    break;
                case DESCRIBE_END:
                    writeCode(FirebirdSQLInfoPacketType.DESCRIBE_END, buffer);
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", requestedItem);
            }
        }
    }
}
