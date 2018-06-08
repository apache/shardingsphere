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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.action;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/*
 * @author lidongbo
 */
public interface IGroupAction {
    
    /**
     * create target node and all need created.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void createAllNeedPath(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;
    
    /**
     * delete target node and children nodes.
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void deleteAllChildren(String key) throws KeeperException, InterruptedException;
    
    /**
    * delete the current node with force and delete the super node whose only child node is current node recursively.
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
    */
    void deleteCurrentBranch(String key) throws KeeperException, InterruptedException;
}
