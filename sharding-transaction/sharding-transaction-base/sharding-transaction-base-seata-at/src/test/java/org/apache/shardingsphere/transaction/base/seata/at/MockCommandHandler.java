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

package org.apache.shardingsphere.transaction.base.seata.at;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.seata.common.XID;
import io.seata.core.protocol.AbstractResultMessage;
import io.seata.core.protocol.HeartbeatMessage;
import io.seata.core.protocol.MergeResultMessage;
import io.seata.core.protocol.MergedWarpMessage;
import io.seata.core.protocol.RegisterRMRequest;
import io.seata.core.protocol.RegisterRMResponse;
import io.seata.core.protocol.RegisterTMRequest;
import io.seata.core.protocol.RegisterTMResponse;
import io.seata.core.protocol.ResultCode;
import io.seata.core.protocol.RpcMessage;
import io.seata.core.protocol.transaction.BranchCommitRequest;
import io.seata.core.protocol.transaction.BranchCommitResponse;
import io.seata.core.protocol.transaction.BranchRegisterRequest;
import io.seata.core.protocol.transaction.BranchRegisterResponse;
import io.seata.core.protocol.transaction.BranchReportRequest;
import io.seata.core.protocol.transaction.BranchReportResponse;
import io.seata.core.protocol.transaction.BranchRollbackRequest;
import io.seata.core.protocol.transaction.BranchRollbackResponse;
import io.seata.core.protocol.transaction.GlobalBeginRequest;
import io.seata.core.protocol.transaction.GlobalBeginResponse;
import io.seata.core.protocol.transaction.GlobalLockQueryRequest;
import io.seata.core.protocol.transaction.GlobalStatusRequest;
import io.seata.core.protocol.transaction.GlobalStatusResponse;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock command handler for seata.
 *
 * @author zhaojun
 */
public final class MockCommandHandler extends ChannelDuplexHandler {
    
    private final AtomicLong id = new AtomicLong();
    
    static {
        XID.setIpAddress("127.0.0.1");
        XID.setPort(8091);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        RpcMessage request = (RpcMessage) msg;
        RpcMessage response = newRpcMessage(request);
        if (request.getBody() instanceof RegisterTMRequest) {
            response.setBody(new RegisterTMResponse(true));
        } else if (request.getBody() instanceof RegisterRMRequest) {
            response.setBody(new RegisterRMResponse(true));
        } else if (request.getBody() == HeartbeatMessage.PING) {
            response.setBody(HeartbeatMessage.PONG);
        } else if (request.getBody() instanceof MergedWarpMessage) {
            setMergeResultMessage(request, response);
        }
        ctx.writeAndFlush(response);
    }
    
    private void setMergeResultMessage(final RpcMessage request, final RpcMessage response) {
        MergedWarpMessage warpMessage = (MergedWarpMessage) request.getBody();
        AbstractResultMessage[] resultMessages = new AbstractResultMessage[warpMessage.msgs.size()];
        for (int i = 0; i < warpMessage.msgs.size(); i++) {
            if (warpMessage.msgs.get(i) instanceof GlobalBeginRequest) {
                GlobalBeginResponse globalBeginResponse = new GlobalBeginResponse();
                globalBeginResponse.setXid(XID.generateXID(id.incrementAndGet()));
                globalBeginResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = globalBeginResponse;
            } else if (warpMessage.msgs.get(i) instanceof BranchRegisterRequest) {
                BranchRegisterResponse branchRegisterResponse = new BranchRegisterResponse();
                branchRegisterResponse.setBranchId(id.incrementAndGet());
                branchRegisterResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = branchRegisterResponse;
            } else if (warpMessage.msgs.get(i) instanceof BranchReportRequest) {
                BranchReportResponse reportResponse = new BranchReportResponse();
                reportResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = reportResponse;
            } else if (warpMessage.msgs.get(i) instanceof BranchCommitRequest) {
                resultMessages[i] = new BranchCommitResponse();
            } else if (warpMessage.msgs.get(i) instanceof BranchRollbackRequest) {
                resultMessages[i] = new BranchRollbackResponse();
            } else if (warpMessage.msgs.get(i) instanceof GlobalLockQueryRequest) {
                resultMessages[i] = new GlobalBeginResponse();
            } else if (warpMessage.msgs.get(i) instanceof GlobalStatusRequest) {
                resultMessages[i] = new GlobalStatusResponse();
            }
        }
        MergeResultMessage resultMessage = new MergeResultMessage();
        resultMessage.setMsgs(resultMessages);
        response.setBody(resultMessage);
    }
    
    private RpcMessage newRpcMessage(final RpcMessage msg) {
        RpcMessage result = new RpcMessage();
        result.setAsync(true);
        result.setHeartbeat(false);
        result.setRequest(false);
        result.setId(msg.getId());
        return result;
    }
}
