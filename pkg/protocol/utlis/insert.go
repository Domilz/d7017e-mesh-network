package utlis

import (
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/pb"
)

func InsertMultipleReadings(readings map[string]*pb.Reading, state *pb.State) {
	for _, reading := range readings {
		InsertSingleReading(state, reading)
	}

}

func InsertSingleReading(state *pb.State, reading *pb.Reading) {
	_, key := state.Readings[reading.TagId]

	if !key || findLatestTimestamp(state.Readings[reading.TagId], reading) {
		state.Readings[reading.TagId] = reading
	}
}

func findLatestTimestamp(reading *pb.Reading, otherReading *pb.Reading) bool {
	return reading.Ts.Seconds <= otherReading.Ts.Seconds
}
