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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.Callback;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.election.LeaderElection;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.Constants;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/*
 * The ContentionStrategy is effective only when all the clients of the node which be competitive are using ContentionStrategy.
 *
 * @author lidongbo
 */
public class ContentionStrategy extends UsualStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ContentionStrategy.class);
    public ContentionStrategy(final IProvider provider) {
        super(provider);
    }
    
    @Override
    /*
    * Don't use this if you don't have to use it.
    */
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.getData(provider.getRealPath(key), callback, ctx);
                logger.debug("ContentionStrategy getData action:{}", key);
            }
        };
        provider.executeContention(election);
        logger.debug("ContentionStrategy getData executeContention");
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateElection(key, value, createMode, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy createCurrentOnly executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateElection(final String key, final String value, final CreateMode createMode, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.create(provider.getRealPath(key), value, createMode);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        LeaderElection election = buildUpdateElection(key, value, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy update executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildUpdateElection(final String key, final String value, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), value);
                logger.debug("ContentionStrategy update action:{},value:{}", key, value);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = buildDeleteElection(key, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy deleteOnlyCurrent executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildDeleteElection(final String key, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.delete(provider.getRealPath(key));
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.delete(provider.getRealPath(key), callback, ctx);
                logger.debug("ContentionStrategy deleteOnlyCurrent action:{},ctx:{}", key, ctx);
            }
        });
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateAllNeedElection(key, value, createMode, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy createAllNeedPath executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateAllNeedElection(final String key, final String value, final CreateMode createMode, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                logger.debug("ContentionStrategy createAllNeedPath action:{}", key);
                createBegin(provider.getRealPath(key), value, createMode);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            provider.create(key, value, createMode);
            return;
        }
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (provider.exists(nodes.get(i))){
                logger.info("create node exist:{}", nodes.get(i));
                continue;
            }
            logger.debug("create node not exist:", nodes.get(i));
            if (i == nodes.size() - 1){
                provider.create(nodes.get(i), value, createMode);
            } else {
                provider.create(nodes.get(i), Constants.NOTHING_VALUE, createMode);
            }
        }
    }
    
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteChildren(provider.getRealPath(key), true);
            }
        });
        logger.debug("ContentionStrategy deleteAllChildren executeContention");
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = provider.getChildren(key);
        logger.debug("deleteChildren:{}", children);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!provider.exists(child)){
                logger.info("delete not exist:{}", child);
                continue;
            }
            logger.debug("deleteChildren:{}", child);
            deleteChildren(child, true);
        }
        if (deleteCurrentNode){
            provider.delete(key);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteBranch(provider.getRealPath(key));
            }
        });
        logger.debug("ContentionStrategy deleteCurrentBranch executeContention");
    }
    
    private void deleteBranch(String key) throws KeeperException, InterruptedException {
        deleteChildren(key, false);
        Stack<String> pathStack = provider.getDeletingPaths(key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (provider.exists(node)){
                try {
                    provider.delete(node);
                } catch (KeeperException.NotEmptyException ee){
                    logger.warn("deleteBranch {} exist other children:{}", node, this.getChildren(node));
                    logger.debug(ee.getMessage());
                    return;
                }
            }
            logger.info("deleteBranch node not exist:{}", node);
        }
    }
    
    
    
    //todo Use arbitrary competitive nodes
    //IExecStrategy convert to ContentionStrategy
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildCreateElection(key, value, createMode, callback));
    }
    
    public void update(final String key, final String value, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildUpdateElection(key, value, null));
    }

    public void deleteOnlyCurrent(final String key, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildDeleteElection(key, null));
    }
}
