package io.shardingjdbc.orchestration.reg.etcd.internal.stub;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: rpc.proto")
public final class ClusterGrpc {

  public static final String SERVICE_NAME = "etcdserverpb.Cluster";
  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse> METHOD_MEMBER_ADD =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Cluster", "MemberAdd"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse.getDefaultInstance()))
          .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberAdd"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse> METHOD_MEMBER_REMOVE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Cluster", "MemberRemove"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse.getDefaultInstance()))
          .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberRemove"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse> METHOD_MEMBER_UPDATE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Cluster", "MemberUpdate"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse.getDefaultInstance()))
          .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberUpdate"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse> METHOD_MEMBER_LIST =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Cluster", "MemberList"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse.getDefaultInstance()))
          .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberList"))
          .build();
  private static final int METHODID_MEMBER_ADD = 0;
  private static final int METHODID_MEMBER_REMOVE = 1;
  private static final int METHODID_MEMBER_UPDATE = 2;
  private static final int METHODID_MEMBER_LIST = 3;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private ClusterGrpc() {}

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClusterStub newStub(io.grpc.Channel channel) {
    return new ClusterStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClusterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ClusterBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClusterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ClusterFutureStub(channel);
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ClusterGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClusterFileDescriptorSupplier())
              .addMethod(METHOD_MEMBER_ADD)
              .addMethod(METHOD_MEMBER_REMOVE)
              .addMethod(METHOD_MEMBER_UPDATE)
              .addMethod(METHOD_MEMBER_LIST)
              .build();
        }
      }
    }
    return result;
  }

  /**
   */
  public static abstract class ClusterImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest request,
                          io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_MEMBER_ADD, responseObserver);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_MEMBER_REMOVE, responseObserver);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_MEMBER_UPDATE, responseObserver);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_MEMBER_LIST, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_MEMBER_ADD,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse>(
                  this, METHODID_MEMBER_ADD)))
          .addMethod(
            METHOD_MEMBER_REMOVE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse>(
                  this, METHODID_MEMBER_REMOVE)))
          .addMethod(
            METHOD_MEMBER_UPDATE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse>(
                  this, METHODID_MEMBER_UPDATE)))
          .addMethod(
            METHOD_MEMBER_LIST,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse>(
                  this, METHODID_MEMBER_LIST)))
          .build();
    }
  }

  /**
   */
  public static final class ClusterStub extends io.grpc.stub.AbstractStub<ClusterStub> {
    private ClusterStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ClusterStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest request,
                          io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_MEMBER_ADD, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_MEMBER_REMOVE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_MEMBER_UPDATE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_MEMBER_LIST, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClusterBlockingStub extends io.grpc.stub.AbstractStub<ClusterBlockingStub> {
    private ClusterBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ClusterBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse memberAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_MEMBER_ADD, getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse memberRemove(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_MEMBER_REMOVE, getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse memberUpdate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_MEMBER_UPDATE, getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse memberList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_MEMBER_LIST, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClusterFutureStub extends io.grpc.stub.AbstractStub<ClusterFutureStub> {
    private ClusterFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected ClusterFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse> memberAdd(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_MEMBER_ADD, getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse> memberRemove(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_MEMBER_REMOVE, getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse> memberUpdate(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_MEMBER_UPDATE, getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse> memberList(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_MEMBER_LIST, getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClusterImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClusterImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MEMBER_ADD:
          serviceImpl.memberAdd((io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberAddResponse>) responseObserver);
          break;
        case METHODID_MEMBER_REMOVE:
          serviceImpl.memberRemove((io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberRemoveResponse>) responseObserver);
          break;
        case METHODID_MEMBER_UPDATE:
          serviceImpl.memberUpdate((io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberUpdateResponse>) responseObserver);
          break;
        case METHODID_MEMBER_LIST:
          serviceImpl.memberList((io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.MemberListResponse>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClusterBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return EtcdProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Cluster");
    }
  }

  private static final class ClusterFileDescriptorSupplier
      extends ClusterBaseDescriptorSupplier {
    ClusterFileDescriptorSupplier() {}
  }

  private static final class ClusterMethodDescriptorSupplier
      extends ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClusterMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
