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

import com.google.common.base.Suppliers;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdBlobInfoRegistry;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdNonFixedLengthColumnSizeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * Firebird prepare transaction command executor.
 */
@RequiredArgsConstructor
public final class FirebirdPrepareStatementCommandExecutor implements CommandExecutor {
    
    private static final ShardingSphereTable LITERAL_TABLE = new ShardingSphereTable(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    
    private static final Collection<String> PASS_THROUGH_FUNCTIONS = new HashSet<>(Arrays.asList("coalesce", "nullif", "maxvalue", "minvalue", "mod"));
    
    private static final Map<String, int[]> FUNCTION_ARG_TYPES = new HashMap<>(48, 1F);
    
    private static final Map<String, Integer> FUNCTION_RETURN_TYPES = new HashMap<>(32, 1F);
    
    static {
        int[] varcharArg = {Types.VARCHAR};
        for (String each : Arrays.asList("ascii_val", "char_length", "character_length", "octet_length", "bit_length", "upper", "lower", "trim", "ltrim", "rtrim", "reverse",
                "base64_encode", "base64_decode", "hex_encode", "hex_decode", "char_to_uuid", "rdb$role_in_use", "hash", "crypt_hash")) {
            FUNCTION_ARG_TYPES.put(each, varcharArg);
        }
        FUNCTION_ARG_TYPES.put("left", new int[]{Types.VARCHAR, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("right", new int[]{Types.VARCHAR, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("lpad", new int[]{Types.VARCHAR, Types.INTEGER, Types.VARCHAR});
        FUNCTION_ARG_TYPES.put("rpad", new int[]{Types.VARCHAR, Types.INTEGER, Types.VARCHAR});
        FUNCTION_ARG_TYPES.put("substring", new int[]{Types.VARCHAR, Types.INTEGER, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("overlay", new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("replace", varcharArg);
        FUNCTION_ARG_TYPES.put("position", new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("blob_append", new int[]{Types.BLOB, Types.VARCHAR});
        int[] integerArg = {Types.INTEGER};
        for (String each : Arrays.asList("ascii_char", "bin_shl", "bin_shr", "bin_shl_rot", "bin_shr_rot", "rsa_private")) {
            FUNCTION_ARG_TYPES.put(each, integerArg);
        }
        FUNCTION_ARG_TYPES.put("gen_id", new int[]{Types.VARCHAR, Types.BIGINT});
        int[] bigintArg = {Types.BIGINT};
        for (String each : Arrays.asList("rdb$get_transaction_cn", "bin_and", "bin_or", "bin_xor", "bin_not")) {
            FUNCTION_ARG_TYPES.put(each, bigintArg);
        }
        FUNCTION_ARG_TYPES.put("round", new int[]{Types.DOUBLE, Types.INTEGER});
        FUNCTION_ARG_TYPES.put("trunc", new int[]{Types.DOUBLE, Types.INTEGER});
        int[] doubleArg = {Types.DOUBLE};
        for (String each : Arrays.asList("abs", "ceil", "ceiling", "floor", "exp", "ln", "log", "log10", "power", "acos", "acosh", "asin", "asinh", "atan", "atanh", "atan2",
                "cos", "cosh", "cot", "sin", "sinh", "tan", "tanh", "sqrt")) {
            FUNCTION_ARG_TYPES.put(each, doubleArg);
        }
        FUNCTION_ARG_TYPES.put("first_day", new int[]{Types.INTEGER, Types.TIMESTAMP});
        FUNCTION_ARG_TYPES.put("last_day", new int[]{Types.INTEGER, Types.TIMESTAMP});
        for (String each : Arrays.asList("ascii_char", "upper", "lower", "trim", "ltrim", "rtrim", "substring", "left", "right", "lpad", "rpad", "replace", "reverse",
                "overlay", "uuid_to_char", "current_role", "current_user")) {
            FUNCTION_RETURN_TYPES.put(each, Types.VARCHAR);
        }
        for (String each : Arrays.asList("ascii_val", "char_length", "character_length", "octet_length", "bit_length", "position", "sign")) {
            FUNCTION_RETURN_TYPES.put(each, Types.INTEGER);
        }
        for (String each : Arrays.asList("gen_id", "count", "bin_and", "bin_or", "bin_xor", "bin_shl", "bin_shr")) {
            FUNCTION_RETURN_TYPES.put(each, Types.BIGINT);
        }
        for (String each : Arrays.asList("sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "exp", "ln", "log", "log10", "power")) {
            FUNCTION_RETURN_TYPES.put(each, Types.DOUBLE);
        }
        FUNCTION_RETURN_TYPES.put("current_timestamp", Types.TIMESTAMP);
        FUNCTION_RETURN_TYPES.put("current_date", Types.DATE);
        FUNCTION_RETURN_TYPES.put("current_time", Types.TIME);
    }
    
    private final FirebirdPrepareStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ReturningSegment returningSegment;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement = resolveCurrentDatabase(sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true));
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(), connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        int statementId = getStatementId();
        if (packet.isValidStatementHandle()) {
            FirebirdStatementResourceCleaner.clean(connectionSession, statementId, true);
        }
        FirebirdServerPreparedStatement serverPreparedStatement = new FirebirdServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        return createResponse(sqlStatementContext);
    }
    
    private SQLStatement resolveCurrentDatabase(final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof DropDatabaseStatement) || null != ((DropDatabaseStatement) sqlStatement).getDatabaseName()) {
            return sqlStatement;
        }
        String currentDatabaseName = connectionSession.getUsedDatabaseName();
        ShardingSpherePreconditions.checkNotNull(currentDatabaseName, NoDatabaseSelectedException::new);
        return new DropDatabaseStatement(sqlStatement.getDatabaseType(), currentDatabaseName, ((DropDatabaseStatement) sqlStatement).isIfExists());
    }
    
    private int getStatementId() {
        return packet.isValidStatementHandle() ? packet.getStatementId() : FirebirdStatementIdGenerator.getInstance().getStatementId(connectionSession.getConnectionId());
    }
    
    private Collection<DatabasePacket> createResponse(final SQLStatementContext sqlStatementContext) {
        FirebirdSQLInfoReturnValue statementType = getFirebirdStatementType(sqlStatementContext.getSqlStatement());
        ShardingSpherePreconditions.checkNotNull(statementType, () -> new UnsupportedSQLOperationException(
                String.format("unsupported SQL statement `%s`", sqlStatementContext.getSqlStatement().getClass().getSimpleName())));
        FirebirdPrepareStatementReturnPacket returnPacket = new FirebirdPrepareStatementReturnPacket();
        while (packet.nextItem()) {
            switch (packet.getCurrentItem()) {
                case STMT_TYPE:
                    returnPacket.setType(statementType);
                    break;
                case SELECT:
                    if (statementType.isSelectDescribable()) {
                        processDescribe(sqlStatementContext, returnPacket.getDescribeSelect(), true);
                    } else {
                        skipDescribe();
                    }
                    break;
                case BIND:
                    if (statementType.isBindDescribable()) {
                        processDescribe(sqlStatementContext, returnPacket.getDescribeBind(), false);
                    } else {
                        skipDescribe();
                    }
                    break;
                default:
                    throw new DatabaseProtocolException("Unknown statement info request type %d", packet.getCurrentItem());
            }
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(returnPacket));
    }
    
    private FirebirdSQLInfoReturnValue getFirebirdStatementType(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return FirebirdSQLInfoReturnValue.SELECT;
        }
        if (statement instanceof InsertStatement) {
            returningSegment = ((InsertStatement) statement).getReturning().orElse(null);
            return null == returningSegment ? FirebirdSQLInfoReturnValue.INSERT : FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
        }
        if (statement instanceof UpdateStatement) {
            returningSegment = ((UpdateStatement) statement).getReturning().orElse(null);
            return null == returningSegment ? FirebirdSQLInfoReturnValue.UPDATE : FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
        }
        if (statement instanceof DeleteStatement) {
            returningSegment = ((DeleteStatement) statement).getReturning().orElse(null);
            return null == returningSegment ? FirebirdSQLInfoReturnValue.DELETE : FirebirdSQLInfoReturnValue.EXEC_PROCEDURE;
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
    
    private void processDescribe(final SQLStatementContext sqlStatementContext, final Collection<FirebirdReturnColumnPacket> describeColumns, final boolean returnAll) {
        // TODO add exception if the first item is not DESCRIBE_VARS
        packet.nextItem();
        Collection<FirebirdSQLInfoPacketType> requestedItems = new LinkedList<>();
        while (packet.nextItem()) {
            requestedItems.add(packet.getCurrentItem());
            if (packet.getCurrentItem() == FirebirdSQLInfoPacketType.DESCRIBE_END) {
                Supplier<ShardingSphereDatabase> database = Suppliers.memoize(() -> ProxyContext.getInstance().getContextManager().getDatabase(connectionSession.getCurrentDatabaseName()));
                Supplier<Optional<ShardingSphereSchema>> schema = Suppliers.memoize(() -> database.get().findDefaultSchema());
                if (returnAll) {
                    processReturnValues(sqlStatementContext, database, schema, describeColumns, requestedItems);
                } else {
                    processParameters(sqlStatementContext, database, schema, describeColumns, requestedItems);
                }
                return;
            }
        }
    }
    
    private void processReturnValues(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                     final Supplier<Optional<ShardingSphereSchema>> schema, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                     final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        int columnCount = 0;
        for (Projection each : getProjections(sqlStatementContext, database, schema)) {
            if (each instanceof ColumnProjection) {
                String tableName = ((ColumnProjection) each).getOriginalTable().getValue();
                ShardingSphereTable table = getTable(tableName.isEmpty() ? getTableNames(sqlStatementContext).iterator().next() : tableName, database, schema);
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
    
    private Collection<Projection> getProjections(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                  final Supplier<Optional<ShardingSphereSchema>> schema) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
            if (!(selectStatementContext.getSqlStatement().getFrom().orElse(null) instanceof SubqueryTableSegment)
                    && selectStatementContext.getProjectionsContext().containsUnexpandedShorthandProjection()) {
                return resolveUnexpandedShorthandProjections(selectStatementContext, database, schema);
            }
            // workaround for SubqueryTableBindUtils transform Expression and Aggregation projections to Column projection
            if (selectStatementContext.getSubqueryContexts().isEmpty() || !(selectStatementContext.getSqlStatement().getFrom().orElse(null) instanceof SubqueryTableSegment)) {
                return selectStatementContext.getProjectionsContext().getExpandProjections();
            }
            Collection<Projection> result = new LinkedList<>();
            for (SelectStatementContext each : selectStatementContext.getSubqueryContexts().values()) {
                result.addAll(each.getProjectionsContext().getExpandProjections());
            }
            return result;
        }
        if (null == returningSegment) {
            return Collections.emptyList();
        }
        ProjectionEngine projectionEngine = new ProjectionEngine(sqlStatementContext.getSqlStatement().getDatabaseType());
        Collection<Projection> result = new LinkedList<>();
        for (ProjectionSegment each : returningSegment.getProjections().getProjections()) {
            Projection projection = projectionEngine.createProjection(each).orElse(null);
            if (projection instanceof ShorthandProjection) {
                result.addAll(processShorthandProjection(sqlStatementContext, database, schema, (ShorthandProjection) projection));
            } else if (!(projection instanceof DerivedProjection) && null != projection) {
                result.add(projection);
            }
        }
        return result;
    }
    
    private Collection<Projection> resolveUnexpandedShorthandProjections(final SelectStatementContext selectStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                                         final Supplier<Optional<ShardingSphereSchema>> schema) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof ShorthandProjection) {
                result.addAll(resolveShorthandColumns(selectStatementContext, database, schema, (ShorthandProjection) each));
            } else if (!(each instanceof DerivedProjection)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<Projection> resolveShorthandColumns(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                           final Supplier<Optional<ShardingSphereSchema>> schema, final ShorthandProjection projection) {
        if (!projection.getActualColumns().isEmpty()) {
            return projection.getActualColumns();
        }
        Collection<Projection> result = new LinkedList<>();
        Optional<IdentifierValue> owner = projection.getOwner();
        for (String tableName : getTableNames(sqlStatementContext)) {
            if (sqlStatementContext instanceof SelectStatementContext && owner.isPresent() && !owner.get().getValue().equalsIgnoreCase(tableName)
                    && !isAliasOf(owner.get().getValue(), tableName, sqlStatementContext)) {
                continue;
            }
            ShardingSphereTable table = getTable(tableName, database, schema);
            table.getAllColumns().forEach(each -> {
                ColumnSegmentBoundInfo info = new ColumnSegmentBoundInfo(null, new IdentifierValue(table.getName()), new IdentifierValue(each.getName()), TableSourceType.PHYSICAL_TABLE);
                result.add(new ColumnProjection(null, new IdentifierValue(each.getName()), null, sqlStatementContext.getSqlStatement().getDatabaseType(), null, null, info));
            });
        }
        return result;
    }
    
    private boolean isAliasOf(final String alias, final String tableName, final SQLStatementContext sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            if (each.getAlias().isPresent() && each.getAlias().get().getValue().equalsIgnoreCase(alias) && each.getTableName().getIdentifier().getValue().equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<Projection> processShorthandProjection(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                              final Supplier<Optional<ShardingSphereSchema>> schema, final ShorthandProjection projection) {
        return resolveShorthandColumns(sqlStatementContext, database, schema, projection);
    }
    
    @HighFrequencyInvocation
    private ShardingSphereTable getTable(final String tableName, final Supplier<ShardingSphereDatabase> database, final Supplier<Optional<ShardingSphereSchema>> schema) {
        ShardingSphereTable result = schema.get().map(optional -> optional.getTable(tableName)).orElse(null);
        if (null == result) {
            result = database.get().getSchema("system_tables").getTable(tableName);
        }
        ShardingSpherePreconditions.checkNotNull(result, () -> new TableNotFoundException(tableName));
        return result;
    }
    
    private void processParameters(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                   final Supplier<Optional<ShardingSphereSchema>> schema, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                   final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        if (sqlStatementContext instanceof InsertStatementContext) {
            processInsertStatement((InsertStatementContext) sqlStatementContext, database, schema, describeColumns, requestedItems);
            return;
        }
        int paramsCount = sqlStatementContext.getSqlStatement().getParameterMarkers().size();
        if (0 == paramsCount) {
            return;
        }
        Map<Integer, ColumnSegment> paramColumns = new HashMap<>(paramsCount, 1F);
        Map<Integer, Integer> paramTypes = new HashMap<>(paramsCount, 1F);
        resolveStatementParams(sqlStatementContext, paramColumns, paramTypes);
        Collection<String> tableNames = getTableNames(sqlStatementContext);
        String fallbackTableName = tableNames.isEmpty() ? "" : tableNames.iterator().next();
        for (int i = 0; i < paramsCount; i++) {
            ColumnSegment columnSegment = paramColumns.get(i);
            int fallbackType = paramTypes.getOrDefault(i, Types.VARCHAR);
            if (null == columnSegment) {
                processCustomColumn(null, null, null, fallbackType, describeColumns, requestedItems, i + 1);
            } else {
                describeParamColumn(columnSegment, fallbackTableName, database, schema, describeColumns, requestedItems, fallbackType, i + 1);
            }
        }
    }
    
    private void processInsertStatement(final InsertStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                        final Supplier<Optional<ShardingSphereSchema>> schema, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                        final Collection<FirebirdSQLInfoPacketType> requestedItems) {
        Collection<String> tableNames = getTableNames(sqlStatementContext);
        Collection<String> affectedColumns = new LinkedList<>();
        for (InsertValueContext context : sqlStatementContext.getInsertValueContexts()) {
            affectedColumns.addAll(processInsertValueContext(sqlStatementContext, context));
        }
        int columnCount = 0;
        for (String tableName : tableNames) {
            ShardingSphereTable table = getTable(tableName, database, schema);
            for (String columnName : affectedColumns) {
                ShardingSphereColumn column = table.getColumn(columnName);
                processColumn(describeColumns, requestedItems, table, column, null, null, ++columnCount);
            }
        }
    }
    
    private Collection<String> processInsertValueContext(final InsertStatementContext context, final InsertValueContext valueContext) {
        Collection<String> result = new LinkedList<>();
        for (int i = 0; i < valueContext.getValueExpressions().size(); i++) {
            if (valueContext.getValueExpressions().get(i) instanceof ParameterMarkerExpressionSegment) {
                result.add(context.getColumnNames().get(i));
            }
        }
        return result;
    }
    
    private Collection<String> getTableNames(final SQLStatementContext sqlStatementContext) {
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        return null == tablesContext ? Collections.emptyList() : tablesContext.getTableNames();
    }
    
    private void resolveStatementParams(final SQLStatementContext sqlStatementContext, final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        if (sqlStatementContext instanceof UpdateStatementContext) {
            for (ColumnAssignmentSegment each : ((UpdateStatementContext) sqlStatementContext).getSqlStatement().getSetAssignment().getAssignments()) {
                resolveExpression(each.getValue(), each.getColumns().get(0), null, paramColumns, paramTypes);
            }
        }
        if (sqlStatementContext instanceof WhereContextAvailable) {
            for (WhereSegment each : getWhereSegments((WhereContextAvailable) sqlStatementContext)) {
                resolveExpression(each.getExpr(), null, null, paramColumns, paramTypes);
            }
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            ((SelectStatementContext) sqlStatementContext).getSqlStatement().getHaving().ifPresent(each -> resolveExpression(each.getExpr(), null, null, paramColumns, paramTypes));
        }
    }
    
    @HighFrequencyInvocation
    private Collection<WhereSegment> getWhereSegments(final WhereContextAvailable sqlStatementContext) {
        if (!(sqlStatementContext instanceof SelectStatementContext) || !((SelectStatementContext) sqlStatementContext).isContainsCombine()) {
            return sqlStatementContext.getWhereSegments();
        }
        List<WhereSegment> result = new ArrayList<>(sqlStatementContext.getWhereSegments());
        appendCombineWhereSegments(((SelectStatementContext) sqlStatementContext).getSqlStatement(), result);
        result.sort(Comparator.comparingInt(WhereSegment::getStartIndex));
        return result;
    }
    
    private void appendCombineWhereSegments(final SelectStatement selectStatement, final Collection<WhereSegment> whereSegments) {
        selectStatement.getCombine().ifPresent(combine -> {
            appendBranchWhereSegments(combine.getLeft().getSelect(), whereSegments);
            appendBranchWhereSegments(combine.getRight().getSelect(), whereSegments);
        });
    }
    
    private void appendBranchWhereSegments(final SelectStatement branch, final Collection<WhereSegment> whereSegments) {
        branch.getWhere().filter(whereSegment -> !whereSegments.contains(whereSegment)).ifPresent(whereSegments::add);
        appendCombineWhereSegments(branch, whereSegments);
    }
    
    private void resolveExpression(final ExpressionSegment expr, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                   final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        if (expr instanceof ParameterMarkerExpressionSegment) {
            int paramIndex = ((ParameterMarkerExpressionSegment) expr).getParameterMarkerIndex();
            if (null != expectedColumn) {
                paramColumns.put(paramIndex, expectedColumn);
            } else if (null != expectedType) {
                paramTypes.put(paramIndex, expectedType);
            }
            return;
        }
        if (expr instanceof BinaryOperationExpression) {
            resolveBinaryOperation((BinaryOperationExpression) expr, expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof InExpression) {
            resolveInExpression((InExpression) expr, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof BetweenExpression) {
            resolveBetweenExpression((BetweenExpression) expr, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof FunctionSegment) {
            resolveFunctionExpression((FunctionSegment) expr, expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof UnaryOperationExpression) {
            UnaryOperationExpression unary = (UnaryOperationExpression) expr;
            boolean logicalNot = "NOT".equalsIgnoreCase(unary.getOperator());
            resolveExpression(unary.getExpression(), logicalNot ? null : expectedColumn, logicalNot ? Types.BOOLEAN : expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof NotExpression) {
            resolveExpression(((NotExpression) expr).getExpression(), null, null, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof CollateExpression) {
            ((CollateExpression) expr).getExpr().ifPresent(each -> resolveExpression(each, expectedColumn, null == expectedType ? Types.VARCHAR : expectedType, paramColumns, paramTypes));
            return;
        }
        if (expr instanceof ListExpression) {
            resolveCollectionExpressions(((ListExpression) expr).getItems(), expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof RowExpression) {
            resolveCollectionExpressions(((RowExpression) expr).getItems(), expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof CaseWhenExpression) {
            resolveCaseWhenExpression((CaseWhenExpression) expr, expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        if (expr instanceof TypeCastExpression) {
            resolveExpression(((TypeCastExpression) expr).getExpression(), null, null, paramColumns, paramTypes);
            return;
        }
        resolveNestedSubqueryExpression(expr, paramColumns, paramTypes);
    }
    
    private void resolveBinaryOperation(final BinaryOperationExpression expr, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                        final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        String operator = expr.getOperator();
        if ("AND".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator)) {
            resolveExpression(expr.getLeft(), null, null, paramColumns, paramTypes);
            resolveExpression(expr.getRight(), null, null, paramColumns, paramTypes);
            return;
        }
        ColumnSegment leftColumn = findDirectColumn(expr.getLeft());
        ColumnSegment rightColumn = findDirectColumn(expr.getRight());
        Integer leftType = findExpressionType(expr.getLeft());
        Integer rightType = findExpressionType(expr.getRight());
        ColumnSegment leftExpectedColumn = null == rightColumn ? expectedColumn : rightColumn;
        Integer leftExpectedType = null == rightType ? expectedType : rightType;
        ColumnSegment rightExpectedColumn = null == leftColumn ? expectedColumn : leftColumn;
        Integer rightExpectedType = null == leftType ? expectedType : leftType;
        if ("||".equals(operator)) {
            leftExpectedType = null == leftExpectedType ? Types.VARCHAR : leftExpectedType;
            rightExpectedType = null == rightExpectedType ? Types.VARCHAR : rightExpectedType;
        }
        resolveExpression(expr.getLeft(), leftExpectedColumn, leftExpectedType, paramColumns, paramTypes);
        resolveExpression(expr.getRight(), rightExpectedColumn, rightExpectedType, paramColumns, paramTypes);
    }
    
    private void resolveInExpression(final InExpression expr, final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        ColumnSegment leftColumn = findDirectColumn(expr.getLeft());
        ColumnSegment rightColumn = findDirectColumn(expr.getRight());
        Integer leftType = findExpressionType(expr.getLeft());
        Integer rightType = findExpressionType(expr.getRight());
        resolveExpression(expr.getLeft(), rightColumn, rightType, paramColumns, paramTypes);
        resolveExpression(expr.getRight(), null == leftColumn ? rightColumn : leftColumn, null == leftType ? rightType : leftType, paramColumns, paramTypes);
    }
    
    private void resolveBetweenExpression(final BetweenExpression expr, final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        ColumnSegment leftColumn = findDirectColumn(expr.getLeft());
        ColumnSegment betweenColumn = findDirectColumn(expr.getBetweenExpr());
        ColumnSegment boundColumn = null == betweenColumn ? findDirectColumn(expr.getAndExpr()) : betweenColumn;
        Integer leftType = findExpressionType(expr.getLeft());
        Integer betweenType = findExpressionType(expr.getBetweenExpr());
        Integer boundType = null == betweenType ? findExpressionType(expr.getAndExpr()) : betweenType;
        resolveExpression(expr.getLeft(), boundColumn, boundType, paramColumns, paramTypes);
        ColumnSegment targetColumn = null == leftColumn ? boundColumn : leftColumn;
        Integer targetType = null == leftType ? boundType : leftType;
        resolveExpression(expr.getBetweenExpr(), targetColumn, targetType, paramColumns, paramTypes);
        resolveExpression(expr.getAndExpr(), targetColumn, targetType, paramColumns, paramTypes);
    }
    
    private void resolveFunctionExpression(final FunctionSegment expr, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                           final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        String functionName = expr.getFunctionName().toLowerCase(Locale.ENGLISH);
        if (PASS_THROUGH_FUNCTIONS.contains(functionName)) {
            ColumnSegment siblingColumn = findFirstDirectColumn(expr.getParameters());
            resolveCollectionExpressions(expr.getParameters(), null == siblingColumn ? expectedColumn : siblingColumn, null == siblingColumn ? expectedType : null, paramColumns, paramTypes);
            return;
        }
        if ("iif".equals(functionName)) {
            resolveIifFunction(expr.getParameters(), expectedColumn, expectedType, paramColumns, paramTypes);
            return;
        }
        int[] argTypes = FUNCTION_ARG_TYPES.get(functionName);
        if (null != argTypes) {
            resolveTypedFunctionArgs(expr.getParameters(), argTypes, paramColumns, paramTypes);
            return;
        }
        resolveCollectionExpressions(expr.getParameters(), null, null, paramColumns, paramTypes);
    }
    
    private void resolveIifFunction(final Collection<ExpressionSegment> params, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                    final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        int index = 0;
        ColumnSegment branchColumn = findFirstBranchDirectColumn(params);
        for (ExpressionSegment each : params) {
            if (0 == index) {
                resolveExpression(each, null, null, paramColumns, paramTypes);
            } else {
                resolveExpression(each, null == branchColumn ? expectedColumn : branchColumn, null == branchColumn ? expectedType : null, paramColumns, paramTypes);
            }
            index++;
        }
    }
    
    @Nullable
    private ColumnSegment findFirstBranchDirectColumn(final Collection<ExpressionSegment> params) {
        int index = 0;
        for (ExpressionSegment each : params) {
            if (0 < index) {
                ColumnSegment result = findDirectColumn(each);
                if (null != result) {
                    return result;
                }
            }
            index++;
        }
        return null;
    }
    
    private void resolveTypedFunctionArgs(final Collection<ExpressionSegment> params, final int[] argTypes,
                                          final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        int index = 0;
        for (ExpressionSegment each : params) {
            int argType = index < argTypes.length ? argTypes[index] : argTypes[argTypes.length - 1];
            resolveExpression(each, null, argType, paramColumns, paramTypes);
            index++;
        }
    }
    
    private void resolveCollectionExpressions(final Collection<ExpressionSegment> exprs, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                              final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        for (ExpressionSegment each : exprs) {
            resolveExpression(each, expectedColumn, expectedType, paramColumns, paramTypes);
        }
    }
    
    private void resolveCaseWhenExpression(final CaseWhenExpression expr, @Nullable final ColumnSegment expectedColumn, @Nullable final Integer expectedType,
                                           final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        ColumnSegment caseColumn = null == expr.getCaseExpr() ? null : findDirectColumn(expr.getCaseExpr());
        if (null != expr.getCaseExpr()) {
            resolveExpression(expr.getCaseExpr(), findFirstDirectColumn(expr.getWhenExprs()), null, paramColumns, paramTypes);
        }
        resolveCollectionExpressions(expr.getWhenExprs(), caseColumn, null, paramColumns, paramTypes);
        ColumnSegment thenColumn = findFirstDirectColumn(expr.getThenExprs());
        ColumnSegment elseColumn = null == expr.getElseExpr() ? null : findDirectColumn(expr.getElseExpr());
        ColumnSegment fallbackColumn = null == elseColumn ? expectedColumn : elseColumn;
        ColumnSegment resultColumn = null == thenColumn ? fallbackColumn : thenColumn;
        Integer resultType = null == resultColumn ? expectedType : null;
        resolveCollectionExpressions(expr.getThenExprs(), resultColumn, resultType, paramColumns, paramTypes);
        if (null != expr.getElseExpr()) {
            resolveExpression(expr.getElseExpr(), resultColumn, resultType, paramColumns, paramTypes);
        }
    }
    
    private void resolveNestedSubqueryExpression(final ExpressionSegment expr, final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        if (expr instanceof SubqueryExpressionSegment) {
            resolveSubquerySelect(((SubqueryExpressionSegment) expr).getSubquery().getSelect(), paramColumns, paramTypes);
        } else if (expr instanceof SubquerySegment) {
            resolveSubquerySelect(((SubquerySegment) expr).getSelect(), paramColumns, paramTypes);
        } else if (expr instanceof ExistsSubqueryExpression) {
            resolveSubquerySelect(((ExistsSubqueryExpression) expr).getSubquery().getSelect(), paramColumns, paramTypes);
        } else if (expr instanceof CommonExpressionSegment) {
            resolveCollectionExpressions(((CommonExpressionSegment) expr).getChildren(), null, null, paramColumns, paramTypes);
        }
    }
    
    private void resolveSubquerySelect(final SelectStatement selectStatement, final Map<Integer, ColumnSegment> paramColumns, final Map<Integer, Integer> paramTypes) {
        selectStatement.getWhere().ifPresent(each -> resolveExpression(each.getExpr(), null, null, paramColumns, paramTypes));
        selectStatement.getHaving().ifPresent(each -> resolveExpression(each.getExpr(), null, null, paramColumns, paramTypes));
        selectStatement.getCombine().ifPresent(combine -> {
            resolveSubquerySelect(combine.getLeft().getSelect(), paramColumns, paramTypes);
            resolveSubquerySelect(combine.getRight().getSelect(), paramColumns, paramTypes);
        });
    }
    
    @Nullable
    private ColumnSegment findDirectColumn(final ExpressionSegment expr) {
        if (expr instanceof ColumnSegment) {
            return (ColumnSegment) expr;
        }
        if (expr instanceof UnaryOperationExpression) {
            return findDirectColumn(((UnaryOperationExpression) expr).getExpression());
        }
        if (expr instanceof CollateExpression) {
            return ((CollateExpression) expr).getExpr().map(this::findDirectColumn).orElse(null);
        }
        if (expr instanceof BinaryOperationExpression) {
            return findBinaryOperationDirectColumn((BinaryOperationExpression) expr);
        }
        if (expr instanceof ListExpression) {
            return findFirstDirectColumn(((ListExpression) expr).getItems());
        }
        if (expr instanceof RowExpression) {
            return findFirstDirectColumn(((RowExpression) expr).getItems());
        }
        if (expr instanceof FunctionSegment) {
            FunctionSegment function = (FunctionSegment) expr;
            return PASS_THROUGH_FUNCTIONS.contains(function.getFunctionName().toLowerCase(Locale.ENGLISH)) ? findFirstDirectColumn(function.getParameters()) : null;
        }
        return null;
    }
    
    @Nullable
    private ColumnSegment findBinaryOperationDirectColumn(final BinaryOperationExpression expr) {
        String operator = expr.getOperator();
        if ("AND".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "||".equals(operator)) {
            return null;
        }
        ColumnSegment leftColumn = findDirectColumn(expr.getLeft());
        return null == leftColumn ? findDirectColumn(expr.getRight()) : leftColumn;
    }
    
    @Nullable
    private ColumnSegment findFirstDirectColumn(final Collection<ExpressionSegment> exprs) {
        for (ExpressionSegment each : exprs) {
            ColumnSegment result = findDirectColumn(each);
            if (null != result) {
                return result;
            }
        }
        return null;
    }
    
    @Nullable
    private Integer findExpressionType(final ExpressionSegment expr) {
        if (expr instanceof UnaryOperationExpression) {
            return findExpressionType(((UnaryOperationExpression) expr).getExpression());
        }
        if (expr instanceof FunctionSegment) {
            return FUNCTION_RETURN_TYPES.get(((FunctionSegment) expr).getFunctionName().toLowerCase(Locale.ENGLISH));
        }
        if (expr instanceof BinaryOperationExpression) {
            return "||".equals(((BinaryOperationExpression) expr).getOperator()) ? Types.VARCHAR : null;
        }
        return null;
    }
    
    private void describeParamColumn(final ColumnSegment columnSegment, final String fallbackTableName, final Supplier<ShardingSphereDatabase> database,
                                     final Supplier<Optional<ShardingSphereSchema>> schema, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                     final Collection<FirebirdSQLInfoPacketType> requestedItems, final int fallbackType, final int columnIndex) {
        String tableName = null != columnSegment.getColumnBoundInfo() && null != columnSegment.getColumnBoundInfo().getOriginalTable()
                ? columnSegment.getColumnBoundInfo().getOriginalTable().getValue()
                : fallbackTableName;
        ShardingSphereTable table = tableName.isEmpty() ? null : getTable(tableName, database, schema);
        String columnName = null != columnSegment.getColumnBoundInfo() && null != columnSegment.getColumnBoundInfo().getOriginalColumn()
                ? columnSegment.getColumnBoundInfo().getOriginalColumn().getValue()
                : columnSegment.getIdentifier().getValue();
        ShardingSphereColumn column = null == table ? null : table.getColumn(columnName);
        if (null != table && null != column) {
            processColumn(describeColumns, requestedItems, table, column, columnSegment.getOwner().map(OwnerSegment::getIdentifier).orElse(null), columnSegment.getIdentifier(), columnIndex);
        } else {
            processCustomColumn(null, null, null, fallbackType, describeColumns, requestedItems, columnIndex);
        }
    }
    
    private void processExpressionProjection(final ExpressionProjection expr, final Collection<FirebirdReturnColumnPacket> describeColumns, final Collection<FirebirdSQLInfoPacketType> requestedItems,
                                             final int columnCount) {
        ExpressionSegment exprSegment = expr.getExpressionSegment().getExpr();
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
            processLiteralColumn(value, expr.getAlias().orElse(null), type, describeColumns, requestedItems, columnCount);
        }
    }
    
    private int getFunctionType(final String functionName) {
        // TODO add proper coalesce and other conditional functions return types
        switch (functionName.toLowerCase(Locale.ENGLISH)) {
            case "substring":
            case "current_role":
            case "current_user":
            case "coalesce":
                return Types.VARCHAR;
            case "gen_id":
            case "count":
                return Types.BIGINT;
            case "current_timestamp":
                return Types.TIMESTAMP;
            default:
                return Types.INTEGER;
        }
    }
    
    private String getOperationName(final String operationType) {
        return "||".equals(operationType) ? "CONCATENATION" : operationType;
    }
    
    private int getOperationType(final String operationType) {
        return "||".equals(operationType) ? Types.VARCHAR : Types.INTEGER;
    }
    
    @HighFrequencyInvocation
    private void processLiteralColumn(@Nullable final Object value, @Nullable final IdentifierValue columnAlias, final int dataType, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                      final Collection<FirebirdSQLInfoPacketType> requestedItems, final int columnCount) {
        ShardingSphereColumn column = new ShardingSphereColumn("CONSTANT", dataType, false, false, true, true, false, false);
        String columnAliasString = null == columnAlias ? column.getName() : columnAlias.getValue();
        String owner = connectionSession.getConnectionContext().getGrantee().getUsername();
        Integer columnLength = value instanceof String ? getStringLiteralLength((String) value) : null;
        describeColumns.add(new FirebirdReturnColumnPacket(requestedItems, columnCount, LITERAL_TABLE, column, null, columnAliasString, owner, columnLength, false, null));
    }
    
    private int getStringLiteralLength(final String value) {
        String text = value.replace("''", "'");
        return text.codePointCount(0, text.length());
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
        boolean blobColumn = isBlobColumn(table, column);
        Integer blobSubtype = blobColumn ? resolveBlobSubtype(table, column) : null;
        Integer columnLength = blobColumn ? null : resolveColumnLength(table, column);
        describeColumns.add(new FirebirdReturnColumnPacket(requestedItems, idx, table, column, tableAliasString, columnAliasString, owner, columnLength, blobColumn, blobSubtype));
    }
    
    private boolean isBlobColumn(final ShardingSphereTable table, final ShardingSphereColumn column) {
        if (null == table || null == column) {
            return false;
        }
        if (null != table.getName() && FirebirdBlobInfoRegistry.isBlobColumn(connectionSession.getCurrentDatabaseName(), getActualTableName(table.getName()), column.getName())) {
            return true;
        }
        return Types.BLOB == column.getDataType();
    }
    
    @HighFrequencyInvocation
    private String getActualTableName(final String logicTableName) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(connectionSession.getCurrentDatabaseName());
        for (DataNodeRuleAttribute each : database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            Optional<String> actualTableName = each.findFirstActualTable(logicTableName);
            if (actualTableName.isPresent()) {
                return actualTableName.get();
            }
        }
        return logicTableName;
    }
    
    private Integer resolveBlobSubtype(final ShardingSphereTable table, final ShardingSphereColumn column) {
        OptionalInt subtype = FirebirdBlobInfoRegistry.findBlobSubtype(connectionSession.getCurrentDatabaseName(), getActualTableName(table.getName()), column.getName());
        return subtype.isPresent() ? subtype.getAsInt() : null;
    }
    
    private Integer resolveColumnLength(final ShardingSphereTable table, final ShardingSphereColumn column) {
        if (null == table || null == column || !isDynamicLengthType(column.getDataType())) {
            return null;
        }
        OptionalInt columnSize = FirebirdNonFixedLengthColumnSizeRegistry.findColumnSize(connectionSession.getCurrentDatabaseName(), table.getName(), column.getName());
        return columnSize.isPresent() ? columnSize.getAsInt() : null;
    }
    
    private boolean isDynamicLengthType(final int dataType) {
        switch (dataType) {
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
                return true;
            default:
                return false;
        }
    }
}
