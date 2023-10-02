package utils

import (
	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
)

func (stateHandler *StateHandler) InsertMultipleReadings(state *pb.State) {
	for _, reading := range state.Readings {
		stateHandler.InsertSingleReading(reading)
	}

}

func (stateHandler *StateHandler) InsertSingleReading(reading *pb.Reading) {
	stateHandler.lock()
	_, key := stateHandler.readingsMap[reading.TagId]

	if !key || findLatestTimestamp(stateHandler.readingsMap[reading.TagId], reading) {

		stateHandler.readingsMap[reading.TagId] = reading

	}
	stateHandler.unLock()
}

func findLatestTimestamp(reading *pb.Reading, otherReading *pb.Reading) bool {
	return reading.Ts.Seconds <= otherReading.Ts.Seconds
}
