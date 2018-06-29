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

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/*
 * @author lidongbo
 */
public interface ITransactionProvider extends IProvider {
    /**
     * only create target node in transaction.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @param transaction transaction
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void createInTransaction(String key, String value, CreateMode createMode, BaseTransaction transaction) throws KeeperException, InterruptedException;
}
