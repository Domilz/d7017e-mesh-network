package main

import (
	"fmt"

	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/pb"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func main() {

	mockReading := &pb.Reading{
		TagId:    "00-B0-D0-63-C2-27",
		DeviceId: "321",
		Rssi:     5,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}
	// mockReading2 := &pb.Reading{
	// 	TagId:    "00-B0-D0-63-C2-28",
	// 	DeviceId: "321",
	// 	Rssi:     5,
	// 	Ts: &timestamp.Timestamp{
	// 		Seconds: timestamppb.Now().Seconds,
	// 	},
	// }
	// mockReading3 := &pb.Reading{
	// 	TagId:    "10-B0-D0-63-C2-26",
	// 	DeviceId: "321",
	// 	Rssi:     5,
	// 	Ts: &timestamp.Timestamp{
	// 		Seconds: timestamppb.Now().Seconds,
	// 	},
	// }

	mockState := &pb.State{
		TagId:    "666",
		Readings: map[string]*pb.Reading{},
	}

	//fmt.Println("Testing a mock state: ", mockState)
	_, err := proto.Marshal(mockReading)
	if err != nil {
		fmt.Println("marshall error", err)
	}

	fmt.Println(mockState.Readings)
	fmt.Println(proto.Size(mockState))

}
