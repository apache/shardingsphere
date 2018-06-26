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

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public class TestHolder extends Holder {
    private final CountDownLatch CONNECTING = new CountDownLatch(1);
    
    public TestHolder(final BaseContext context) {
        super(context);
    }
    
    @Override
    protected void start(final int wait, final TimeUnit units) throws IOException, InterruptedException {
        initZookeeper();
        System.out.println("begin start await0:" + this.isConnected());
        CONNECTING.await(wait, units);
        System.out.println("await:"+ wait);
        System.out.println("start connected0:" + this.isConnected());
    }
    
    @Override
    protected void processConnection(final WatchedEvent event) {
        if (Watcher.Event.EventType.None == event.getType()) {
            if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                try {
                    System.out.println("begin processConnection wait0:" + this.isConnected() + " ThreadId : " + Thread.currentThread().getId());
                    Thread.sleep(1000);
                    System.out.println("processConnection done. ThreadId : " + Thread.currentThread().getId());
                } catch (Exception e) {
                    System.out.println("wait " + e.getMessage());
                }
                this.setConnected(true);
                System.out.println("processConnection connected0:" + this.isConnected());
                CONNECTING.countDown();
                return;
            }
        }
    }
}
