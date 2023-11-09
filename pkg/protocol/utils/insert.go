package utils

import (
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
)

func (stateHandler *StateHandler) InsertMultipleReadings(state *pb.State) {
	selfReading, _ := stateHandler.readingsMap[stateHandler.TagId]
	for _, reading := range state.Readings {
		if reading.TagId == state.TagId && selfReading != nil {
			if findLatestTimestamp(selfReading, reading) {
				selfReading.IsDirect = 0
				selfReading.RpId = reading.RpId
				selfReading.Rssi = reading.Rssi
				selfReading.Ts = reading.Ts
			} else {
				reading.IsDirect = 0
				reading.RpId = selfReading.RpId
				reading.Rssi = selfReading.Rssi
				reading.Ts = selfReading.Ts
			}
		}

		stateHandler.InsertSingleReading(reading)

	}

}

func (stateHandler *StateHandler) InsertSingleReading(reading *pb.Reading) {
	stateHandler.lock()
	value, keyExist := stateHandler.readingsMap[reading.TagId]

	if !keyExist || findLatestTimestamp(value, reading) {

		stateHandler.readingsMap[reading.TagId] = reading

	}
	stateHandler.unLock()
}

func (stateHandler *StateHandler) InsertSerializedState(serializedState []byte) error {
	state, err := DeserializeState(serializedState)
	if err != nil {
		return err
	}
	stateHandler.InsertMultipleReadings(state)
	return nil

}

func findLatestTimestamp(reading *pb.Reading, otherReading *pb.Reading) bool {
	return reading.Ts.Seconds <= otherReading.Ts.Seconds
}
