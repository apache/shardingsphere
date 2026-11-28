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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
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
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
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
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Firebird prepare transaction command executor.
 */
@RequiredArgsConstructor
public final class FirebirdPrepareStatementCommandExecutor implements CommandExecutor {
    
    private static final ShardingSphereTable LITERAL_TABLE = new ShardingSphereTable(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    
    private final FirebirdPrepareStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ReturningSegment returningSegment;
    
    private final Map<SelectStatement, List<CommonTableExpressionSegment>> commonTableExpressionScopes = new IdentityHashMap<>();
    
    private final Map<SelectStatement, Collection<TableSegment>> tableSources = new IdentityHashMap<>();
    
    private final Map<SelectStatement, Collection<ProjectionSegment>> expandedProjectionSegments = new IdentityHashMap<>();
    
    private final Map<SelectStatement, Collection<ColumnSegmentBoundInfo>> shorthandColumnBoundInfos = new IdentityHashMap<>();
    
    private final Map<SelectStatement, Map<String, ProjectionSegment>> labeledProjectionSegments = new IdentityHashMap<>();
    
    private final Map<SelectStatement, Map<String, ProjectionSegment>> caseInsensitiveLabeledProjectionSegments = new IdentityHashMap<>();
    
    private final Map<ProjectionSegment, IdentifierValue> declaredColumnLabels = new IdentityHashMap<>();
    
    private final Map<ProjectionSegment, Collection<ProjectionSegment>> mergedJoinColumnSegments = new IdentityHashMap<>();
    
    private final Map<Projection, Collection<ProjectionSegment>> mergedJoinColumnProjections = new IdentityHashMap<>();
    
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
            Optional<Projection> originProjection = findOriginProjection(sqlStatementContext, each, database, schema);
            Projection projection = originProjection.orElse(each);
            IdentifierValue columnAlias = originProjection.isPresent() || isTemporaryTableColumn(each)
                    ? getDerivedColumnAlias(sqlStatementContext, (ColumnProjection) each, projection)
                    : each.getAlias().orElse(null);
            if (projection instanceof ColumnProjection) {
                ShardingSphereTable table = getTable(getTableName(sqlStatementContext, (ColumnProjection) projection), database, schema);
                ShardingSphereColumn column = table.getColumn(((ColumnProjection) projection).getOriginalColumn().getValue());
                processColumn(describeColumns, requestedItems, table, column, ((ColumnProjection) projection).getOwner().orElse(null), columnAlias, ++columnCount);
            } else if (projection instanceof ExpressionProjection) {
                processExpressionProjection((ExpressionProjection) projection, columnAlias, describeColumns, requestedItems, ++columnCount);
            } else if (projection instanceof AggregationProjection) {
                String functionName = ((AggregationProjection) projection).getType().name();
                processCustomColumn(null, functionName, columnAlias, getFunctionType(functionName), describeColumns, requestedItems, ++columnCount);
            } else if (projection instanceof SubqueryProjection) {
                processCustomColumn(null, ((SubqueryProjection) projection).getColumnName(), columnAlias, Types.INTEGER, describeColumns, requestedItems, ++columnCount);
            }
        }
    }
    
    private Collection<Projection> getProjections(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                  final Supplier<Optional<ShardingSphereSchema>> schema) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
            if (selectStatementContext.getProjectionsContext().containsUnexpandedShorthandProjection()) {
                return resolveUnexpandedShorthandProjections(selectStatementContext, database, schema);
            }
            return expandShorthandProjections(selectStatementContext);
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
        TableSegment from = selectStatementContext.getSqlStatement().getFrom().orElse(null);
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof ShorthandProjection && from instanceof SubqueryTableSegment && ((ShorthandProjection) each).getActualColumns().isEmpty()) {
                result.addAll(selectStatementContext.getSubqueryContexts().get(((SubqueryTableSegment) from).getSubquery().getStartIndex()).getProjectionsContext().getExpandProjections());
            } else if (each instanceof ShorthandProjection) {
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
    
    @HighFrequencyInvocation
    private Collection<Projection> expandShorthandProjections(final SelectStatementContext selectStatementContext) {
        SelectStatement select = selectStatementContext.getSqlStatement();
        List<CommonTableExpressionSegment> commonTableExpressions = getCommonTableExpressions(Collections.emptyList(), select);
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof ShorthandProjection) {
                result.addAll(getShorthandColumns(commonTableExpressions, select, (ShorthandProjection) each));
            } else if (!(each instanceof DerivedProjection)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<CommonTableExpressionSegment> getCommonTableExpressions(final List<CommonTableExpressionSegment> outerCommonTableExpressions, final SelectStatement select) {
        return commonTableExpressionScopes.computeIfAbsent(select, key -> createCommonTableExpressions(outerCommonTableExpressions, key));
    }
    
    private List<CommonTableExpressionSegment> createCommonTableExpressions(final List<CommonTableExpressionSegment> outerCommonTableExpressions, final SelectStatement select) {
        if (!select.getWith().isPresent()) {
            return outerCommonTableExpressions;
        }
        Collection<CommonTableExpressionSegment> localCommonTableExpressions = select.getWith().get().getCommonTableExpressions();
        List<CommonTableExpressionSegment> result = new ArrayList<>(outerCommonTableExpressions.size() + localCommonTableExpressions.size());
        result.addAll(outerCommonTableExpressions);
        result.addAll(localCommonTableExpressions);
        return result;
    }
    
    private Collection<Projection> getShorthandColumns(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select, final ShorthandProjection projection) {
        Optional<ShorthandProjectionSegment> shorthandProjection = findShorthandProjectionSegment(select, projection.getOwner().orElse(null));
        Optional<Collection<ProjectionSegment>> derivedColumns =
                shorthandProjection.flatMap(shorthandProjectionSegment -> findDerivedShorthandColumnSegments(commonTableExpressions, select, shorthandProjectionSegment));
        if (!derivedColumns.isPresent()) {
            return projection.getActualColumns();
        }
        ProjectionEngine projectionEngine = new ProjectionEngine(select.getDatabaseType());
        Collection<Projection> result = new LinkedList<>();
        for (ProjectionSegment each : derivedColumns.get()) {
            Optional<Projection> derivedColumn = projectionEngine.createProjection(each);
            if (derivedColumn.isPresent()) {
                Collection<ProjectionSegment> joinColumns = mergedJoinColumnSegments.get(each);
                if (null != joinColumns) {
                    mergedJoinColumnProjections.put(derivedColumn.get(), joinColumns);
                }
                result.add(derivedColumn.get());
            }
        }
        return result;
    }
    
    private Optional<ShorthandProjectionSegment> findShorthandProjectionSegment(final SelectStatement select, @Nullable final IdentifierValue owner) {
        for (ProjectionSegment each : select.getProjections().getProjections()) {
            if (each instanceof ShorthandProjectionSegment && isSameOwner(((ShorthandProjectionSegment) each).getOwner().map(OwnerSegment::getIdentifier).orElse(null), owner)) {
                return Optional.of((ShorthandProjectionSegment) each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isSameOwner(@Nullable final IdentifierValue owner, @Nullable final IdentifierValue otherOwner) {
        return null == owner || null == otherOwner ? owner == otherOwner : getIdentifierKey(owner).equals(getIdentifierKey(otherOwner));
    }
    
    private String getIdentifierKey(final IdentifierValue identifier) {
        return QuoteCharacter.NONE == identifier.getQuoteCharacter() ? identifier.getValue().toUpperCase(Locale.ENGLISH) : identifier.getValue();
    }
    
    private Optional<Collection<ProjectionSegment>> findDerivedShorthandColumnSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                                       final ShorthandProjectionSegment shorthandProjection) {
        Optional<TableSegment> tableSource = findShorthandTableSource(select, shorthandProjection.getOwner().map(OwnerSegment::getIdentifier).orElse(null));
        if (!tableSource.isPresent() || !containsDerivedTableSource(commonTableExpressions, tableSource.get())) {
            return Optional.empty();
        }
        return Optional.of(getTableSourceColumnSegments(commonTableExpressions, select, tableSource.get(), shorthandProjection.getActualProjectionSegments()));
    }
    
    private Optional<TableSegment> findShorthandTableSource(final SelectStatement select, @Nullable final IdentifierValue owner) {
        if (null == owner) {
            return select.getFrom().filter(this::isExpandableBySource);
        }
        String ownerKey = getIdentifierKey(owner);
        for (TableSegment each : getTableSources(select)) {
            if (ownerKey.equals(getIdentifierKey(getTableAliasOrName(each)))) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isExpandableBySource(final TableSegment tableSegment) {
        if (tableSegment instanceof JoinTableSegment) {
            return isExpandableBySource(((JoinTableSegment) tableSegment).getLeft()) && isExpandableBySource(((JoinTableSegment) tableSegment).getRight());
        }
        return tableSegment instanceof SimpleTableSegment || tableSegment instanceof SubqueryTableSegment;
    }
    
    private Collection<TableSegment> getTableSources(final SelectStatement select) {
        return tableSources.computeIfAbsent(select, this::createTableSources);
    }
    
    private Collection<TableSegment> createTableSources(final SelectStatement select) {
        Collection<TableSegment> result = new LinkedList<>();
        select.getFrom().ifPresent(from -> addTableSources(from, result));
        return result;
    }
    
    private void addTableSources(final TableSegment tableSegment, final Collection<TableSegment> tableSources) {
        if (tableSegment instanceof JoinTableSegment) {
            addTableSources(((JoinTableSegment) tableSegment).getLeft(), tableSources);
            addTableSources(((JoinTableSegment) tableSegment).getRight(), tableSources);
        } else if (tableSegment instanceof SimpleTableSegment || tableSegment instanceof SubqueryTableSegment) {
            tableSources.add(tableSegment);
        }
    }
    
    private IdentifierValue getTableAliasOrName(final TableSegment tableSegment) {
        return tableSegment.getAlias().orElseGet(() -> tableSegment instanceof SimpleTableSegment ? ((SimpleTableSegment) tableSegment).getTableName().getIdentifier() : new IdentifierValue(""));
    }
    
    private boolean containsDerivedTableSource(final List<CommonTableExpressionSegment> commonTableExpressions, final TableSegment tableSource) {
        if (tableSource instanceof JoinTableSegment) {
            JoinTableSegment joinTable = (JoinTableSegment) tableSource;
            return containsDerivedTableSource(commonTableExpressions, joinTable.getLeft()) || containsDerivedTableSource(commonTableExpressions, joinTable.getRight());
        }
        return tableSource instanceof SubqueryTableSegment || findCommonTableExpressionIndex(commonTableExpressions, tableSource) >= 0;
    }
    
    private int findCommonTableExpressionIndex(final List<CommonTableExpressionSegment> commonTableExpressions, final TableSegment tableSource) {
        if (!(tableSource instanceof SimpleTableSegment) || ((SimpleTableSegment) tableSource).getOwner().isPresent()) {
            return -1;
        }
        String tableNameKey = getIdentifierKey(((SimpleTableSegment) tableSource).getTableName().getIdentifier());
        for (int i = commonTableExpressions.size() - 1; i >= 0; i--) {
            if (commonTableExpressions.get(i).getAlias().filter(alias -> tableNameKey.equals(getIdentifierKey(alias))).isPresent()) {
                return i;
            }
        }
        return -1;
    }
    
    private Collection<ProjectionSegment> getTableSourceColumnSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                       final TableSegment tableSource, final Collection<ProjectionSegment> actualColumns) {
        if (tableSource instanceof JoinTableSegment) {
            return getJoinTableColumnSegments(commonTableExpressions, select, (JoinTableSegment) tableSource, actualColumns);
        }
        if (tableSource instanceof SubqueryTableSegment) {
            SubqueryTableSegment subqueryTable = (SubqueryTableSegment) tableSource;
            SelectStatement subquerySelect = subqueryTable.getSubquery().getSelect();
            Collection<ProjectionSegment> projections = getExpandedProjectionSegments(getCommonTableExpressions(commonTableExpressions, subquerySelect), getAnchorSelect(subquerySelect));
            return createDerivedTableColumnSegments(select.getDatabaseType(), projections, subqueryTable.getColumns(), getTableAliasOrName(subqueryTable));
        }
        int commonTableExpressionIndex = findCommonTableExpressionIndex(commonTableExpressions, tableSource);
        if (commonTableExpressionIndex >= 0) {
            CommonTableExpressionSegment commonTableExpression = commonTableExpressions.get(commonTableExpressionIndex);
            SelectStatement commonTableExpressionSelect = commonTableExpression.getSubquery().getSelect();
            List<CommonTableExpressionSegment> scopedCommonTableExpressions = getCommonTableExpressions(commonTableExpressions.subList(0, commonTableExpressionIndex), commonTableExpressionSelect);
            Collection<ProjectionSegment> projections = getExpandedProjectionSegments(scopedCommonTableExpressions, getAnchorSelect(commonTableExpressionSelect));
            return createDerivedTableColumnSegments(select.getDatabaseType(), projections, commonTableExpression.getColumns(), getTableAliasOrName(tableSource));
        }
        String tableAliasOrNameKey = getIdentifierKey(getTableAliasOrName(tableSource));
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : actualColumns) {
            if (isPhysicalTableColumnOf(each, tableAliasOrNameKey)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<ProjectionSegment> getJoinTableColumnSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                     final JoinTableSegment joinTable, final Collection<ProjectionSegment> actualColumns) {
        Collection<ProjectionSegment> leftColumns = getTableSourceColumnSegments(commonTableExpressions, select, joinTable.getLeft(), actualColumns);
        Collection<ProjectionSegment> rightColumns = getTableSourceColumnSegments(commonTableExpressions, select, joinTable.getRight(), actualColumns);
        Collection<String> usingColumnKeys = joinTable.isNatural() ? getNaturalJoinColumnKeys(joinTable, leftColumns, rightColumns) : getUsingColumnKeys(joinTable.getUsing());
        Collection<ProjectionSegment> result = new LinkedList<>();
        if (usingColumnKeys.isEmpty()) {
            result.addAll(leftColumns);
            result.addAll(rightColumns);
            return result;
        }
        boolean rightJoin = JoinType.RIGHT.name().equalsIgnoreCase(joinTable.getJoinType());
        for (String each : usingColumnKeys) {
            Optional<ProjectionSegment> usingColumn = findColumnSegment(rightJoin ? rightColumns : leftColumns, each);
            Optional<ProjectionSegment> otherUsingColumn = findColumnSegment(rightJoin ? leftColumns : rightColumns, each);
            if (usingColumn.isPresent() && otherUsingColumn.isPresent()) {
                mergedJoinColumnSegments.put(usingColumn.get(), Arrays.asList(usingColumn.get(), otherUsingColumn.get()));
            }
            (usingColumn.isPresent() ? usingColumn : otherUsingColumn).ifPresent(result::add);
        }
        addRemainingColumnSegments(leftColumns, usingColumnKeys, result);
        addRemainingColumnSegments(rightColumns, usingColumnKeys, result);
        return result;
    }
    
    private Collection<String> getNaturalJoinColumnKeys(final JoinTableSegment joinTable, final Collection<ProjectionSegment> leftColumns, final Collection<ProjectionSegment> rightColumns) {
        Collection<String> rightColumnKeys = new HashSet<>(rightColumns.size(), 1F);
        Collection<String> caseInsensitiveRightColumnKeys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ProjectionSegment each : rightColumns) {
            String columnKey = getColumnLabelKey(each);
            rightColumnKeys.add(columnKey);
            caseInsensitiveRightColumnKeys.add(columnKey);
        }
        Collection<String> derivedUsingColumnKeys = getUsingColumnKeys(joinTable.getDerivedUsing());
        Collection<String> result = new LinkedHashSet<>();
        for (ProjectionSegment each : leftColumns) {
            String columnKey = getColumnLabelKey(each);
            if (rightColumnKeys.contains(columnKey) || derivedUsingColumnKeys.contains(columnKey) && !caseInsensitiveRightColumnKeys.contains(columnKey)) {
                result.add(columnKey);
            }
        }
        return result;
    }
    
    private Collection<String> getUsingColumnKeys(final Collection<ColumnSegment> usingColumns) {
        Collection<String> result = new LinkedHashSet<>(usingColumns.size(), 1F);
        for (ColumnSegment each : usingColumns) {
            result.add(getIdentifierKey(each.getIdentifier()));
        }
        return result;
    }
    
    private Optional<ProjectionSegment> findColumnSegment(final Collection<ProjectionSegment> columns, final String columnKey) {
        for (ProjectionSegment each : columns) {
            if (columnKey.equals(getColumnLabelKey(each))) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private String getColumnLabelKey(final ProjectionSegment projection) {
        if (projection instanceof AliasAvailable && ((AliasAvailable) projection).getAlias().isPresent()) {
            return getIdentifierKey(((AliasAvailable) projection).getAlias().get());
        }
        return projection instanceof ColumnProjectionSegment ? getIdentifierKey(((ColumnProjectionSegment) projection).getColumn().getIdentifier()) : projection.getColumnLabel();
    }
    
    private void addRemainingColumnSegments(final Collection<ProjectionSegment> columns, final Collection<String> usingColumnKeys, final Collection<ProjectionSegment> remainingColumns) {
        for (ProjectionSegment each : columns) {
            if (!usingColumnKeys.contains(getColumnLabelKey(each))) {
                remainingColumns.add(each);
            }
        }
    }
    
    private SelectStatement getAnchorSelect(final SelectStatement select) {
        return select.getCombine().map(optional -> optional.getLeft().getSelect()).orElse(select);
    }
    
    private Collection<ProjectionSegment> getExpandedProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select) {
        Collection<ProjectionSegment> result = expandedProjectionSegments.get(select);
        if (null == result) {
            result = expandProjectionSegments(commonTableExpressions, select);
            expandedProjectionSegments.put(select, result);
        }
        return result;
    }
    
    private Collection<ProjectionSegment> expandProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : select.getProjections().getProjections()) {
            if (each instanceof ShorthandProjectionSegment) {
                ShorthandProjectionSegment shorthandProjection = (ShorthandProjectionSegment) each;
                result.addAll(findDerivedShorthandColumnSegments(commonTableExpressions, select, shorthandProjection).orElseGet(shorthandProjection::getActualProjectionSegments));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<ProjectionSegment> createDerivedTableColumnSegments(final DatabaseType databaseType, final Collection<ProjectionSegment> projections,
                                                                           final Collection<ColumnSegment> declaredColumns, final IdentifierValue tableName) {
        Iterator<ColumnSegment> columns = declaredColumns.iterator();
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : SubqueryTableBindUtils.createSubqueryProjections(projections, tableName, databaseType, TableSourceType.TEMPORARY_TABLE)) {
            if (each.isVisible()) {
                result.add(columns.hasNext() ? createDeclaredColumnSegment(tableName, columns.next().getIdentifier(), each) : each);
            }
        }
        return result;
    }
    
    private ColumnProjectionSegment createDeclaredColumnSegment(final IdentifierValue tableName, final IdentifierValue columnName, final ProjectionSegment projection) {
        ColumnSegment result = new ColumnSegment(0, 0, columnName);
        if (!tableName.getValue().isEmpty()) {
            result.setOwner(new OwnerSegment(0, 0, tableName));
        }
        result.setColumnBoundInfo(projection instanceof ColumnProjectionSegment
                ? ((ColumnProjectionSegment) projection).getColumn().getColumnBoundInfo()
                : new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(null, null), new IdentifierValue(""), columnName, TableSourceType.TEMPORARY_TABLE));
        return new ColumnProjectionSegment(result);
    }
    
    private boolean isPhysicalTableColumnOf(final ProjectionSegment projection, final String tableAliasOrNameKey) {
        if (!(projection instanceof ColumnProjectionSegment)) {
            return false;
        }
        ColumnSegment column = ((ColumnProjectionSegment) projection).getColumn();
        return null != column.getColumnBoundInfo() && TableSourceType.PHYSICAL_TABLE == column.getColumnBoundInfo().getTableSourceType()
                && column.getOwner().filter(owner -> tableAliasOrNameKey.equals(getIdentifierKey(owner.getIdentifier()))).isPresent();
    }
    
    private Collection<Projection> processShorthandProjection(final SQLStatementContext sqlStatementContext, final Supplier<ShardingSphereDatabase> database,
                                                              final Supplier<Optional<ShardingSphereSchema>> schema, final ShorthandProjection projection) {
        return resolveShorthandColumns(sqlStatementContext, database, schema, projection);
    }
    
    @HighFrequencyInvocation
    private Optional<Projection> findOriginProjection(final SQLStatementContext sqlStatementContext, final Projection projection, final Supplier<ShardingSphereDatabase> database,
                                                      final Supplier<Optional<ShardingSphereSchema>> schema) {
        if (!(projection instanceof ColumnProjection) || !(sqlStatementContext.getSqlStatement() instanceof SelectStatement)) {
            return Optional.empty();
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatementContext.getSqlStatement();
        List<CommonTableExpressionSegment> commonTableExpressions = getCommonTableExpressions(Collections.emptyList(), selectStatement);
        Collection<ProjectionSegment> joinColumns = mergedJoinColumnProjections.get(projection);
        if (null != joinColumns) {
            return findCommonTypeProjection(sqlStatementContext, getJoinColumnOriginProjectionSegments(commonTableExpressions, selectStatement, joinColumns), database, schema);
        }
        ColumnProjection columnProjection = (ColumnProjection) projection;
        if (!isDerivedColumn(columnProjection.getColumnBoundInfo())) {
            return Optional.empty();
        }
        Collection<ProjectionSegment> result = findOriginProjectionSegments(commonTableExpressions, selectStatement, columnProjection.getOwner().orElse(null), columnProjection.getName(),
                columnProjection.getColumnBoundInfo(), true);
        return findCommonTypeProjection(sqlStatementContext, result, database, schema);
    }
    
    private Collection<ProjectionSegment> getJoinColumnOriginProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                                final Collection<ProjectionSegment> joinColumns) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : joinColumns) {
            result.addAll(traceOriginProjectionSegments(commonTableExpressions, select, each));
        }
        return result;
    }
    
    private Optional<Projection> findCommonTypeProjection(final SQLStatementContext sqlStatementContext, final Collection<ProjectionSegment> projectionSegments,
                                                          final Supplier<ShardingSphereDatabase> database, final Supplier<Optional<ShardingSphereSchema>> schema) {
        if (projectionSegments.isEmpty()) {
            return Optional.empty();
        }
        ProjectionEngine projectionEngine = new ProjectionEngine(sqlStatementContext.getSqlStatement().getDatabaseType());
        Iterator<ProjectionSegment> iterator = projectionSegments.iterator();
        Optional<Projection> result = projectionEngine.createProjection(iterator.next());
        if (!result.isPresent() || !iterator.hasNext()) {
            return result;
        }
        int resultDataType = getDataType(sqlStatementContext, result.get(), database, schema);
        while (iterator.hasNext()) {
            Optional<Projection> projection = projectionEngine.createProjection(iterator.next());
            int dataType = projection.isPresent() ? getDataType(sqlStatementContext, projection.get(), database, schema) : Types.NULL;
            if (isBroaderDataType(dataType, resultDataType)) {
                result = projection;
                resultDataType = dataType;
            }
        }
        return result;
    }
    
    private int getDataType(final SQLStatementContext sqlStatementContext, final Projection projection, final Supplier<ShardingSphereDatabase> database,
                            final Supplier<Optional<ShardingSphereSchema>> schema) {
        if (projection instanceof ColumnProjection) {
            ShardingSphereTable table = getTable(getTableName(sqlStatementContext, (ColumnProjection) projection), database, schema);
            ShardingSphereColumn column = table.getColumn(((ColumnProjection) projection).getOriginalColumn().getValue());
            return null == column ? Types.NULL : column.getDataType();
        }
        if (projection instanceof ExpressionProjection) {
            return getExpressionType(((ExpressionProjection) projection).getExpressionSegment().getExpr());
        }
        if (projection instanceof AggregationProjection) {
            return getFunctionType(((AggregationProjection) projection).getType().name());
        }
        return projection instanceof SubqueryProjection ? Types.INTEGER : Types.NULL;
    }
    
    private int getExpressionType(final ExpressionSegment expr) {
        if (expr instanceof FunctionSegment) {
            return getFunctionType(((FunctionSegment) expr).getFunctionName());
        }
        if (expr instanceof BinaryOperationExpression) {
            return getOperationType(((BinaryOperationExpression) expr).getOperator());
        }
        return expr instanceof LiteralExpressionSegment ? getLiteralType(((LiteralExpressionSegment) expr).getLiterals()) : Types.NULL;
    }
    
    private boolean isBroaderDataType(final int dataType, final int currentDataType) {
        if (Types.NULL == currentDataType) {
            return Types.NULL != dataType;
        }
        int currentPrecedence = getNumericTypePrecedence(currentDataType);
        return currentPrecedence > 0 && getNumericTypePrecedence(dataType) > currentPrecedence;
    }
    
    private int getNumericTypePrecedence(final int dataType) {
        switch (dataType) {
            case Types.SMALLINT:
                return 1;
            case Types.INTEGER:
                return 2;
            case Types.BIGINT:
                return 3;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return 4;
            case Types.FLOAT:
            case Types.REAL:
                return 5;
            case Types.DOUBLE:
                return 6;
            default:
                return 0;
        }
    }
    
    private boolean isDerivedColumn(@Nullable final ColumnSegmentBoundInfo columnBoundInfo) {
        return null != columnBoundInfo && (columnBoundInfo.getOriginalTable().getValue().isEmpty() || TableSourceType.TEMPORARY_TABLE == columnBoundInfo.getTableSourceType());
    }
    
    private Collection<ProjectionSegment> findOriginProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                       @Nullable final IdentifierValue owner, final IdentifierValue columnName, final ColumnSegmentBoundInfo columnBoundInfo,
                                                                       final boolean traceOrigin) {
        String ownerKey = null == owner ? null : getIdentifierKey(owner);
        boolean shorthandExpanded = isShorthandExpandedColumn(select, columnBoundInfo);
        Collection<ProjectionSegment> result = findSourceProjectionSegments(commonTableExpressions, select, ownerKey, shorthandExpanded, columnName, true, traceOrigin);
        return result.isEmpty() ? findSourceProjectionSegments(commonTableExpressions, select, ownerKey, shorthandExpanded, columnName, false, traceOrigin) : result;
    }
    
    private boolean isShorthandExpandedColumn(final SelectStatement select, final ColumnSegmentBoundInfo columnBoundInfo) {
        return shorthandColumnBoundInfos.computeIfAbsent(select, this::getShorthandColumnBoundInfos).contains(columnBoundInfo);
    }
    
    private Collection<ColumnSegmentBoundInfo> getShorthandColumnBoundInfos(final SelectStatement select) {
        Collection<ColumnSegmentBoundInfo> result = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ProjectionSegment each : select.getProjections().getProjections()) {
            if (each instanceof ShorthandProjectionSegment) {
                addActualColumnBoundInfos((ShorthandProjectionSegment) each, result);
            }
        }
        return result;
    }
    
    private void addActualColumnBoundInfos(final ShorthandProjectionSegment shorthandProjection, final Collection<ColumnSegmentBoundInfo> columnBoundInfos) {
        for (ProjectionSegment each : shorthandProjection.getActualProjectionSegments()) {
            if (each instanceof ColumnProjectionSegment) {
                columnBoundInfos.add(((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo());
            }
        }
    }
    
    private Collection<ProjectionSegment> findSourceProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select, @Nullable final String ownerKey,
                                                                       final boolean shorthandExpanded, final IdentifierValue columnName, final boolean exactMatch, final boolean traceOrigin) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (TableSegment each : getTableSources(select)) {
            if (null == ownerKey || ownerKey.equals(getIdentifierKey(getColumnOwner(each, shorthandExpanded)))) {
                result.addAll(findTableSourceProjectionSegments(commonTableExpressions, each, columnName, exactMatch, traceOrigin));
            }
            if (!result.isEmpty() && (null != ownerKey || !exactMatch)) {
                return result;
            }
        }
        return result;
    }
    
    private IdentifierValue getColumnOwner(final TableSegment tableSource, final boolean shorthandExpanded) {
        return shorthandExpanded && tableSource instanceof SimpleTableSegment ? ((SimpleTableSegment) tableSource).getTableName().getIdentifier() : getTableAliasOrName(tableSource);
    }
    
    private Collection<ProjectionSegment> findTableSourceProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final TableSegment tableSource,
                                                                            final IdentifierValue columnName, final boolean exactMatch, final boolean traceOrigin) {
        if (tableSource instanceof SubqueryTableSegment) {
            SubqueryTableSegment subqueryTable = (SubqueryTableSegment) tableSource;
            SelectStatement subquerySelect = subqueryTable.getSubquery().getSelect();
            return findDerivedColumnProjectionSegments(getCommonTableExpressions(commonTableExpressions, subquerySelect), subquerySelect, subqueryTable.getColumns(), columnName, exactMatch,
                    traceOrigin);
        }
        int commonTableExpressionIndex = findCommonTableExpressionIndex(commonTableExpressions, tableSource);
        if (commonTableExpressionIndex < 0) {
            return Collections.emptyList();
        }
        CommonTableExpressionSegment commonTableExpression = commonTableExpressions.get(commonTableExpressionIndex);
        SelectStatement commonTableExpressionSelect = commonTableExpression.getSubquery().getSelect();
        List<CommonTableExpressionSegment> scopedCommonTableExpressions = getCommonTableExpressions(commonTableExpressions.subList(0, commonTableExpressionIndex), commonTableExpressionSelect);
        return findDerivedColumnProjectionSegments(scopedCommonTableExpressions, commonTableExpressionSelect, commonTableExpression.getColumns(), columnName, exactMatch, traceOrigin);
    }
    
    private Collection<ProjectionSegment> findDerivedColumnProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement derivedSelect,
                                                                              final Collection<ColumnSegment> declaredColumns, final IdentifierValue columnName, final boolean exactMatch,
                                                                              final boolean traceOrigin) {
        SelectStatement select = getAnchorSelect(derivedSelect);
        Optional<ProjectionSegment> result = findProjectionSegment(commonTableExpressions, select, declaredColumns, columnName, exactMatch);
        if (!result.isPresent()) {
            return Collections.emptyList();
        }
        return traceOrigin ? getOriginProjectionSegments(commonTableExpressions, select, result.get()) : Collections.singletonList(result.get());
    }
    
    private Optional<ProjectionSegment> findProjectionSegment(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                              final Collection<ColumnSegment> declaredColumns, final IdentifierValue columnName, final boolean exactMatch) {
        Map<String, ProjectionSegment> labeledSegments =
                labeledProjectionSegments.computeIfAbsent(select, key -> getLabeledProjectionSegments(getExpandedProjectionSegments(commonTableExpressions, key), declaredColumns));
        return exactMatch ? Optional.ofNullable(labeledSegments.get(getIdentifierKey(columnName))) : findCaseInsensitiveProjectionSegment(select, labeledSegments, columnName);
    }
    
    private Map<String, ProjectionSegment> getLabeledProjectionSegments(final Collection<ProjectionSegment> projections, final Collection<ColumnSegment> declaredColumns) {
        Map<String, ProjectionSegment> result = new LinkedHashMap<>(projections.size(), 1F);
        Iterator<ColumnSegment> columns = declaredColumns.iterator();
        for (ProjectionSegment each : projections) {
            if (columns.hasNext()) {
                IdentifierValue declaredColumn = columns.next().getIdentifier();
                declaredColumnLabels.put(each, declaredColumn);
                result.putIfAbsent(getIdentifierKey(declaredColumn), each);
            } else {
                result.putIfAbsent(getColumnLabelKey(each), each);
            }
        }
        return result;
    }
    
    private Optional<ProjectionSegment> findCaseInsensitiveProjectionSegment(final SelectStatement select, final Map<String, ProjectionSegment> labeledSegments, final IdentifierValue columnName) {
        return Optional.ofNullable(caseInsensitiveLabeledProjectionSegments.computeIfAbsent(select, key -> getCaseInsensitiveLabeledProjectionSegments(labeledSegments)).get(columnName.getValue()));
    }
    
    private Map<String, ProjectionSegment> getCaseInsensitiveLabeledProjectionSegments(final Map<String, ProjectionSegment> labeledSegments) {
        Map<String, ProjectionSegment> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Entry<String, ProjectionSegment> entry : labeledSegments.entrySet()) {
            result.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private Collection<ProjectionSegment> getOriginProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                      final ProjectionSegment projection) {
        Collection<ProjectionSegment> joinColumns = mergedJoinColumnSegments.get(projection);
        return null == joinColumns ? traceOriginProjectionSegments(commonTableExpressions, select, projection) : getJoinColumnOriginProjectionSegments(commonTableExpressions, select, joinColumns);
    }
    
    private Collection<ProjectionSegment> traceOriginProjectionSegments(final List<CommonTableExpressionSegment> commonTableExpressions, final SelectStatement select,
                                                                        final ProjectionSegment projection) {
        if (!(projection instanceof ColumnProjectionSegment) || !isDerivedColumn(((ColumnProjectionSegment) projection).getColumn().getColumnBoundInfo())) {
            return Collections.singletonList(projection);
        }
        ColumnSegment column = ((ColumnProjectionSegment) projection).getColumn();
        Collection<ProjectionSegment> result = findOriginProjectionSegments(commonTableExpressions, select, column.getOwner().map(OwnerSegment::getIdentifier).orElse(null), column.getIdentifier(),
                column.getColumnBoundInfo(), true);
        return result.isEmpty() ? Collections.singletonList(projection) : result;
    }
    
    private boolean isTemporaryTableColumn(final Projection projection) {
        if (!(projection instanceof ColumnProjection)) {
            return false;
        }
        ColumnSegmentBoundInfo columnBoundInfo = ((ColumnProjection) projection).getColumnBoundInfo();
        return null != columnBoundInfo && TableSourceType.TEMPORARY_TABLE == columnBoundInfo.getTableSourceType();
    }
    
    @Nullable
    private IdentifierValue getDerivedColumnAlias(final SQLStatementContext sqlStatementContext, final ColumnProjection projection, final Projection describedProjection) {
        if (projection.getAlias().isPresent()) {
            return projection.getAlias().get();
        }
        IdentifierValue columnName = QuoteCharacter.NONE == projection.getName().getQuoteCharacter()
                ? findSourceColumnLabel(sqlStatementContext, projection).orElseGet(projection::getName)
                : projection.getName();
        return QuoteCharacter.NONE == columnName.getQuoteCharacter() && describedProjection instanceof ColumnProjection
                && columnName.getValue().equalsIgnoreCase(((ColumnProjection) describedProjection).getOriginalColumn().getValue()) ? null : columnName;
    }
    
    private Optional<IdentifierValue> findSourceColumnLabel(final SQLStatementContext sqlStatementContext, final ColumnProjection projection) {
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement)) {
            return Optional.empty();
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatementContext.getSqlStatement();
        Collection<ProjectionSegment> sourceProjections = findOriginProjectionSegments(getCommonTableExpressions(Collections.emptyList(), selectStatement), selectStatement,
                projection.getOwner().orElse(null), projection.getName(), projection.getColumnBoundInfo(), false);
        return sourceProjections.isEmpty() ? Optional.empty() : findColumnLabel(sourceProjections.iterator().next());
    }
    
    private Optional<IdentifierValue> findColumnLabel(final ProjectionSegment projection) {
        if (declaredColumnLabels.containsKey(projection)) {
            return Optional.of(declaredColumnLabels.get(projection));
        }
        if (projection instanceof AliasAvailable && ((AliasAvailable) projection).getAlias().isPresent()) {
            return ((AliasAvailable) projection).getAlias();
        }
        return projection instanceof ColumnProjectionSegment ? Optional.of(((ColumnProjectionSegment) projection).getColumn().getIdentifier()) : Optional.empty();
    }
    
    private String getTableName(final SQLStatementContext sqlStatementContext, final ColumnProjection projection) {
        String result = projection.getOriginalTable().getValue();
        return result.isEmpty() || isReturningContextName(result) ? getTableNames(sqlStatementContext).iterator().next() : result;
    }
    
    private boolean isReturningContextName(final String tableName) {
        return null != returningSegment && ("OLD".equalsIgnoreCase(tableName) || "NEW".equalsIgnoreCase(tableName));
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
        Collection<ColumnSegment> affectedColumns = findAffectedColumns(sqlStatementContext);
        int parametersCount = sqlStatementContext.getSqlStatement().getParameterMarkers().size();
        int columnCount = 0;
        for (ColumnSegment columnSegment : affectedColumns) {
            ShardingSphereTable table = getTable(columnSegment.getColumnBoundInfo().getOriginalTable().getValue(), database, schema);
            ShardingSphereColumn column = table.getColumn(columnSegment.getColumnBoundInfo().getOriginalColumn().getValue());
            processColumn(describeColumns, requestedItems, table, column, columnSegment.getOwner().map(OwnerSegment::getIdentifier).orElse(null), columnSegment.getIdentifier(), ++columnCount);
        }
        for (int i = 0; i < parametersCount - affectedColumns.size(); i++) {
            processCustomColumn(null, null, null, 12, describeColumns, requestedItems, ++columnCount);
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
            for (WhereSegment each : getWhereSegments((WhereContextAvailable) sqlStatementContext)) {
                processExpr(each.getExpr(), result);
            }
        }
        return result;
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
    
    private void processExpressionProjection(final ExpressionProjection expr, @Nullable final IdentifierValue columnAlias, final Collection<FirebirdReturnColumnPacket> describeColumns,
                                             final Collection<FirebirdSQLInfoPacketType> requestedItems, final int columnCount) {
        ExpressionSegment exprSegment = expr.getExpressionSegment().getExpr();
        if (exprSegment instanceof FunctionSegment) {
            String functionName = ((FunctionSegment) exprSegment).getFunctionName();
            processCustomColumn(null, functionName, columnAlias, getFunctionType(functionName), describeColumns, requestedItems, columnCount);
        } else if (exprSegment instanceof BinaryOperationExpression) {
            String operationName = getOperationName(((BinaryOperationExpression) exprSegment).getOperator());
            int operationType = getOperationType(((BinaryOperationExpression) exprSegment).getOperator());
            processCustomColumn(null, operationName, columnAlias, operationType, describeColumns, requestedItems, columnCount);
        } else if (exprSegment instanceof LiteralExpressionSegment) {
            Object value = ((LiteralExpressionSegment) exprSegment).getLiterals();
            processLiteralColumn(value, columnAlias, getLiteralType(value), describeColumns, requestedItems, columnCount);
        }
    }
    
    private int getLiteralType(@Nullable final Object value) {
        if (value instanceof String) {
            return Types.VARCHAR;
        }
        if (value instanceof Integer) {
            return Types.INTEGER;
        }
        if (value instanceof Long) {
            return Types.BIGINT;
        }
        return value instanceof Number ? Types.NUMERIC : Types.NULL;
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
