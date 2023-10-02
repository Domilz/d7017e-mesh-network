// Code generated by protoc-gen-go-grpc. DO NOT EDIT.
// versions:
// - protoc-gen-go-grpc v1.2.0
// - protoc             v4.24.3
// source: tag.proto

package __

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.32.0 or later.
const _ = grpc.SupportPackageIsVersion7

// StatePropogationClient is the client API for StatePropogation service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type StatePropogationClient interface {
	Propogation(ctx context.Context, opts ...grpc.CallOption) (StatePropogation_PropogationClient, error)
}

type statePropogationClient struct {
	cc grpc.ClientConnInterface
}

func NewStatePropogationClient(cc grpc.ClientConnInterface) StatePropogationClient {
	return &statePropogationClient{cc}
}

func (c *statePropogationClient) Propogation(ctx context.Context, opts ...grpc.CallOption) (StatePropogation_PropogationClient, error) {
	stream, err := c.cc.NewStream(ctx, &StatePropogation_ServiceDesc.Streams[0], "/protocol.StatePropogation/Propogation", opts...)
	if err != nil {
		return nil, err
	}
	x := &statePropogationPropogationClient{stream}
	return x, nil
}

type StatePropogation_PropogationClient interface {
	Send(*State) error
	Recv() (*State, error)
	grpc.ClientStream
}

type statePropogationPropogationClient struct {
	grpc.ClientStream
}

func (x *statePropogationPropogationClient) Send(m *State) error {
	return x.ClientStream.SendMsg(m)
}

func (x *statePropogationPropogationClient) Recv() (*State, error) {
	m := new(State)
	if err := x.ClientStream.RecvMsg(m); err != nil {
		return nil, err
	}
	return m, nil
}

// StatePropogationServer is the server API for StatePropogation service.
// All implementations must embed UnimplementedStatePropogationServer
// for forward compatibility
type StatePropogationServer interface {
	Propogation(StatePropogation_PropogationServer) error
	mustEmbedUnimplementedStatePropogationServer()
}

// UnimplementedStatePropogationServer must be embedded to have forward compatible implementations.
type UnimplementedStatePropogationServer struct {
}

func (UnimplementedStatePropogationServer) Propogation(StatePropogation_PropogationServer) error {
	return status.Errorf(codes.Unimplemented, "method Propogation not implemented")
}
func (UnimplementedStatePropogationServer) mustEmbedUnimplementedStatePropogationServer() {}

// UnsafeStatePropogationServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to StatePropogationServer will
// result in compilation errors.
type UnsafeStatePropogationServer interface {
	mustEmbedUnimplementedStatePropogationServer()
}

func RegisterStatePropogationServer(s grpc.ServiceRegistrar, srv StatePropogationServer) {
	s.RegisterService(&StatePropogation_ServiceDesc, srv)
}

func _StatePropogation_Propogation_Handler(srv interface{}, stream grpc.ServerStream) error {
	return srv.(StatePropogationServer).Propogation(&statePropogationPropogationServer{stream})
}

type StatePropogation_PropogationServer interface {
	Send(*State) error
	Recv() (*State, error)
	grpc.ServerStream
}

type statePropogationPropogationServer struct {
	grpc.ServerStream
}

func (x *statePropogationPropogationServer) Send(m *State) error {
	return x.ServerStream.SendMsg(m)
}

func (x *statePropogationPropogationServer) Recv() (*State, error) {
	m := new(State)
	if err := x.ServerStream.RecvMsg(m); err != nil {
		return nil, err
	}
	return m, nil
}

// StatePropogation_ServiceDesc is the grpc.ServiceDesc for StatePropogation service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var StatePropogation_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "protocol.StatePropogation",
	HandlerType: (*StatePropogationServer)(nil),
	Methods:     []grpc.MethodDesc{},
	Streams: []grpc.StreamDesc{
		{
			StreamName:    "Propogation",
			Handler:       _StatePropogation_Propogation_Handler,
			ServerStreams: true,
			ClientStreams: true,
		},
	},
	Metadata: "tag.proto",
}
