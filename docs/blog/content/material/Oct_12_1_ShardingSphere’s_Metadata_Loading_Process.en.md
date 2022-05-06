+++
title = "ShardingSphere’s Metadata Loading Process"
weight = 17
chapter = true
+++

**1. Overview**

  Metadata is the data that constitutes the data. In database terms, any data that describes the database is metadata. Column names, database names, usernames, table names, etc. and data customization library tables that store information about database objects are metadata. ShardingSphere core functions such as data sharding, encryption and decryption are all based on the database metadata.

  
  This shows that metadata is the core of the ShardingSphere system and is also the core data of every data storage related middleware or component. With the injection of metadata, it is equivalent to having a nerve center for the whole system, which can be combined with metadata to perform personalized operations on libraries, tables, and columns, such as data sharding, data encryption, SQL rewriting, etc.

  For the ShardingSphere metadata loading process, it is first necessary to clarify the type and hierarchy of metadata in ShardingSphere. The metadata in ShardingSphere is mainly based around the `ShardingSphereMetaData`, the core of which is the `ShardingSphereSchema`, which is the metadata of the database and is also the top-level object of the data source metadata. The structure of the database metadata in ShardingSphere is shown as below, for each layer, the upper layer data comes from the assembly of the lower layer data, so we use the following bottom-up hierarchy to analyze one by one.

![](https://shardingsphere.apache.org/blog/img/Blog_17_img_1_ShardingSphere_Database_Metadata_Structure_Diagram_en.png)


**2. ColumMetaData and IndexMetaData**

`ColumMetaData` and `IndexMetaData` are the basic elements that make up `TableMetaData`. In the following, we will analyze the structure of the two metadata types and the loading process separately. `ColumMetaData` has the following main structure:  

 ~~~
 public final class ColumnMetaData {
    // 
    private final String name;
    // 
    private final int dataType;
    // 
    private final boolean primaryKey;
    // 
    private final boolean generated;
    //
    private final boolean caseSensitive;
}
 ~~~
 
The loading process is mainly encapsulated in the `org.apache.shardingsphere.infra.metadata.schema.builder.loader.ColumnMetaDataLoader#load` method, and its main process is to load the metadata of all the columns under a table name by getting the metadata matching the table name through the database link. The core code is as follows:

 ~~~
/**
 * Load column meta data list.
 *
 * @param connection connection
 * @param tableNamePattern table name pattern
 * @param databaseType database type
 * @return column meta data list
 * @throws SQLException SQL exception
 */
public static Collection<ColumnMetaData> load(final Connection connection, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
    Collection<ColumnMetaData> result = new LinkedList<>();
    Collection<String> primaryKeys = loadPrimaryKeys(connection, tableNamePattern);
    List<String> columnNames = new ArrayList<>();
    List<Integer> columnTypes = new ArrayList<>();
    List<String> columnTypeNames = new ArrayList<>();
    List<Boolean> isPrimaryKeys = new ArrayList<>();
    List<Boolean> isCaseSensitives = new ArrayList<>();
    try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNamePattern, "%")) {
        while (resultSet.next()) {
            String tableName = resultSet.getString(TABLE_NAME);
            if (Objects.equals(tableNamePattern, tableName)) {
                String columnName = resultSet.getString(COLUMN_NAME);
                columnTypes.add(resultSet.getInt(DATA_TYPE));
                columnTypeNames.add(resultSet.getString(TYPE_NAME));
                isPrimaryKeys.add(primaryKeys.contains(columnName));
                columnNames.add(columnName);
            }
        }
    }
    try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(generateEmptyResultSQL(tableNamePattern, databaseType))) {
        for (int i = 0; i < columnNames.size(); i++) {
            isCaseSensitives.add(resultSet.getMetaData().isCaseSensitive(resultSet.findColumn(columnNames.get(i))));
            result.add(new ColumnMetaData(columnNames.get(i), columnTypes.get(i), isPrimaryKeys.get(i),
                    resultSet.getMetaData().isAutoIncrement(i + 1), isCaseSensitives.get(i)));
        }
    }
    return result;
}
 ~~~
 
`IndexMetaData`is the name of the index in the table, so there are no complex structural properties, just a name. Instead of going into detail, we rather focus on the loading process. Its loading process is similar with that of column, and the main process is in the `org.apache.shardingsphere.infra.metadata.schema.builder.loader.IndexMetaDataLoader#load` method. The basic process is also through the database link to obtain core `IndexMetaData` in the `IndexInfo` organization of the relevant database and table metadata, and the implementation code is as follows:

