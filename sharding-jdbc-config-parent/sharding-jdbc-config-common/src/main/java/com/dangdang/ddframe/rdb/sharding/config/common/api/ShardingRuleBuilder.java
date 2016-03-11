/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.AbstractShardingRuleConfigFileDelegate;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Getter;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.LoggerFactory;

/**
 * 分片规则构建器.
 * 
 * @author gaohongtao
 */
public class ShardingRuleBuilder {
    
    private Map<String, DataSource> dataSourceMap;
    
    @Getter
    private AbstractShardingRuleConfigFileDelegate ruleConfigDelegate;
    
    private DataSourceRule dataSourceRule;
    
    /**
     * 设置数据源映射.
     * 
     * @param dataSourceMap 数据源映射
     * @return 构建器对象
     */
    public ShardingRuleBuilder setDataSourceMap(final Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
        return this;
    }
    
    /**
     * 解析规则配置对象中的规则配置.
     * 
     * @param config 规则配置对象
     * @return 构建器对象
     */
    public ShardingRuleBuilder parse(final ShardingRuleConfig config) {
        return parse("default", config);
    }
    
    /**
     * 解析规则配置对象中的规则配置.
     * 
     * @param logRoot 规则名称
     * @param config 规则配置对象
     * @return 构建器对象
     */
    public ShardingRuleBuilder parse(final String logRoot, final ShardingRuleConfig config) {
        if (null == dataSourceMap && null != config.getDataSource()) {
            dataSourceMap = config.getDataSource();
        }
        Binding binding = new Binding();
        binding.setProperty("shardingRuleConfig", config);
        GroovyShell shell = new GroovyShell(binding);
        URL templateUrl = this.getClass().getClassLoader().getResource("shardingJDBC_config_template.groovy");
        Preconditions.checkNotNull(templateUrl);
        Object result;
        try {
            result = shell.run(templateUrl.toURI(), new String[]{});
        } catch (final IOException | URISyntaxException ignored) {
            throw new UnsupportedOperationException("can not load template");
        }
        ruleConfigDelegate = (AbstractShardingRuleConfigFileDelegate) getShell(logRoot).parse(result.toString());
        initDelegate();
        return this;
    }
    
    private GroovyShell getShell(final String logRoot) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(AbstractShardingRuleConfigFileDelegate.class.getName());
        Binding binding = new Binding();
        binding.setVariable("log", LoggerFactory.getLogger(Joiner.on(".").join("com.dangdang.ddframe.rdb.sharding.configFile", logRoot)));
        return new GroovyShell(binding, compilerConfiguration);
    }
    
    private void initDelegate() {
        if (null != dataSourceMap) {
            dataSourceRule = new DataSourceRule(dataSourceMap);
            ruleConfigDelegate.setDataSourceRule(dataSourceRule);
        }
    }
    
    /**
     * 构建分片规则.
     * 
     * @return 分片规则对象
     */
    public ShardingRule build() {
        ruleConfigDelegate.run();
        Preconditions.checkNotNull(ruleConfigDelegate.getTableRules(), "Sharding JDBC: Config file must contains table config");
        
        return new ShardingRule(ruleConfigDelegate.getDataSourceRule(), ruleConfigDelegate.getTableRules(),
                null == ruleConfigDelegate.getBindingTableRules() ? Collections.<BindingTableRule>emptyList() : ruleConfigDelegate.getBindingTableRules(),
                null == ruleConfigDelegate.getDefaultDatabaseShardingStrategy() ? new DatabaseShardingStrategy(Collections.<String>emptyList(),
                        new NoneDatabaseShardingAlgorithm()) : ruleConfigDelegate.getDefaultDatabaseShardingStrategy(),
                null == ruleConfigDelegate.getDefaultTableShardingStrategy() ? new TableShardingStrategy(Collections.<String>emptyList(),
                        new NoneTableShardingAlgorithm()) : ruleConfigDelegate.getDefaultTableShardingStrategy());
    }
    
}
