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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

/*
 * Client task.
 *
 * @author lidongbo
 */
@Slf4j
public abstract class ClientTask implements Runnable {
    
    private final IProvider provider;
    
    public ClientTask(final IProvider provider) {
        this.provider = provider;
    }
    
    /**
     * Run.
     *
     * @param provider provider
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public abstract void run(IProvider provider) throws KeeperException, InterruptedException;
    
    @Override
    public void run() {
        try {
            run(provider);
        } catch (final KeeperException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
