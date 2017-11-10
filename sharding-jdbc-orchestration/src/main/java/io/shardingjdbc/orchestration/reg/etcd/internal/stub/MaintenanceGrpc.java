package io.shardingjdbc.orchestration.reg.etcd.internal.stub;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: rpc.proto")
public final class MaintenanceGrpc {

  public static final String SERVICE_NAME = "etcdserverpb.Maintenance";
  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse> METHOD_ALARM =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Maintenance", "Alarm"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MaintenanceMethodDescriptorSupplier("Alarm"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse> METHOD_STATUS =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Maintenance", "Status"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MaintenanceMethodDescriptorSupplier("Status"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse> METHOD_DEFRAGMENT =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Maintenance", "Defragment"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MaintenanceMethodDescriptorSupplier("Defragment"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse> METHOD_HASH =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Maintenance", "Hash"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MaintenanceMethodDescriptorSupplier("Hash"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse> METHOD_SNAPSHOT =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Maintenance", "Snapshot"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MaintenanceMethodDescriptorSupplier("Snapshot"))
          .build();
  private static final int METHODID_ALARM = 0;
  private static final int METHODID_STATUS = 1;
  private static final int METHODID_DEFRAGMENT = 2;
  private static final int METHODID_HASH = 3;
  private static final int METHODID_SNAPSHOT = 4;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private MaintenanceGrpc() {}

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MaintenanceStub newStub(io.grpc.Channel channel) {
    return new MaintenanceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MaintenanceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new MaintenanceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MaintenanceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new MaintenanceFutureStub(channel);
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MaintenanceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MaintenanceFileDescriptorSupplier())
              .addMethod(METHOD_ALARM)
              .addMethod(METHOD_STATUS)
              .addMethod(METHOD_DEFRAGMENT)
              .addMethod(METHOD_HASH)
              .addMethod(METHOD_SNAPSHOT)
              .build();
        }
      }
    }
    return result;
  }

  /**
   */
  public static abstract class MaintenanceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Alarm activates, deactivates, and queries alarms regarding cluster health.
     * </pre>
     */
    public void alarm(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest request,
                      io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ALARM, responseObserver);
    }

    /**
     * <pre>
     * Status gets the status of the member.
     * </pre>
     */
    public void status(io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest request,
                       io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STATUS, responseObserver);
    }

    /**
     * <pre>
     * Defragment defragments a member's backend database to recover storage space.
     * </pre>
     */
    public void defragment(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DEFRAGMENT, responseObserver);
    }

    /**
     * <pre>
     * Hash returns the hash of the local KV state for consistency checking purpose.
     * This is designed for testing; do not use this in production when there
     * are ongoing transactions.
     * </pre>
     */
    public void hash(io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest request,
                     io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_HASH, responseObserver);
    }

    /**
     * <pre>
     * Snapshot sends a snapshot of the entire backend from a member over a stream to a client.
     * </pre>
     */
    public void snapshot(io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SNAPSHOT, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_ALARM,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse>(
                  this, METHODID_ALARM)))
          .addMethod(
            METHOD_STATUS,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse>(
                  this, METHODID_STATUS)))
          .addMethod(
            METHOD_DEFRAGMENT,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse>(
                  this, METHODID_DEFRAGMENT)))
          .addMethod(
            METHOD_HASH,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse>(
                  this, METHODID_HASH)))
          .addMethod(
            METHOD_SNAPSHOT,
            asyncServerStreamingCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse>(
                  this, METHODID_SNAPSHOT)))
          .build();
    }
  }

  /**
   */
  public static final class MaintenanceStub extends io.grpc.stub.AbstractStub<MaintenanceStub> {
    private MaintenanceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MaintenanceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MaintenanceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MaintenanceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Alarm activates, deactivates, and queries alarms regarding cluster health.
     * </pre>
     */
    public void alarm(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest request,
                      io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ALARM, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Status gets the status of the member.
     * </pre>
     */
    public void status(io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest request,
                       io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STATUS, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Defragment defragments a member's backend database to recover storage space.
     * </pre>
     */
    public void defragment(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DEFRAGMENT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Hash returns the hash of the local KV state for consistency checking purpose.
     * This is designed for testing; do not use this in production when there
     * are ongoing transactions.
     * </pre>
     */
    public void hash(io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest request,
                     io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_HASH, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Snapshot sends a snapshot of the entire backend from a member over a stream to a client.
     * </pre>
     */
    public void snapshot(io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_SNAPSHOT, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class MaintenanceBlockingStub extends io.grpc.stub.AbstractStub<MaintenanceBlockingStub> {
    private MaintenanceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MaintenanceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MaintenanceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MaintenanceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Alarm activates, deactivates, and queries alarms regarding cluster health.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse alarm(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ALARM, getCallOptions(), request);
    }

    /**
     * <pre>
     * Status gets the status of the member.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse status(io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STATUS, getCallOptions(), request);
    }

    /**
     * <pre>
     * Defragment defragments a member's backend database to recover storage space.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse defragment(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DEFRAGMENT, getCallOptions(), request);
    }

    /**
     * <pre>
     * Hash returns the hash of the local KV state for consistency checking purpose.
     * This is designed for testing; do not use this in production when there
     * are ongoing transactions.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse hash(io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_HASH, getCallOptions(), request);
    }

    /**
     * <pre>
     * Snapshot sends a snapshot of the entire backend from a member over a stream to a client.
     * </pre>
     */
    public java.util.Iterator<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse> snapshot(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_SNAPSHOT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class MaintenanceFutureStub extends io.grpc.stub.AbstractStub<MaintenanceFutureStub> {
    private MaintenanceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MaintenanceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MaintenanceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MaintenanceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Alarm activates, deactivates, and queries alarms regarding cluster health.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse> alarm(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ALARM, getCallOptions()), request);
    }

    /**
     * <pre>
     * Status gets the status of the member.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse> status(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STATUS, getCallOptions()), request);
    }

    /**
     * <pre>
     * Defragment defragments a member's backend database to recover storage space.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse> defragment(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DEFRAGMENT, getCallOptions()), request);
    }

    /**
     * <pre>
     * Hash returns the hash of the local KV state for consistency checking purpose.
     * This is designed for testing; do not use this in production when there
     * are ongoing transactions.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse> hash(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_HASH, getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MaintenanceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MaintenanceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ALARM:
          serviceImpl.alarm((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AlarmResponse>) responseObserver);
          break;
        case METHODID_STATUS:
          serviceImpl.status((io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.StatusResponse>) responseObserver);
          break;
        case METHODID_DEFRAGMENT:
          serviceImpl.defragment((io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DefragmentResponse>) responseObserver);
          break;
        case METHODID_HASH:
          serviceImpl.hash((io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.HashResponse>) responseObserver);
          break;
        case METHODID_SNAPSHOT:
          serviceImpl.snapshot((io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.SnapshotResponse>) responseObserver);
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

  private static abstract class MaintenanceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MaintenanceBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.shardingjdbc.orchestration.reg.etcd.internal.stub.EtcdProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Maintenance");
    }
  }

  private static final class MaintenanceFileDescriptorSupplier
      extends MaintenanceBaseDescriptorSupplier {
    MaintenanceFileDescriptorSupplier() {}
  }

  private static final class MaintenanceMethodDescriptorSupplier
      extends MaintenanceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MaintenanceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