~~~
public static Collection<IndexMetaData> load(final Connection connection, final String table) throws SQLException {
    Collection<IndexMetaData> result = new HashSet<>();
    try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getSchema(), table, false, false)) {
        while (resultSet.next()) {
            String indexName = resultSet.getString(INDEX_NAME);
            if (null != indexName) {
                result.add(new IndexMetaData(indexName));
            }
        }
    } catch (final SQLException ex) {
        if (ORACLE_VIEW_NOT_APPROPRIATE_VENDOR_CODE != ex.getErrorCode()) {
            throw ex;
        }
    }
    return result;
}
~~~

**3. TableMetaData**  

This class is the basic element of `ShardingSphereMetaData` and has the following structure:

~~~
public final class TableMetaData {
    // Table Name
    private final String name;
    // Column Metadata
    private final Map<String, ColumnMetaData> columns;
    // Index Metadata
    private final Map<String, IndexMetaData> indexes;
    //Omit Method
}
~~~

From the above structure we can see that `TableMetaData` is assembled from `ColumnMetaData` and `IndexMetaData`, so the loading process of `TableMetaData` can be understood as an intermediate layer, and the specific implementation still depends on `ColumnMetaDataLoader` and `IndexMetaDataLoader` to get the table name and related links for data loading. So the relatively simple `TableMetaData` loading process is mainly in the `org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader#load` method, and its core loading process is as follows:  

~~~
public static Optional<TableMetaData> load(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
    // Get the Link
    try (MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
        // Format fuzzy matching field of the table name, according to database type
        String formattedTableNamePattern = databaseType.formatTableNamePattern(tableNamePattern);
        // LoadColumnMetaData and IndexMetaData to assemble TableMetaData
        return isTableExist(connectionAdapter, formattedTableNamePattern)
                ? Optional.of(new TableMetaData(tableNamePattern, ColumnMetaDataLoader.load(
                        connectionAdapter, formattedTableNamePattern, databaseType), IndexMetaDataLoader.load(connectionAdapter, formattedTableNamePattern)))
                : Optional.empty();
    }
}
~~~

**4. SchemaMetaData**  

According to the analysis of the two lower layers, it’s clear that this layer is the outermost layer of metadata exposure, and the outermost layer is structured as a `ShardingSphereSchema` with the following main structure:  

~~~
/**
 * ShardingSphere schema.
 */
@Getter
public final class ShardingSphereSchema {

    private final Map<String, TableMetaData> tables;

    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public ShardingSphereSchema() {
        tables = new ConcurrentHashMap<>();
    }

    public ShardingSphereSchema(final Map<String, TableMetaData> tables) {
        this.tables = new ConcurrentHashMap<>(tables.size(), 1);
        tables.forEach((key, value) -> this.tables.put(key.toLowerCase(), value));
    }
~~~

In line with the schema concept, it contains several tables. The attribute of `ShardingSphereSchema` is a map structure, the key is `tableName`, and the value is the metadata of the table corresponding to the `tableName`.  

Initialization is done primarily through the constructor. So again, the focus is on the table metadata loading, let’s follow up from the entry.  

The core entry point for the entire metadata load is in `org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder#build`. In the build, we assemble and load the corresponding metadata through configuration rules. The core code is as follows:  

~~~
/**
 * Build meta data contexts.
 * 
 * @exception SQLException SQL exception
 * @return meta data contexts
 */
public StandardMetaDataContexts build() throws SQLException {
    Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
    Map<String, ShardingSphereMetaData> actualMetaDataMap = new HashMap<>(schemaRuleConfigs.size(), 1);
    for (String each : schemaRuleConfigs.keySet()) {
        Map<String, DataSource> dataSourceMap = dataSources.get(each);
        Collection<RuleConfiguration> ruleConfigs = schemaRuleConfigs.get(each);
        DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
        // Obtain configuration rules
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.buildSchemaRules(each, ruleConfigs, databaseType, dataSourceMap);
        // Load actualTableMetaData and logicTableMetaData
        Map<TableMetaData, TableMetaData> tableMetaDatas = SchemaBuilder.build(new SchemaBuilderMaterials(databaseType, dataSourceMap, rules, props));
        // Assemble rule metadata
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, rules);
        // Assemble data source metadata
        ShardingSphereResource resource = buildResource(databaseType, dataSourceMap);
        // Assemble database metadata
        ShardingSphereSchema actualSchema = new ShardingSphereSchema(tableMetaDatas.keySet().stream().filter(Objects::nonNull).collect(Collectors.toMap(TableMetaData::getName, v -> v)));
        actualMetaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, actualSchema));
        metaDataMap.put(each, new ShardingSphereMetaData(each, resource, ruleMetaData, buildSchema(tableMetaDatas)));
    }
    // 
    OptimizeContextFactory optimizeContextFactory = new OptimizeContextFactory(actualMetaDataMap);
    return new StandardMetaDataContexts(metaDataMap, buildGlobalSchemaMetaData(metaDataMap), executorEngine, props, optimizeContextFactory);
}
~~~  

