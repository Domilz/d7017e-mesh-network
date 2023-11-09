package utils

import (
	"sync"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type StateHandler struct {
	TagId       string
	readingsMap map[string]*pb.Reading
	mutex       sync.RWMutex
}

func (stateHandler *StateHandler) InitStateHandler(id string) {
	stateHandler.lock()
	stateHandler.TagId = id
	stateHandler.readingsMap = make(map[string]*pb.Reading)
	stateHandler.unLock()
}

func (stateHandler *StateHandler) lock() {
	stateHandler.mutex.Lock()
}

func (stateHandler *StateHandler) unLock() {
	stateHandler.mutex.Unlock()
}

func (stateHandler *StateHandler) GetReading(id string) *pb.Reading {
	stateHandler.lock()
	r := stateHandler.readingsMap[id]
	stateHandler.unLock()
	return r
}

func (stateHandler *StateHandler) GetState() *pb.State {
	stateHandler.lock()
	s := &pb.State{TagId: stateHandler.TagId}
	for _, reading := range stateHandler.readingsMap {
		s.Readings = append(s.Readings, reading)
	}

	stateHandler.unLock()
	return s
}

func (stateHandler *StateHandler) GetSerializedState() ([]byte, error) {

	stateHandler.lock()
	s := pb.State{TagId: stateHandler.TagId}
	for _, reading := range stateHandler.readingsMap {
		s.Readings = append(s.Readings, reading)
	}

	serializedState, err := SerializeState(&s)
	if err != nil {
		return nil, err
	}

	stateHandler.unLock()
	return serializedState, nil
}

func SerializeState(state *pb.State) ([]byte, error) {
	marshaledState, err := proto.Marshal(state)
	if err != nil {
		return nil, err
	}

	return marshaledState, nil
}

func DeserializeState(stateArray []byte) (*pb.State, error) {
	stateMessage := &pb.State{}
	err := proto.Unmarshal(stateArray, stateMessage)
	if err != nil {
		return nil, err
	}

	return stateMessage, nil
}

func (stateHandler *StateHandler) UpdateReadingofSelf(rpId string, rssi int32) {
	stateHandler.lock()
	r := &pb.Reading{TagId: stateHandler.TagId, RpId: rpId, Rssi: rssi, Ts: timestamppb.Now(), IsDirect: 1}
	stateHandler.readingsMap[stateHandler.TagId] = r
	stateHandler.unLock()

}
