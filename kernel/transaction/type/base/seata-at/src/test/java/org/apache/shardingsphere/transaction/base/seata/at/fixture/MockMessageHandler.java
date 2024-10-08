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

package org.apache.shardingsphere.transaction.base.seata.at.fixture;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.apache.seata.common.XID;
import org.apache.seata.core.model.GlobalStatus;
import org.apache.seata.core.protocol.AbstractMessage;
import org.apache.seata.core.protocol.AbstractResultMessage;
import org.apache.seata.core.protocol.HeartbeatMessage;
import org.apache.seata.core.protocol.MergeResultMessage;
import org.apache.seata.core.protocol.MergedWarpMessage;
import org.apache.seata.core.protocol.ProtocolConstants;
import org.apache.seata.core.protocol.RegisterRMRequest;
import org.apache.seata.core.protocol.RegisterRMResponse;
import org.apache.seata.core.protocol.RegisterTMRequest;
import org.apache.seata.core.protocol.RegisterTMResponse;
import org.apache.seata.core.protocol.ResultCode;
import org.apache.seata.core.protocol.RpcMessage;
import org.apache.seata.core.protocol.transaction.BranchCommitRequest;
import org.apache.seata.core.protocol.transaction.BranchCommitResponse;
import org.apache.seata.core.protocol.transaction.BranchRegisterRequest;
import org.apache.seata.core.protocol.transaction.BranchRegisterResponse;
import org.apache.seata.core.protocol.transaction.BranchReportRequest;
import org.apache.seata.core.protocol.transaction.BranchReportResponse;
import org.apache.seata.core.protocol.transaction.BranchRollbackRequest;
import org.apache.seata.core.protocol.transaction.BranchRollbackResponse;
import org.apache.seata.core.protocol.transaction.GlobalBeginRequest;
import org.apache.seata.core.protocol.transaction.GlobalBeginResponse;
import org.apache.seata.core.protocol.transaction.GlobalCommitRequest;
import org.apache.seata.core.protocol.transaction.GlobalCommitResponse;
import org.apache.seata.core.protocol.transaction.GlobalRollbackRequest;
import org.apache.seata.core.protocol.transaction.GlobalRollbackResponse;
import org.apache.seata.core.protocol.transaction.GlobalStatusRequest;
import org.apache.seata.core.protocol.transaction.GlobalStatusResponse;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock command handler for seata.
 */
@Sharable
public final class MockMessageHandler extends ChannelDuplexHandler {
    
    private final AtomicLong id = new AtomicLong();
    
    @Getter
    private final Queue<Object> requestQueue = new ConcurrentLinkedQueue<>();
    
    @Getter
    private final Queue<Object> responseQueue = new ConcurrentLinkedQueue<>();
    
    static {
        XID.setIpAddress("127.0.0.1");
        XID.setPort(8891);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        RpcMessage request = (RpcMessage) msg;
        RpcMessage response = newRpcMessage(request);
        Object requestBody = request.getBody();
        if (requestBody instanceof RegisterTMRequest) {
            response.setBody(new RegisterTMResponse(true));
        } else if (requestBody instanceof RegisterRMRequest) {
            response.setBody(new RegisterRMResponse(true));
        } else if (requestBody == HeartbeatMessage.PING) {
            response.setBody(HeartbeatMessage.PONG);
        } else if (requestBody instanceof MergedWarpMessage) {
            setMergeResultMessage(request, response);
        } else if (requestBody instanceof GlobalBeginRequest) {
            GlobalBeginResponse globalBeginResponse = new GlobalBeginResponse();
            globalBeginResponse.setXid(XID.generateXID(id.incrementAndGet()));
            globalBeginResponse.setResultCode(ResultCode.Success);
            response.setBody(globalBeginResponse);
        } else if (requestBody instanceof GlobalCommitRequest) {
            GlobalCommitResponse globalCommitResponse = new GlobalCommitResponse();
            globalCommitResponse.setResultCode(ResultCode.Success);
            globalCommitResponse.setGlobalStatus(GlobalStatus.Committing);
            response.setBody(globalCommitResponse);
        } else if (requestBody instanceof GlobalRollbackRequest) {
            GlobalRollbackResponse globalRollbackResponse = new GlobalRollbackResponse();
            globalRollbackResponse.setResultCode(ResultCode.Success);
            globalRollbackResponse.setGlobalStatus(GlobalStatus.Rollbacked);
            response.setBody(globalRollbackResponse);
        }
        if (requestBody != HeartbeatMessage.PING) {
            requestQueue.offer(requestBody);
            responseQueue.offer(response.getBody());
        }
        ctx.writeAndFlush(response);
    }
    
    private RpcMessage newRpcMessage(final RpcMessage request) {
        RpcMessage result = new RpcMessage();
        result.setMessageType(request.getBody() == HeartbeatMessage.PING ? ProtocolConstants.MSGTYPE_HEARTBEAT_RESPONSE : ProtocolConstants.MSGTYPE_RESPONSE);
        result.setCodec(request.getCodec());
        result.setCompressor(request.getCompressor());
        result.setId(request.getId());
        return result;
    }
    
    private void setMergeResultMessage(final RpcMessage request, final RpcMessage response) {
        MergedWarpMessage warpMessage = (MergedWarpMessage) request.getBody();
        AbstractResultMessage[] resultMessages = new AbstractResultMessage[warpMessage.msgs.size()];
        for (int i = 0; i < warpMessage.msgs.size(); i++) {
            AbstractMessage each = warpMessage.msgs.get(i);
            if (each instanceof GlobalBeginRequest) {
                GlobalBeginResponse globalBeginResponse = new GlobalBeginResponse();
                globalBeginResponse.setXid(XID.generateXID(id.incrementAndGet()));
                globalBeginResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = globalBeginResponse;
            } else if (each instanceof GlobalCommitRequest) {
                GlobalCommitResponse globalCommitResponse = new GlobalCommitResponse();
                globalCommitResponse.setResultCode(ResultCode.Success);
                globalCommitResponse.setGlobalStatus(GlobalStatus.Committing);
                resultMessages[i] = globalCommitResponse;
            } else if (each instanceof GlobalRollbackRequest) {
                GlobalRollbackResponse globalRollbackResponse = new GlobalRollbackResponse();
                globalRollbackResponse.setResultCode(ResultCode.Success);
                globalRollbackResponse.setGlobalStatus(GlobalStatus.Rollbacking);
                resultMessages[i] = globalRollbackResponse;
            } else if (each instanceof BranchRegisterRequest) {
                BranchRegisterResponse branchRegisterResponse = new BranchRegisterResponse();
                branchRegisterResponse.setBranchId(id.incrementAndGet());
                branchRegisterResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = branchRegisterResponse;
            } else if (each instanceof BranchReportRequest) {
                BranchReportResponse reportResponse = new BranchReportResponse();
                reportResponse.setResultCode(ResultCode.Success);
                resultMessages[i] = reportResponse;
            } else if (each instanceof BranchCommitRequest) {
                resultMessages[i] = new BranchCommitResponse();
            } else if (each instanceof BranchRollbackRequest) {
                resultMessages[i] = new BranchRollbackResponse();
            } else if (each instanceof GlobalStatusRequest) {
                resultMessages[i] = new GlobalStatusResponse();
            }
        }
        MergeResultMessage resultMessage = new MergeResultMessage();
        resultMessage.setMsgs(resultMessages);
        response.setBody(resultMessage);
    }
}
