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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Result set for shadow algorithm.
 */
public final class ShadowAlgorithmQueryResultSet implements DistSQLResultSet {
    
    private static final String SHADOW_ALGORITHM_NAME = "shadow_algorithm_name";
    
    private static final String TYPE = "type";
    
    private static final String PROPERTIES = "properties";
    
    private static final String DEFAULT = "is_default";
    
    private Iterator<Entry<String, ShardingSphereAlgorithmConfiguration>> data = Collections.emptyIterator();
    
    private String defaultAlgorithm;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShadowRuleConfiguration> rule = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShadowRuleConfiguration).map(each -> (ShadowRuleConfiguration) each).findAny();
        rule.ifPresent(configuration -> {
            data = configuration.getShadowAlgorithms().entrySet().iterator();
            defaultAlgorithm = configuration.getDefaultShadowAlgorithmName();
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(SHADOW_ALGORITHM_NAME, TYPE, PROPERTIES, DEFAULT);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(data.next());
    }
    
    private Collection<Object> buildTableRowData(final Entry<String, ShardingSphereAlgorithmConfiguration> data) {
        return Arrays.asList(data.getKey(), data.getValue().getType(), convertToString(data.getValue().getProps()), data.getKey().equals(defaultAlgorithm));
    }
    
    private String convertToString(final Properties props) {
        return Objects.nonNull(props) ? PropertiesConverter.convert(props) : "";
    }
    
    @Override
    public String getType() {
        return ShowShadowAlgorithmsStatement.class.getCanonicalName();
    }
}
