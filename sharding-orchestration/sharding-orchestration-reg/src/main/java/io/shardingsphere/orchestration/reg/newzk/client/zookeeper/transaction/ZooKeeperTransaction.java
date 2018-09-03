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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction;

import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.Holder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.Transaction;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Zookeeper transaction support.
 *
 * @author lidongbo
 * @since zookeeper 3.4.0
 */
public final class ZooKeeperTransaction extends BaseTransaction {
    
    private final String rootNode;
    
    private final Transaction transaction;

    public ZooKeeperTransaction(final String root, final Holder holder) {
        rootNode = root;
        transaction = holder.getZooKeeper().transaction();
    }
    
    @Override
    public ZooKeeperTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        transaction.create(PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        return this;
    }
    
    @Override
    public ZooKeeperTransaction delete(final String path) {
        return delete(path, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZooKeeperTransaction delete(final String path, final int version) {
        transaction.delete(PathUtil.getRealPath(rootNode, path), version);
        return this;
    }
    
    @Override
    public ZooKeeperTransaction check(final String path) {
        return check(path, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZooKeeperTransaction check(final String path, final int version) {
        transaction.check(PathUtil.getRealPath(rootNode, path), version);
        return this;
    }
    
    @Override
    public ZooKeeperTransaction setData(final String path, final byte[] data) {
        return setData(path, data, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZooKeeperTransaction setData(final String path, final byte[] data, final int version) {
        transaction.setData(PathUtil.getRealPath(rootNode, path), data, version);
        return this;
    }
    
    @Override
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        return transaction.commit();
    }
}
