// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.28.1
// 	protoc        v4.24.3
// source: tag.proto

package __

import (
	timestamp "github.com/golang/protobuf/ptypes/timestamp"
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

type State struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	TagId string `protobuf:"bytes,1,opt,name=tag_id,json=tagId,proto3" json:"tag_id,omitempty"` // ID of tag
	Readings []*Reading `protobuf:"bytes,2,rep,name=readings,proto3" json:"readings,omitempty"` // List of readings made by reporter
}

func (x *State) Reset() {
	*x = State{}
	if protoimpl.UnsafeEnabled {
		mi := &file_Tag_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *State) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*State) ProtoMessage() {}

func (x *State) ProtoReflect() protoreflect.Message {
	mi := &file_Tag_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use State.ProtoReflect.Descriptor instead.
func (*State) Descriptor() ([]byte, []int) {
	return file_Tag_proto_rawDescGZIP(), []int{0}
}

func (x *State) GetTagId() string {
	if x != nil {
		return x.TagId
	}
	return ""
}

func (x *State) GetReadings() []*Reading {
	if x != nil {
		return x.Readings
	}
	return nil
}

type Reading struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	TagId    string               `protobuf:"bytes,1,opt,name=tag_id,json=tagId,proto3" json:"tag_id,omitempty"`          // ID of the tag
	DeviceId string               `protobuf:"bytes,2,opt,name=device_id,json=deviceId,proto3" json:"device_id,omitempty"` // ID of the reported device
	Rssi     int32                `protobuf:"varint,3,opt,name=rssi,proto3" json:"rssi,omitempty"`                        // Received signal strength indicator
	Ts       *timestamp.Timestamp `protobuf:"bytes,4,opt,name=ts,proto3" json:"ts,omitempty"`                             // Time reading was received
}

func (x *Reading) Reset() {
	*x = Reading{}
	if protoimpl.UnsafeEnabled {
		mi := &file_Tag_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Reading) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Reading) ProtoMessage() {}

func (x *Reading) ProtoReflect() protoreflect.Message {
	mi := &file_Tag_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Reading.ProtoReflect.Descriptor instead.
func (*Reading) Descriptor() ([]byte, []int) {
	return file_Tag_proto_rawDescGZIP(), []int{1}
}

func (x *Reading) GetTagId() string {
	if x != nil {
		return x.TagId
	}
	return ""
}

func (x *Reading) GetDeviceId() string {
	if x != nil {
		return x.DeviceId
	}
	return ""
}

func (x *Reading) GetRssi() int32 {
	if x != nil {
		return x.Rssi
	}
	return 0
}

func (x *Reading) GetTs() *timestamp.Timestamp {
	if x != nil {
		return x.Ts
	}
	return nil
}

var File_Tag_proto protoreflect.FileDescriptor

var file_Tag_proto_rawDesc = []byte{
	0x0a, 0x09, 0x54, 0x61, 0x67, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12, 0x08, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x63, 0x6f, 0x6c, 0x1a, 0x1f, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0x4d, 0x0a, 0x05, 0x53, 0x74, 0x61, 0x74, 0x65, 0x12,
	0x15, 0x0a, 0x06, 0x74, 0x61, 0x67, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52,
	0x05, 0x74, 0x61, 0x67, 0x49, 0x64, 0x12, 0x2d, 0x0a, 0x08, 0x72, 0x65, 0x61, 0x64, 0x69, 0x6e,
	0x67, 0x73, 0x18, 0x02, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x11, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0x63, 0x6f, 0x6c, 0x2e, 0x52, 0x65, 0x61, 0x64, 0x69, 0x6e, 0x67, 0x52, 0x08, 0x72, 0x65, 0x61,
	0x64, 0x69, 0x6e, 0x67, 0x73, 0x22, 0x7d, 0x0a, 0x07, 0x52, 0x65, 0x61, 0x64, 0x69, 0x6e, 0x67,
	0x12, 0x15, 0x0a, 0x06, 0x74, 0x61, 0x67, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09,
	0x52, 0x05, 0x74, 0x61, 0x67, 0x49, 0x64, 0x12, 0x1b, 0x0a, 0x09, 0x64, 0x65, 0x76, 0x69, 0x63,
	0x65, 0x5f, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x08, 0x64, 0x65, 0x76, 0x69,
	0x63, 0x65, 0x49, 0x64, 0x12, 0x12, 0x0a, 0x04, 0x72, 0x73, 0x73, 0x69, 0x18, 0x03, 0x20, 0x01,
	0x28, 0x05, 0x52, 0x04, 0x72, 0x73, 0x73, 0x69, 0x12, 0x2a, 0x0a, 0x02, 0x74, 0x73, 0x18, 0x04,
	0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70,
	0x52, 0x02, 0x74, 0x73, 0x32, 0x49, 0x0a, 0x10, 0x53, 0x74, 0x61, 0x74, 0x65, 0x50, 0x72, 0x6f,
	0x70, 0x61, 0x67, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x12, 0x35, 0x0a, 0x0b, 0x50, 0x72, 0x6f, 0x70,
	0x61, 0x67, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x12, 0x0f, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x63,
	0x6f, 0x6c, 0x2e, 0x53, 0x74, 0x61, 0x74, 0x65, 0x1a, 0x0f, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0x63, 0x6f, 0x6c, 0x2e, 0x53, 0x74, 0x61, 0x74, 0x65, 0x22, 0x00, 0x28, 0x01, 0x30, 0x01, 0x42,
	0x03, 0x5a, 0x01, 0x2f, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_Tag_proto_rawDescOnce sync.Once
	file_Tag_proto_rawDescData = file_Tag_proto_rawDesc
)

func file_Tag_proto_rawDescGZIP() []byte {
	file_Tag_proto_rawDescOnce.Do(func() {
		file_Tag_proto_rawDescData = protoimpl.X.CompressGZIP(file_Tag_proto_rawDescData)
	})
	return file_Tag_proto_rawDescData
}

var file_tag_proto_msgTypes = make([]protoimpl.MessageInfo, 2)
var file_tag_proto_goTypes = []interface{}{
	(*State)(nil),                 // 0: protocol.State
	(*Reading)(nil),               // 1: protocol.Reading
	(*timestamppb.Timestamp)(nil), // 2: google.protobuf.Timestamp
}
var file_tag_proto_depIdxs = []int32{
	1, // 0: protocol.State.readings:type_name -> protocol.Reading
	2, // 1: protocol.Reading.ts:type_name -> google.protobuf.Timestamp
	0, // 2: protocol.StatePropagation.Propagation:input_type -> protocol.State
	0, // 3: protocol.StatePropagation.Propagation:output_type -> protocol.State
	3, // [3:4] is the sub-list for method output_type
	2, // [2:3] is the sub-list for method input_type
	2, // [2:2] is the sub-list for extension type_name
	2, // [2:2] is the sub-list for extension extendee
	0, // [0:2] is the sub-list for field type_name
}

func init() { file_Tag_proto_init() }
func file_Tag_proto_init() {
	if File_Tag_proto != nil {
		return
	}
	if !protoimpl.UnsafeEnabled {
		file_Tag_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*State); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_Tag_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Reading); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_Tag_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   2,
			NumExtensions: 0,
			NumServices:   1,
		},
		GoTypes:           file_Tag_proto_goTypes,
		DependencyIndexes: file_Tag_proto_depIdxs,
		MessageInfos:      file_Tag_proto_msgTypes,
	}.Build()
	File_Tag_proto = out.File
	file_Tag_proto_rawDesc = nil
	file_Tag_proto_goTypes = nil
	file_Tag_proto_depIdxs = nil
}
