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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayPolicyExecutor;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Connection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
@Getter(value = AccessLevel.PROTECTED)
public abstract class BaseOperation implements Delayed {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOperation.class);
    
    private final IProvider provider;
    
    @Setter
    private DelayPolicyExecutor delayPolicyExecutor;
    
    protected BaseOperation(final IProvider provider) {
        this.provider = provider;
    }
    
    @Override
    public long getDelay(final TimeUnit unit) {
        long absoluteBlock = this.delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
        LOGGER.debug("queue getDelay block:{}", absoluteBlock);
        return unit.convert(absoluteBlock, TimeUnit.MILLISECONDS);
    }
    
    /**
     * queue precedence.
     */
    @Override
    public int compareTo(final Delayed delayed) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    protected abstract void execute() throws KeeperException, InterruptedException;
    
    /**
     * queue precedence.
     *
     * @return whether or not continue enqueue
     * @throws KeeperException Keeper Exception
     * @throws InterruptedException InterruptedException
     */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result;
        try {
            execute();
            result = true;
        } catch (KeeperException e) {
            if (Connection.needReset(e)) {
                provider.resetConnection();
            }
            result = false;
        }
        if (!result && delayPolicyExecutor.hasNext()) {
            delayPolicyExecutor.next();
            return true;
        }
        return false;
    }
}
