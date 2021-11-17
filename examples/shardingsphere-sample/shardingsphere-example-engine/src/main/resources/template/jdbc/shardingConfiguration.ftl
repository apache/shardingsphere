<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
    
    /**
     * Create a DataSource object, which is an object rewritten by ShardingSphere itself 
     * and contains various rules for rewriting the original data storage. When in use, you only need to use this object.
     * @return
     * @throws SQLException
    */
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createModeConfiguration(), createDataSourceMap(), Collections.singleton(createShardingRuleConfiguration()), new Properties());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "${r"demo_ds_${user_id % 2}"}");
        result.getShardingAlgorithms() .put("inline", new ShardingSphereAlgorithmConfiguration("INLINE", props));
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", getProperties()));
        return result;
    }
    
    private static ModeConfiguration createModeConfiguration() {
        return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("File", new Properties()), true);
    }
    
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private static ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order_item");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("demo_ds_0", createDataSource("demo_ds_0"));
        result.put("demo_ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("worker-id", "123");
        return result;
    }
