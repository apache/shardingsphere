package io.shardingjdbc.orchestration.reg.etcd.internal.stub;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: rpc.proto")
public final class LeaseGrpc {

  public static final String SERVICE_NAME = "etcdserverpb.Lease";
  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse> METHOD_LEASE_GRANT =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Lease", "LeaseGrant"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse.getDefaultInstance()))
          .setSchemaDescriptor(new LeaseMethodDescriptorSupplier("LeaseGrant"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse> METHOD_LEASE_REVOKE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Lease", "LeaseRevoke"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new LeaseMethodDescriptorSupplier("LeaseRevoke"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse> METHOD_LEASE_KEEP_ALIVE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Lease", "LeaseKeepAlive"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse.getDefaultInstance()))
          .setSchemaDescriptor(new LeaseMethodDescriptorSupplier("LeaseKeepAlive"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse> METHOD_LEASE_TIME_TO_LIVE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Lease", "LeaseTimeToLive"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse.getDefaultInstance()))
          .setSchemaDescriptor(new LeaseMethodDescriptorSupplier("LeaseTimeToLive"))
          .build();
  private static final int METHODID_LEASE_GRANT = 0;
  private static final int METHODID_LEASE_REVOKE = 1;
  private static final int METHODID_LEASE_TIME_TO_LIVE = 2;
  private static final int METHODID_LEASE_KEEP_ALIVE = 3;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private LeaseGrpc() {}

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LeaseStub newStub(io.grpc.Channel channel) {
    return new LeaseStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LeaseBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new LeaseBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LeaseFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new LeaseFutureStub(channel);
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (LeaseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LeaseFileDescriptorSupplier())
              .addMethod(METHOD_LEASE_GRANT)
              .addMethod(METHOD_LEASE_REVOKE)
              .addMethod(METHOD_LEASE_KEEP_ALIVE)
              .addMethod(METHOD_LEASE_TIME_TO_LIVE)
              .build();
        }
      }
    }
    return result;
  }

  /**
   */
  public static abstract class LeaseImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * LeaseGrant creates a lease which expires if the server does not receive a keepAlive
     * within a given time to live period. All keys attached to the lease will be expired and
     * deleted if the lease expires. Each expired key generates a delete event in the event history.
     * </pre>
     */
    public void leaseGrant(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LEASE_GRANT, responseObserver);
    }

    /**
     * <pre>
     * LeaseRevoke revokes a lease. All keys attached to the lease will expire and be deleted.
     * </pre>
     */
    public void leaseRevoke(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LEASE_REVOKE, responseObserver);
    }

    /**
     * <pre>
     * LeaseKeepAlive keeps the lease alive by streaming keep alive requests from the client
     * to the server and streaming keep alive responses from the server to the client.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest> leaseKeepAlive(
        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_LEASE_KEEP_ALIVE, responseObserver);
    }

    /**
     * <pre>
     * LeaseTimeToLive retrieves lease information.
     * </pre>
     */
    public void leaseTimeToLive(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest request,
                                io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LEASE_TIME_TO_LIVE, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_LEASE_GRANT,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse>(
                  this, METHODID_LEASE_GRANT)))
          .addMethod(
            METHOD_LEASE_REVOKE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse>(
                  this, METHODID_LEASE_REVOKE)))
          .addMethod(
            METHOD_LEASE_KEEP_ALIVE,
            asyncBidiStreamingCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse>(
                  this, METHODID_LEASE_KEEP_ALIVE)))
          .addMethod(
            METHOD_LEASE_TIME_TO_LIVE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse>(
                  this, METHODID_LEASE_TIME_TO_LIVE)))
          .build();
    }
  }

  /**
   */
  public static final class LeaseStub extends io.grpc.stub.AbstractStub<LeaseStub> {
    private LeaseStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LeaseStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected LeaseStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LeaseStub(channel, callOptions);
    }

    /**
     * <pre>
     * LeaseGrant creates a lease which expires if the server does not receive a keepAlive
     * within a given time to live period. All keys attached to the lease will be expired and
     * deleted if the lease expires. Each expired key generates a delete event in the event history.
     * </pre>
     */
    public void leaseGrant(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LEASE_GRANT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * LeaseRevoke revokes a lease. All keys attached to the lease will expire and be deleted.
     * </pre>
     */
    public void leaseRevoke(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LEASE_REVOKE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * LeaseKeepAlive keeps the lease alive by streaming keep alive requests from the client
     * to the server and streaming keep alive responses from the server to the client.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveRequest> leaseKeepAlive(
        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_LEASE_KEEP_ALIVE, getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * LeaseTimeToLive retrieves lease information.
     * </pre>
     */
    public void leaseTimeToLive(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest request,
                                io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LEASE_TIME_TO_LIVE, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class LeaseBlockingStub extends io.grpc.stub.AbstractStub<LeaseBlockingStub> {
    private LeaseBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LeaseBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected LeaseBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LeaseBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * LeaseGrant creates a lease which expires if the server does not receive a keepAlive
     * within a given time to live period. All keys attached to the lease will be expired and
     * deleted if the lease expires. Each expired key generates a delete event in the event history.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse leaseGrant(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LEASE_GRANT, getCallOptions(), request);
    }

    /**
     * <pre>
     * LeaseRevoke revokes a lease. All keys attached to the lease will expire and be deleted.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse leaseRevoke(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LEASE_REVOKE, getCallOptions(), request);
    }

    /**
     * <pre>
     * LeaseTimeToLive retrieves lease information.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse leaseTimeToLive(io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LEASE_TIME_TO_LIVE, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class LeaseFutureStub extends io.grpc.stub.AbstractStub<LeaseFutureStub> {
    private LeaseFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LeaseFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected LeaseFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LeaseFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * LeaseGrant creates a lease which expires if the server does not receive a keepAlive
     * within a given time to live period. All keys attached to the lease will be expired and
     * deleted if the lease expires. Each expired key generates a delete event in the event history.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse> leaseGrant(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LEASE_GRANT, getCallOptions()), request);
    }

    /**
     * <pre>
     * LeaseRevoke revokes a lease. All keys attached to the lease will expire and be deleted.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse> leaseRevoke(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LEASE_REVOKE, getCallOptions()), request);
    }

    /**
     * <pre>
     * LeaseTimeToLive retrieves lease information.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse> leaseTimeToLive(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LEASE_TIME_TO_LIVE, getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LeaseImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LeaseImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LEASE_GRANT:
          serviceImpl.leaseGrant((io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseGrantResponse>) responseObserver);
          break;
        case METHODID_LEASE_REVOKE:
          serviceImpl.leaseRevoke((io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseRevokeResponse>) responseObserver);
          break;
        case METHODID_LEASE_TIME_TO_LIVE:
          serviceImpl.leaseTimeToLive((io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseTimeToLiveResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LEASE_KEEP_ALIVE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.leaseKeepAlive(
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.LeaseKeepAliveResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class LeaseBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LeaseBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.shardingjdbc.orchestration.reg.etcd.internal.stub.EtcdProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Lease");
    }
  }

  private static final class LeaseFileDescriptorSupplier
      extends LeaseBaseDescriptorSupplier {
    LeaseFileDescriptorSupplier() {}
  }

  private static final class LeaseMethodDescriptorSupplier
      extends LeaseBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LeaseMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
