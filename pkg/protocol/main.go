package main

import (
	"fmt"

	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/pb"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func main() {

	mockReading := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId: mockReading,
		},
	}

	fmt.Println("Testing a mock state: ", mockState)

}
