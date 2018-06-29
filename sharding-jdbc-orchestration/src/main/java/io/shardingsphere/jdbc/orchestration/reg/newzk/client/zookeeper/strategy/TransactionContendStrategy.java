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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.Callback;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.ITransactionProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.provider.BaseProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/*
 * @author lidongbo
 * @since zookeeper 3.4.0
 */
public class TransactionContendStrategy extends ContentionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContendStrategy.class);
    
    public TransactionContendStrategy(final ITransactionProvider provider) {
        super(provider);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateAllNeedElection(key, value, createMode, null);
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy createAllNeedPath executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateAllNeedElection(final String key, final String value, final CreateMode createMode, final Callback callback) {
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                LOGGER.debug("ContentionStrategy createAllNeedPath action:{}", key);
                ZKTransaction transaction = new ZKTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                createBegin(key, value, createMode, transaction);
                transaction.commit();
            }
            
            @Override
            public void callback() {
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }

    private void createBegin(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1) {
            ((ITransactionProvider) getProvider()).createInTransaction(key, value, createMode, transaction);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (getProvider().exists(nodes.get(i))) {
                LOGGER.info("create node exist:{}", nodes.get(i));
                continue;
            }
            LOGGER.debug("node not exist and create:", nodes.get(i));
            if (i == nodes.size() - 1) {
                ((ITransactionProvider) getProvider()).createInTransaction(nodes.get(i), value, createMode, transaction);
            } else {
                ((ITransactionProvider) getProvider()).createInTransaction(nodes.get(i), Constants.NOTHING_VALUE, createMode, transaction);
            }
        }
    }

    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZKTransaction transaction = new ZKTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                deleteChildren(getProvider().getRealPath(key), true, transaction);
                transaction.commit();
            }
        });
        LOGGER.debug("ContentionStrategy deleteAllChildren executeContention");
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        List<String> children = getProvider().getChildren(key);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!getProvider().exists(child)) {
                LOGGER.info("delete not exist:{}", child);
                continue;
            }
            LOGGER.debug("deleteChildren:{}", child);
            deleteChildren(child, true, transaction);
        }
        if (deleteCurrentNode) {
            transaction.delete(key, Constants.VERSION);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZKTransaction transaction = new ZKTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                deleteBranch(getProvider().getRealPath(key), transaction);
                transaction.commit();
            }
        });
        LOGGER.debug("ContentionStrategy deleteCurrentBranch executeContention");
    }

    private void deleteBranch(final String key, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        deleteChildren(key, false, transaction);
        Stack<String> pathStack = getProvider().getDeletingPaths(key);
        String prePath = key;
        while (!pathStack.empty()) {
            String node = pathStack.pop();
            // contrast cache
            // Performance needs testing
            List<String> children = getProvider().getChildren(node);
            boolean canDelete = children.size() == 0 || children.size() == 1;
            if (children.size() == 1) {
                if (!PathUtil.getRealPath(node, children.get(0)).equals(prePath)) {
                    canDelete = false;
                }
            }
            if (getProvider().exists(node) && canDelete) {
                transaction.delete(node, Constants.VERSION);
            }
            prePath = node;
        }
    }
}
