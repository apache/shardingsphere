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
import org.apache.zookeeper.KeeperException;

/**
 * Async retry operation which update action.
 *
 * @author lidongbo
 */
public final class UpdateOperation extends BaseOperation {
    
    private final String key;
    
    private final String value;
    
    public UpdateOperation(final IProvider provider, final String key, final String value) {
        super(provider);
        this.key = key;
        this.value = value;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        getProvider().update(getProvider().getRealPath(key), value);
    }
    
    @Override
    public String toString() {
        return String.format("UpdateOperation key: %s, value: %s", key, value);
    }
}
