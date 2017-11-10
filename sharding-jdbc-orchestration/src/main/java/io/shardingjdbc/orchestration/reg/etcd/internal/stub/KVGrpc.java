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
public final class KVGrpc {

  public static final String SERVICE_NAME = "etcdserverpb.KV";
  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse> METHOD_RANGE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.KV", "Range"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new KVMethodDescriptorSupplier("Range"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse> METHOD_PUT =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.KV", "Put"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse.getDefaultInstance()))
          .setSchemaDescriptor(new KVMethodDescriptorSupplier("Put"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse> METHOD_DELETE_RANGE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.KV", "DeleteRange"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new KVMethodDescriptorSupplier("DeleteRange"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse> METHOD_TXN =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.KV", "Txn"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse.getDefaultInstance()))
          .setSchemaDescriptor(new KVMethodDescriptorSupplier("Txn"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse> METHOD_COMPACT =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.KV", "Compact"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse.getDefaultInstance()))
          .setSchemaDescriptor(new KVMethodDescriptorSupplier("Compact"))
          .build();
  private static final int METHODID_RANGE = 0;
  private static final int METHODID_PUT = 1;
  private static final int METHODID_DELETE_RANGE = 2;
  private static final int METHODID_TXN = 3;
  private static final int METHODID_COMPACT = 4;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private KVGrpc() {}

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KVStub newStub(io.grpc.Channel channel) {
    return new KVStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KVBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new KVBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KVFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new KVFutureStub(channel);
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (KVGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KVFileDescriptorSupplier())
              .addMethod(METHOD_RANGE)
              .addMethod(METHOD_PUT)
              .addMethod(METHOD_DELETE_RANGE)
              .addMethod(METHOD_TXN)
              .addMethod(METHOD_COMPACT)
              .build();
        }
      }
    }
    return result;
  }

  /**
   */
  public static abstract class KVImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest request,
                      io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_RANGE, responseObserver);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest request,
                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PUT, responseObserver);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DELETE_RANGE, responseObserver);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest request,
                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TXN, responseObserver);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_COMPACT, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_RANGE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse>(
                  this, METHODID_RANGE)))
          .addMethod(
            METHOD_PUT,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            METHOD_DELETE_RANGE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse>(
                  this, METHODID_DELETE_RANGE)))
          .addMethod(
            METHOD_TXN,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse>(
                  this, METHODID_TXN)))
          .addMethod(
            METHOD_COMPACT,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse>(
                  this, METHODID_COMPACT)))
          .build();
    }
  }

  /**
   */
  public static final class KVStub extends io.grpc.stub.AbstractStub<KVStub> {
    private KVStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KVStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected KVStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest request,
                      io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RANGE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest request,
                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PUT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE_RANGE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest request,
                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TXN, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COMPACT, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class KVBlockingStub extends io.grpc.stub.AbstractStub<KVBlockingStub> {
    private KVBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KVBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected KVBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse range(io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RANGE, getCallOptions(), request);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse put(io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PUT, getCallOptions(), request);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse deleteRange(io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE_RANGE, getCallOptions(), request);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse txn(io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TXN, getCallOptions(), request);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse compact(io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COMPACT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class KVFutureStub extends io.grpc.stub.AbstractStub<KVFutureStub> {
    private KVFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private KVFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected KVFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse> range(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RANGE, getCallOptions()), request);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse> put(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PUT, getCallOptions()), request);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse> deleteRange(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE_RANGE, getCallOptions()), request);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse> txn(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TXN, getCallOptions()), request);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse> compact(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COMPACT, getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final KVImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(KVImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RANGE:
          serviceImpl.range((io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.RangeResponse>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.PutResponse>) responseObserver);
          break;
        case METHODID_DELETE_RANGE:
          serviceImpl.deleteRange((io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.DeleteRangeResponse>) responseObserver);
          break;
        case METHODID_TXN:
          serviceImpl.txn((io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.TxnResponse>) responseObserver);
          break;
        case METHODID_COMPACT:
          serviceImpl.compact((io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.CompactionResponse>) responseObserver);
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

  private static abstract class KVBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    KVBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.shardingjdbc.orchestration.reg.etcd.internal.stub.EtcdProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("KV");
    }
  }

  private static final class KVFileDescriptorSupplier
      extends KVBaseDescriptorSupplier {
    KVFileDescriptorSupplier() {}
  }

  private static final class KVMethodDescriptorSupplier
      extends KVBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    KVMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
