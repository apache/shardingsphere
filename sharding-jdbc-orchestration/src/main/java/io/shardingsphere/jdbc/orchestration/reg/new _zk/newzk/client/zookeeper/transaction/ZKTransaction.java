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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.Transaction;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * @author lidongbo
 * @since zookeeper 3.4.0
 */
public class ZKTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction.class);
    
    private final Transaction transaction;
    
    private final String rootNode;

    public ZKTransaction(final String root, final io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.Holder holder) {
        transaction = holder.getZooKeeper().transaction();
        rootNode = root;
        LOGGER.debug("ZKTransaction root:{}", rootNode);
    }
    
    /**
     * create target node.
     *
     * @param path key
     * @param data value
     * @param acl acl
     * @param createMode createMode
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        this.transaction.create(io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        LOGGER.debug("wait create:{},data:{},acl:{},createMode:{}", new Object[]{path, data, acl, createMode});
        return this;
    }
    
    /**
     * delete target node.
     *
     * @param path key
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction delete(final String path) {
        return delete(path, Constants.VERSION);
    }
    
    /**
     * delete target node.
     *
     * @param path key
     * @param version version
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction delete(final String path, final int version) {
        this.transaction.delete(io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil.getRealPath(rootNode, path), version);
        LOGGER.debug("wait delete:{}", path);
        return this;
    }
    
    /**
     * check target node.
     *
     * @param path key
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction check(final String path) {
        return check(path, Constants.VERSION);
    }
    
    /**
     * check target node.
     *
     * @param path key
     * @param version version
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction check(final String path, final int version) {
        this.transaction.check(io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil.getRealPath(rootNode, path), version);
        LOGGER.debug("wait check:{}", path);
        return this;
    }
    
    /**
     * update target node.
     *
     * @param path key
     * @param data data
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction setData(final String path, final byte[] data) {
        return setData(path, data, Constants.VERSION);
    }
    
    /**
     * update target node.
     *
     * @param path key
     * @param data data
     * @param version version
     * @return ZKTransaction
     */
    public io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction setData(final String path, final byte[] data, final int version) {
        this.transaction.setData(io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil.getRealPath(rootNode, path), data, version);
        LOGGER.debug("wait setData:{},data:{}", path, data);
        return this;
    }
    
    /**
     * commit.
     *
     * @return operation result
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        LOGGER.debug("ZKTransaction commit");
        return this.transaction.commit();
    }
}
