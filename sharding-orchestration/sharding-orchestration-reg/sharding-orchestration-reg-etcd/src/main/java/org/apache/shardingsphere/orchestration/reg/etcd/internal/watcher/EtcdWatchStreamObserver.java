/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.reg.etcd.internal.watcher;

import etcdserverpb.Rpc;
import etcdserverpb.Rpc.WatchResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import mvccpb.Kv.Event;
import org.apache.shardingsphere.orchestration.reg.exception.RegistryCenterException;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEventListener;

/**
 * Watch stream observer.
 *
 * @author junxiong
 */
@RequiredArgsConstructor
public final class EtcdWatchStreamObserver implements StreamObserver<WatchResponse> {
    
    private final DataChangedEventListener dataChangedEventListener;
    
    @Override
    public void onNext(final Rpc.WatchResponse response) {
        if (response.getCanceled() || response.getCreated()) {
            return;
        }
        for (Event event : response.getEventsList()) {
            ChangedType changedType = getChangedType(event);
            if (ChangedType.IGNORED != changedType) {
                dataChangedEventListener.onChange(new DataChangedEvent(event.getKv().getKey().toStringUtf8(), event.getKv().getValue().toStringUtf8(), changedType));
            }
        }
    }
    
    private ChangedType getChangedType(final Event event) {
        switch (event.getType()) {
            case PUT:
                return DataChangedEvent.ChangedType.UPDATED;
            case DELETE:
                return DataChangedEvent.ChangedType.DELETED;
            default:
                return DataChangedEvent.ChangedType.IGNORED;
        }
    }
    
    @Override
    public void onError(final Throwable throwable) {
        // TODO retry watch later
        throw new RegistryCenterException(new Exception(throwable));
    }
    
    @Override
    public void onCompleted() {
    }
}