The above code shows that in the build method, the basic database data such as database type, database connection pool, etc. are loaded based on the configured schemarule, through which the assembly of the `ShardingSphereResource` is completed; the assembly of the `ShardingSphereRuleMetaData` such as configuration rules, encryption rules, authentication rules, etc. are assembled; the necessary database metadata in the `ShardingSphereSchema` are loaded. Trace to find the method for loading table metadata, namely `org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilder#build`, in which the `actualTableMetaData` and `logicTableMetaData` are loaded respectively.  

Then what is an actualTable and what is a `logicTable`? Simply put for `t_order_1`, `t_order_2` is considered a node of `t_order`, so in the concept of analysis, `t_order` is `logicTable`, while `t_order_1` and `t_order_2` is `actualTable`. With these two concepts clearly defined, we then look at the build method together, mainly divided into the following two steps.  

**i) actualTableMetaData loading**  

`ActualTableMetaData` is the basic table of system sharding. In the 5.0 beta version, we adopt the method of database dialect to use SQL to query and load metadata, so the basic process is to query and load database metadata through SQL first. If no database dialect loader is found, the JDBC driver connection is used to obtain it, and then the metadata of the configuration table is loaded in combination with the table name configured in ShardingSphereRule. The core code is shown below.  

~~~ 
private static Map<String, TableMetaData> buildActualTableMetaDataMap(final SchemaBuilderMaterials materials) throws SQLException {
    Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
    // Database SQL Loading Metadata
    appendRemainTables(materials, result);
    for (ShardingSphereRule rule : materials.getRules()) {
        if (rule instanceof TableContainedRule) {
            for (String table : ((TableContainedRule) rule).getTables()) {
                if (!result.containsKey(table)) {
                    TableMetaDataBuilder.load(table, materials).map(optional -> result.put(table, optional));
                }
            }
        }
    }
    return result;
}
~~~ 

**ii) logicTableMetaData loading**

From the above concept we can see that `logicTable` is an actual logical node assembled from an `actualTable` based on different rules, which may be a sharded node or a cryptographic node or something else. Therefore, the `logicTableMetaData` is based on the `actualTableMetaData`, combined with specific configuration rules such as library and table rules and other associated nodes.
In terms of the specific process, it first obtains the table name of the configuration rule, then determines whether the `actualTableMetaData` has been loaded, and generates the metadata of the relevant logical node by combining the configuration rule with the `TableMetaDataBuilder#decorate` method. The core code flow is shown below:  

~~~ 
private static Map<String, TableMetaData> buildLogicTableMetaDataMap(final SchemaBuilderMaterials materials, final Map<String, TableMetaData> tables) throws SQLException {
    Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
    for (ShardingSphereRule rule : materials.getRules()) {
        if (rule instanceof TableContainedRule) {
            for (String table : ((TableContainedRule) rule).getTables()) {
                if (tables.containsKey(table)) {
                    TableMetaData metaData = TableMetaDataBuilder.decorate(table, tables.get(table), materials.getRules());
                    result.put(table, metaData);
                }
            }
        }
    }
    return result;
}
~~~   

At this point, the core metadata is loaded and encapsulated into a Map for return, for use in each requirement scenario.

    
**5. Metadata Loading Optimization Analysis**  

Although metadata is the essential core of our system, data loading during system startup will inevitably increase system load and lower system startup efficiency. Therefore, we need to optimize the loading process. At present, we are exploring the following two ways:

**A. Replace Native JDBC Driver Connections with SQL Queries**

Prior to the 5.0 beta version, the approach used was to load via the native JDBC driver. In 5.0 beta, we have gradually adopted a multi-threaded approach to metadata loading using a database dialect, via SQL queries. The speed of loading system data has been further improved. A detailed dialect loader can be found in the related implementation of `org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader`.

**B. Reduce Metadata Load Times**  

For the loading of resources common to the system, we follow the concept of “one-time loading for multiple uses”. Of course, we must consider space and time in this process. As a result, we are constantly optimizing to reduce duplicate loading of metadata to enhance overall system efficiency.

    
**About The Author**

![](https://shardingsphere.apache.org/blog/img/Blog_17_img_2_Tang_Guocheng_Photo.png)

Tang Guocheng, a software engineer at Xiaomi, is mainly responsible for the development of the MIUI browser server side. He is a technology and Open-Source enthusiast, loves to explore and is keen on researching and learning about Open-Source middleware solutions. He is a proud member of the ShardingSphere community and is working hard to improve his skills with the support of the community, and to contribute to the development of the ShardingSphere community.

**ShardingSphere Community:**

 ShardingSphere Github: [https://github.com/apache/shardingsphere]() 
 
 ShardingSphere Twitter: [https://twitter.com/ShardingSphere]()
 
 ShardingSphere Slack Channel:[ShardingSphere Slack Channel:]()
