/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.BaseOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Async retry operation which create all need action.
 *
 * @author lidongbo
 */
public final class CreateAllNeedOperation extends BaseOperation {
    
    private final String key;
    
    private final String value;
    
    private final CreateMode createMode;
    
    public CreateAllNeedOperation(final IProvider provider, final String key, final String value, final CreateMode createMode) {
        super(provider);
        this.key = key;
        this.value = value;
        this.createMode = createMode;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(getProvider()).createAllNeedPath(key, value, createMode);
    }
    
    @Override
    public String toString() {
        return String.format("CreateAllNeedOperation key: %s,value: %s, createMode: %s", key, value, createMode.name());
    }
}
