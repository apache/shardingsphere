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

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Base transaction.
 *
 * @author lidongbo
 */
public class BaseTransaction {
    
    /**
     * Create target node.
     *
     * @param path key
     * @param data value
     * @param acl acl
     * @param createMode create mode
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Delete target node.
     *
     * @param path key
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction delete(final String path) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Delete target node.
     *
     * @param path key
     * @param version version
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction delete(final String path, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Check target node.
     *
     * @param path key
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction check(final String path) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    
    /**
     * Check target node.
     *
     * @param path key
     * @param version version
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction check(final String path, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Update target node.
     *
     * @param path key
     * @param data data
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction setData(final String path, final byte[] data) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Update target node.
     *
     * @param path key
     * @param data data
     * @param version version
     * @return zookeeper transaction
     */
    public ZooKeeperTransaction setData(final String path, final byte[] data, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    /**
     * Commit.
     *
     * @return operation result
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        throw new UnsupportedOperationException("check zk version!");
    }
}
