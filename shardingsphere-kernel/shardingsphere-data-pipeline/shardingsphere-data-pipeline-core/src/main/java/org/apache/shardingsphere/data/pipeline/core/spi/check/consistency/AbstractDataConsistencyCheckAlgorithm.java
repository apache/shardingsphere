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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import org.apache.shardingsphere.data.pipeline.core.check.consistency.SingleTableDataCalculatorRegistry;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;

import java.util.Properties;

/**
 * Abstract data consistency check algorithm.
 */
public abstract class AbstractDataConsistencyCheckAlgorithm implements DataConsistencyCheckAlgorithm {
    
    private Properties props = new Properties();
    
    @Override
    public Properties getProps() {
        return props;
    }
    
    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public String getProvider() {
        return "ShardingSphere";
    }
    
    @Override
    public final SingleTableDataCalculator getSingleTableDataCalculator(final String supportedDatabaseType) {
        SingleTableDataCalculator result = SingleTableDataCalculatorRegistry.newServiceInstance(getType(), supportedDatabaseType);
        result.setAlgorithmProps(props);
        result.init();
        return result;
    }
}
