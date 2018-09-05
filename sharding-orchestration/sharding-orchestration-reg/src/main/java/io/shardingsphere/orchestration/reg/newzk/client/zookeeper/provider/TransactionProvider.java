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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.provider;

import io.shardingsphere.orchestration.reg.newzk.client.action.ITransactionProvider;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.Holder;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.ZooKeeperTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Provider with transaction.
 *
 * @author lidongbo
 */
public final class TransactionProvider extends BaseProvider implements ITransactionProvider {
    
    public TransactionProvider(final String rootNode, final Holder holder, final boolean watched, final List<ACL> authorities) {
        super(rootNode, holder, watched, authorities);
    }
    
    @Override
    public void createInTransaction(final String key, final String value, final CreateMode createMode, final BaseTransaction transaction) {
        transaction.create(key, value.getBytes(ZookeeperConstants.UTF_8), getAuthorities(), createMode);
    }
    
    @Override
    public BaseTransaction transaction() {
        return new ZooKeeperTransaction(getRootNode(), getHolder());
    }
}
