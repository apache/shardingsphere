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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry.DelayPolicyExecutor;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.Connection;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/*
 * base async retry operation
 *
 * @author lidongbo
 */
public abstract class BaseOperation implements Delayed {
    private static final Logger logger = LoggerFactory.getLogger(BaseOperation.class);
    protected final IProvider provider;
    protected DelayPolicyExecutor delayPolicyExecutor;
    
    protected BaseOperation(final IProvider provider) {
        this.provider = provider;
    }
    
    public void setRetrial(final DelayPolicyExecutor delayPolicyExecutor){
        this.delayPolicyExecutor = delayPolicyExecutor;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        long absoluteBlock = this.delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
        logger.debug("queue getDelay block:{}", absoluteBlock);
        long result = unit.convert(absoluteBlock, TimeUnit.MILLISECONDS);
        return result;
    }
    
    /**
     * queue precedence
     */
    @Override
    public int compareTo(Delayed delayed) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    protected abstract void execute() throws KeeperException, InterruptedException;
    
    /*
    * @Return whether or not continue enqueue
    */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result;
        try {
            execute();
            result = true;
        } catch (KeeperException ee) {
            if (Connection.needReset(ee)){
                provider.resetConnection();
                result = false;
            } else {
                throw ee;
            }
        }
        if (!result && delayPolicyExecutor.hasNext()){
            delayPolicyExecutor.next();
            return true;
        }
        return false;
    }
}
