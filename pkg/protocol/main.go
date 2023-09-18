package main

import (
	"fmt"

	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/protobuffer"
	"github.com/golang/protobuf/ptypes/timestamp"
)

func main() {

	mockReading := &protobuffer.Reading{
		TagId:    "123",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: 666,
			Nanos:   999,
		},
	}

	mockState := &protobuffer.State{
		TagId: "666",
		Readings: []*protobuffer.Reading{
			mockReading,
		},
	}

	fmt.Println("Testing a mock state: ", mockState)

}
