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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SimpleTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType hiveDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Hive");
    
    @SuppressWarnings("resource")
    @Test
    void assertBindTableNotExists() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_not_exists")));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        assertThrows(TableNotFoundException.class, () -> SimpleTableSegmentBinder.bind(
                simpleTableSegment, new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts));
    }
    
    @Test
    void assertBindUnknownOwnerWithSkipMetadataValidate() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(6, 24, new IdentifierValue("zzz_yanyi_100045")));
        simpleTableSegment.setOwner(new OwnerSegment(0, 4, new IdentifierValue("yanyi")));
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, new SQLStatementBinderContext(
                createMetaData(), "foo_db", hintValueContext, SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts);
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("yanyi"));
        assertTrue(((SimpleTableSegmentBinderContext) tableBinderContexts.values().iterator().next()).isSkipColumnBind());
    }
    
    @Test
    void assertBindWithDBLinkContainsDBLink() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_not_exists")));
        simpleTableSegment.setDbLink(new IdentifierValue("foo_db_link"));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegmentBinder.bind(simpleTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts);
        SimpleTableSegmentBinderContext tableSegmentBinderContext = (SimpleTableSegmentBinderContext) tableBinderContexts.values().iterator().next();
        assertTrue(tableSegmentBinderContext.isContainsDBLink());
    }
    
    @Test
    void assertBindTableSampleExpression() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_order")));
        simpleTableSegment.setTableSampled(true);
        simpleTableSegment.setTableSampleExpression(new ColumnSegment(31, 38, new IdentifierValue("order_id")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, new SQLStatementBinderContext(
                createMetaData(), "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts);
        assertTrue(actual.isTableSampled());
        assertTrue(actual.getTableSampleExpression().isPresent());
        assertTrue(actual.getTableSampleExpression().get() instanceof ColumnSegment);
        ColumnSegment actualExpression = (ColumnSegment) actual.getTableSampleExpression().get();
        assertThat(actualExpression.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualExpression.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
    }
    
    @Test
    void assertBindOwnerAsDatabaseForDefaultSchemaDialect() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(20, 27, new IdentifierValue("t_order")));
        simpleTableSegment.setOwner(new OwnerSegment(0, 18, new IdentifierValue("sharding_db")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, new SQLStatementBinderContext(
                createMetaData(), "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(hiveDatabaseType).build()), tableBinderContexts);
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalDatabase().getValue(), is("sharding_db"));
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertBindOwnerAsDatabaseWithLoadedSchema() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(20, 27, new IdentifierValue("t_order")));
        simpleTableSegment.setOwner(new OwnerSegment(0, 18, new IdentifierValue("sharding_db")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, new SQLStatementBinderContext(
                createHiveMetaDataWithLoadedSchema(), "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(hiveDatabaseType).build()), tableBinderContexts);
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalDatabase().getValue(), is("sharding_db"));
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("ds_sharding_db"));
    }
    
    @Test
    void assertBindDefaultSchemaWhenTableNameIsNotUnique() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_order")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, new SQLStatementBinderContext(
                createHiveMetaDataWithDuplicateTables(), "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(hiveDatabaseType).build()), tableBinderContexts);
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("default"));
    }
    
    @Test
    void assertBindSystemTableNameExistingInCurrentSchema() {
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order", "cluster_information"), createSystemSchema());
        SimpleTableSegment actual = bindWithSystemSchema(createTableSegment("cluster_information"), createSelectStatement(), database, mockDialectDatabaseMetaData(mockSchemaOption(false)));
        assertBoundSchema(actual, "foo_db");
    }
    
    @Test
    void assertBindSystemTableNameExistingInCurrentSchemaWithSystemSchemaPreferred() {
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order", "cluster_information"), createSystemSchema());
        SimpleTableSegment actual = bindWithSystemSchema(createTableSegment("cluster_information"), createSelectStatement(), database, mockDialectDatabaseMetaData(mockSystemSchemaOption(true, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @Test
    void assertBindSystemTableName() {
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order"), createSystemSchema());
        SimpleTableSegment actual =
                bindWithSystemSchema(createTableSegment("cluster_information"), createSelectStatement(), database, mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @Test
    void assertBindSystemTableNameWithoutCurrentSchema() {
        ShardingSphereDatabase database = createDatabase(createSystemSchema());
        SimpleTableSegment actual =
                bindWithSystemSchema(createTableSegment("cluster_information"), createSelectStatement(), database, mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @Test
    void assertBindDBLinkTableWithSystemTableName() {
        SimpleTableSegment segment = createTableSegment("cluster_information");
        segment.setDbLink(new IdentifierValue("foo_db_link"));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment actual = bindWithSystemSchema(segment, createSelectStatement(), createDatabase(createCurrentSchema("t_order"), createSystemSchema()),
                mockDialectDatabaseMetaData(mockSystemSchemaOption(false)), tableBinderContexts);
        assertBoundSchema(actual, "foo_db");
        assertTrue(((SimpleTableSegmentBinderContext) tableBinderContexts.values().iterator().next()).isContainsDBLink());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bindDDLTargetWithSystemTableNameArguments")
    void assertBindDDLTargetWithSystemTableName(final String name, final Function<SimpleTableSegment, SQLStatement> sqlStatementFactory) {
        SimpleTableSegment segment = createTableSegment("cluster_information");
        SQLStatement sqlStatement = sqlStatementFactory.apply(segment);
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order"), createSystemSchema());
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(mockSystemSchemaOption(false, false));
        assertThrows(TableNotFoundException.class, () -> bindWithSystemSchema(segment, sqlStatement, database, dialectDatabaseMetaData));
    }
    
    @Test
    void assertBindDDLTargetWithSystemTableNameResolvedToSystemSchema() {
        SimpleTableSegment segment = createTableSegment("cluster_information");
        SQLStatement sqlStatement = new DropTableStatement(databaseType, Collections.singleton(segment), false, false);
        SimpleTableSegment actual =
                bindWithSystemSchema(segment, sqlStatement, createDatabase(createCurrentSchema("t_order"), createSystemSchema()), mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bindNonTargetSystemTableNameArguments")
    void assertBindNonTargetSystemTableName(final String name, final SQLStatement sqlStatement) {
        SimpleTableSegment actual = bindWithSystemSchema(createTableSegment("cluster_information"), sqlStatement,
                createDatabase(createCurrentSchema("t_order"), createSystemSchema()), mockDialectDatabaseMetaData(mockSystemSchemaOption(false, false)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bindCreateTargetWithSystemTableNameArguments")
    void assertBindCreateTargetWithSystemTableName(final String name, final Function<SimpleTableSegment, SQLStatement> sqlStatementFactory) {
        SimpleTableSegment segment = createTableSegment("cluster_information");
        SimpleTableSegment actual = bindWithSystemSchema(segment, sqlStatementFactory.apply(segment),
                createDatabase(createCurrentSchema("t_order"), createSystemSchema()), mockDialectDatabaseMetaData(mockSystemSchemaOption(false)));
        assertBoundSchema(actual, "foo_db");
    }
    
    @Test
    void assertBindNonSystemTableNameWithSystemSchemaPreferred() {
        SimpleTableSegment actual = bindWithSystemSchema(createTableSegment("t_order"), createSelectStatement(),
                createDatabase(createCurrentSchema("t_order"), createSystemSchema()), mockDialectDatabaseMetaData(mockSystemSchemaOption(true, true)));
        assertBoundSchema(actual, "foo_db");
    }
    
    @Test
    void assertBindQuotedTableNameWithoutSystemSchema() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("Foo_Tbl", QuoteCharacter.QUOTE)));
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order"));
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(mockSystemSchemaOption(true, true));
        assertThrows(TableNotFoundException.class, () -> bindWithSystemSchema(segment, createSelectStatement(), database, dialectDatabaseMetaData));
    }
    
    @Test
    void assertBindQuotedSystemTableName() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 20, new IdentifierValue("cluster_information", QuoteCharacter.QUOTE)));
        SimpleTableSegment actual =
                bindWithSystemSchema(segment, createSelectStatement(), createDatabase(createCurrentSchema("t_order"), createSystemSchema()),
                        mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @Test
    void assertBindQuotedUpperCaseSystemTableName() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 20, new IdentifierValue("CLUSTER_INFORMATION", QuoteCharacter.QUOTE)));
        ShardingSphereDatabase database = createDatabase(createCurrentSchema("t_order"), createSystemSchema());
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true));
        assertThrows(TableNotFoundException.class, () -> bindWithSystemSchema(segment, createSelectStatement(), database, dialectDatabaseMetaData));
    }
    
    @Test
    void assertBindQuotedUpperCaseSystemTableNameWithNormalizedQuotedLookup() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 20, new IdentifierValue("CLUSTER_INFORMATION", QuoteCharacter.QUOTE)));
        ShardingSphereDatabase database = spy(createDatabase(createCurrentSchema("t_order"), createSystemSchema()));
        when(database.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet()));
        SimpleTableSegment actual = bindWithSystemSchema(segment, createSelectStatement(), database, mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    @Test
    void assertBindQuotedTableNameAssembledInSystemSchemaOnly() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("Foo_Tbl", QuoteCharacter.QUOTE)));
        SimpleTableSegment actual =
                bindWithSystemSchema(segment, createSelectStatement(), createDatabase(createCurrentSchema("t_order"), createSystemSchema()),
                        mockDialectDatabaseMetaData(mockSystemSchemaOption(false, true)));
        assertBoundSchema(actual, "shardingsphere");
    }
    
    private static Stream<Arguments> bindDDLTargetWithSystemTableNameArguments() {
        DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        return Stream.of(
                Arguments.of("drop table", (Function<SimpleTableSegment, SQLStatement>) each -> new DropTableStatement(fixtureDatabaseType, Collections.singleton(each), false, false)),
                Arguments.of("drop view", (Function<SimpleTableSegment, SQLStatement>) each -> new DropViewStatement(fixtureDatabaseType, Collections.singleton(each), false)),
                Arguments.of("truncate table", (Function<SimpleTableSegment, SQLStatement>) each -> new TruncateStatement(fixtureDatabaseType, Collections.singleton(each), Collections.emptyList())),
                Arguments.of("alter table", (Function<SimpleTableSegment, SQLStatement>) each -> AlterTableStatement.builder().databaseType(fixtureDatabaseType).table(each).build()),
                Arguments.of("alter view", (Function<SimpleTableSegment, SQLStatement>) each -> createAlterViewStatement(fixtureDatabaseType, each)),
                Arguments.of("create index", (Function<SimpleTableSegment, SQLStatement>) each -> CreateIndexStatement.builder().databaseType(fixtureDatabaseType).table(each).build()));
    }
    
    private static Stream<Arguments> bindNonTargetSystemTableNameArguments() {
        DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        SimpleTableSegment targetSegment = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_order")));
        return Stream.of(
                Arguments.of("select", SelectStatement.builder().databaseType(fixtureDatabaseType).build()),
                Arguments.of("drop table", new DropTableStatement(fixtureDatabaseType, Collections.singleton(targetSegment), false, false)),
                Arguments.of("alter table", AlterTableStatement.builder().databaseType(fixtureDatabaseType).table(targetSegment).build()),
                Arguments.of("alter view", createAlterViewStatement(fixtureDatabaseType, targetSegment)),
                Arguments.of("create index", CreateIndexStatement.builder().databaseType(fixtureDatabaseType)
                        .index(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("idx_foo")))).table(targetSegment).build()));
    }
    
    private static Stream<Arguments> bindCreateTargetWithSystemTableNameArguments() {
        DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        SimpleTableSegment sourceSegment = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_order")));
        return Stream.of(
                Arguments.of("create table", (Function<SimpleTableSegment, SQLStatement>) each -> CreateTableStatement.builder().databaseType(fixtureDatabaseType).table(each).build()),
                Arguments.of("create view", (Function<SimpleTableSegment, SQLStatement>) each -> createCreateViewStatement(fixtureDatabaseType, each)),
                Arguments.of("alter table rename", (Function<SimpleTableSegment, SQLStatement>) each -> AlterTableStatement.builder()
                        .databaseType(fixtureDatabaseType).table(sourceSegment).renameTable(each).build()),
                Arguments.of("alter view rename", (Function<SimpleTableSegment, SQLStatement>) each -> createAlterViewRenameStatement(fixtureDatabaseType, sourceSegment, each)),
                Arguments.of("rename table", (Function<SimpleTableSegment, SQLStatement>) each -> createRenameTableStatement(fixtureDatabaseType, sourceSegment, each)));
    }
    
    private static CreateViewStatement createCreateViewStatement(final DatabaseType databaseType, final SimpleTableSegment view) {
        CreateViewStatement result = new CreateViewStatement(databaseType);
        result.setView(view);
        return result;
    }
    
    private static AlterViewStatement createAlterViewRenameStatement(final DatabaseType databaseType, final SimpleTableSegment view, final SimpleTableSegment renameView) {
        AlterViewStatement result = createAlterViewStatement(databaseType, view);
        result.setRenameView(renameView);
        return result;
    }
    
    private static RenameTableStatement createRenameTableStatement(final DatabaseType databaseType, final SimpleTableSegment table, final SimpleTableSegment renameTable) {
        RenameTableDefinitionSegment renameTableDefinition = new RenameTableDefinitionSegment(0, 0);
        renameTableDefinition.setTable(table);
        renameTableDefinition.setRenameTable(renameTable);
        return new RenameTableStatement(databaseType, Collections.singleton(renameTableDefinition));
    }
    
    private static AlterViewStatement createAlterViewStatement(final DatabaseType databaseType, final SimpleTableSegment view) {
        AlterViewStatement result = new AlterViewStatement(databaseType);
        result.setView(view);
        return result;
    }
    
    private SimpleTableSegment createTableSegment(final String tableName) {
        return new SimpleTableSegment(new TableNameSegment(0, tableName.length() - 1, new IdentifierValue(tableName)));
    }
    
    private SelectStatement createSelectStatement() {
        return SelectStatement.builder().databaseType(databaseType).build();
    }
    
    private ShardingSphereDatabase createDatabase(final ShardingSphereSchema... schemas) {
        return new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), Arrays.asList(schemas), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereSchema createCurrentSchema(final String... tableNames) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        for (String each : tableNames) {
            tables.add(createTable(each));
        }
        return new ShardingSphereSchema("foo_db", databaseType, tables, Collections.emptyList());
    }
    
    private ShardingSphereSchema createSystemSchema() {
        return new ShardingSphereSchema("shardingsphere", databaseType, Arrays.asList(createTable("cluster_information"), createTable("Foo_Tbl")), Collections.emptyList());
    }
    
    private ShardingSphereTable createTable(final String tableName) {
        return new ShardingSphereTable(tableName, Collections.singleton(new ShardingSphereColumn("foo_col", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final DialectSchemaOption schemaOption) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getSchemaOption()).thenReturn(schemaOption);
        return result;
    }
    
    private DialectSchemaOption mockSchemaOption(final boolean systemSchemaPreferred) {
        DialectSchemaOption result = mock(DialectSchemaOption.class);
        when(result.getDefaultSchema()).thenReturn(Optional.empty());
        when(result.isSystemSchemaPreferredOverCurrentSchema()).thenReturn(systemSchemaPreferred);
        return result;
    }
    
    private DialectSchemaOption mockSystemSchemaOption(final boolean systemSchemaPreferred) {
        DialectSchemaOption result = mockSchemaOption(systemSchemaPreferred);
        when(result.getDefaultSystemSchema()).thenReturn(Optional.of("shardingsphere"));
        return result;
    }
    
    private DialectSchemaOption mockSystemSchemaOption(final boolean systemSchemaPreferred, final boolean ddlTargetResolvedToSystemSchema) {
        DialectSchemaOption result = mockSystemSchemaOption(systemSchemaPreferred);
        when(result.isDDLTargetResolvedToSystemSchema()).thenReturn(ddlTargetResolvedToSystemSchema);
        return result;
    }
    
    private SimpleTableSegment bindWithSystemSchema(final SimpleTableSegment segment, final SQLStatement sqlStatement, final ShardingSphereDatabase database,
                                                    final DialectDatabaseMetaData dialectDatabaseMetaData) {
        return bindWithSystemSchema(segment, sqlStatement, database, dialectDatabaseMetaData, LinkedHashMultimap.create());
    }
    
    /**
     * Bind simple table segment with mocked dialect database metadata.
     *
     * <p>{@code mockConstruction} is used directly instead of {@code AutoMockExtension}: each test needs a per-instance initializer returning its own dialect database metadata,
     * while {@code ConstructionMockSettings} mocks construction for the whole test class without an initializer, which would return no dialect database metadata
     * and break the other tests in this class that rely on real {@link DatabaseTypeRegistry} instances.</p>
     *
     * @param segment simple table segment
     * @param sqlStatement SQL statement
     * @param database database
     * @param dialectDatabaseMetaData dialect database metadata
     * @param tableBinderContexts table binder contexts
     * @return bound simple table segment
     */
    private SimpleTableSegment bindWithSystemSchema(final SimpleTableSegment segment, final SQLStatement sqlStatement, final ShardingSphereDatabase database,
                                                    final DialectDatabaseMetaData dialectDatabaseMetaData, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), props);
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(
                        DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData))) {
            return SimpleTableSegmentBinder.bind(segment, new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), sqlStatement), tableBinderContexts);
        }
    }
    
    private void assertBoundSchema(final SimpleTableSegment actual, final String expectedSchemaName) {
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is(expectedSchemaName));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue shardingDatabase = new IdentifierValue("sharding_db");
        IdentifierValue publicSchema = new IdentifierValue("public");
        IdentifierValue testSchema = new IdentifierValue("test");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        IdentifierValue pgDatabase = new IdentifierValue("pg_database");
        when(schema.getName()).thenReturn("sharding_db");
        when(schema.containsTable(tOrder)).thenReturn(true);
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(pgDatabase).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("datname", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("datdba", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("sharding_db")).thenReturn(schema);
        when(result.getDatabase("foo_db").getSchema("public")).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("test")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase)).thenReturn(schema);
        when(result.getDatabase(shardingDatabase).getSchema(shardingDatabase)).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(publicSchema)).thenReturn(schema);
        when(result.getDatabase(shardingDatabase).getSchema(testSchema)).thenReturn(schema);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(fooDatabase)).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tOrder)).thenReturn(true);
        when(result.containsDatabase(shardingDatabase)).thenReturn(true);
        when(result.getDatabase("sharding_db").getDefaultSchemaName()).thenReturn("sharding_db");
        when(result.getDatabase(shardingDatabase).getDefaultSchemaName()).thenReturn("sharding_db");
        when(result.getDatabase("sharding_db").containsSchema("sharding_db")).thenReturn(true);
        when(result.getDatabase(shardingDatabase).containsSchema(shardingDatabase)).thenReturn(true);
        when(result.getDatabase("sharding_db").getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getDatabase(shardingDatabase).getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getDatabase(shardingDatabase).getSchema(shardingDatabase).containsTable(tOrder)).thenReturn(true);
        return result;
    }
    
    private ShardingSphereMetaData createHiveMetaDataWithLoadedSchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema systemSchema = mock(ShardingSphereSchema.class);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue shardingDatabase = new IdentifierValue("sharding_db");
        IdentifierValue loadedSchema = new IdentifierValue("ds_sharding_db");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        when(schema.getName()).thenReturn("ds_sharding_db");
        when(systemSchema.getName()).thenReturn("shardingsphere");
        when(schema.containsTable(tOrder)).thenReturn(true);
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.containsDatabase(shardingDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema(shardingDatabase)).thenReturn(false);
        when(result.getDatabase(fooDatabase).containsSchema(shardingDatabase)).thenReturn(false);
        when(result.getDatabase("sharding_db").getProtocolType()).thenReturn(hiveDatabaseType);
        when(result.getDatabase(shardingDatabase).getProtocolType()).thenReturn(hiveDatabaseType);
        when(result.getDatabase("sharding_db").getAllSchemas()).thenReturn(Arrays.asList(schema, systemSchema));
        when(result.getDatabase(shardingDatabase).getAllSchemas()).thenReturn(Arrays.asList(schema, systemSchema));
        when(result.getDatabase("sharding_db").containsSchema(loadedSchema)).thenReturn(true);
        when(result.getDatabase(shardingDatabase).containsSchema(loadedSchema)).thenReturn(true);
        when(result.getDatabase("sharding_db").getSchema(loadedSchema)).thenReturn(schema);
        when(result.getDatabase(shardingDatabase).getSchema(loadedSchema)).thenReturn(schema);
        return result;
    }
    
    private ShardingSphereMetaData createHiveMetaDataWithDuplicateTables() {
        IdentifierValue databaseName = new IdentifierValue("foo_db");
        IdentifierValue defaultSchemaName = new IdentifierValue("default");
        IdentifierValue analyticsSchemaName = new IdentifierValue("analytics");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        ShardingSphereSchema defaultSchema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema analyticsSchema = mock(ShardingSphereSchema.class);
        when(defaultSchema.getName()).thenReturn(defaultSchemaName.getValue());
        when(defaultSchema.containsTable(tOrder)).thenReturn(true);
        when(defaultSchema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(analyticsSchema.getName()).thenReturn(analyticsSchemaName.getValue());
        when(analyticsSchema.containsTable(tOrder)).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getDefaultSchemaName()).thenReturn(defaultSchemaName.getValue());
        when(database.containsSchema(defaultSchemaName)).thenReturn(true);
        when(database.getSchema(defaultSchemaName)).thenReturn(defaultSchema);
        when(database.getAllSchemas()).thenReturn(Arrays.asList(defaultSchema, analyticsSchema));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase(databaseName)).thenReturn(true);
        when(result.getDatabase(databaseName)).thenReturn(database);
        when(result.getDatabase(databaseName.getValue())).thenReturn(database);
        return result;
    }
}
