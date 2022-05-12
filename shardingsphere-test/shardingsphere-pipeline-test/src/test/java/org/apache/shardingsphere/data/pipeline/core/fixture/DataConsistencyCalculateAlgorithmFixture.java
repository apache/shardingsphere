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

package org.apache.shardingsphere.data.pipeline.core.fixture;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
public final class DataConsistencyCalculateAlgorithmFixture implements DataConsistencyCalculateAlgorithm {
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Iterable<Object> calculate(final DataConsistencyCalculateParameter parameter) {
        return Collections.singletonList(true);
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return DatabaseTypeFactory.getInstances().stream().map(DatabaseType::getType).collect(Collectors.toList());
    }
    
    @Override
    public String getDescription() {
        return "Fixture";
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
