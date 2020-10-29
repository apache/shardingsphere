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

package org.apache.shardingsphere.scaling.core.fixture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.shardingsphere.scaling.core.job.position.BasePositionManager;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;

import javax.sql.DataSource;

public final class FixtureNopManager extends BasePositionManager<IncrementalPosition> implements PositionManager<IncrementalPosition> {
    
    private DataSource dataSource;
    
    public FixtureNopManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public FixtureNopManager(final String position) {
    }
    
    @Override
    public IncrementalPosition getPosition() {
        
        return new IncrementalPosition() {
            @Override
            public int compareTo(final Position o) {
                return 0;
            }
            
            @Override
            public JsonElement toJson() {
                return new JsonObject();
            }
        };
    }
}
