package utils

import (
	"sync"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
)

type StateHandler struct {
	TagId       string
	readingsMap map[string]*pb.Reading
	mutex       sync.RWMutex
}

func (stateHandler *StateHandler) initStateHandler(id string) {
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

func (stateHandler *StateHandler) getReading(id string) *pb.Reading {
	stateHandler.lock()
	r := stateHandler.readingsMap[id]
	stateHandler.unLock()
	return r
}

func (stateHandler *StateHandler) getState() *pb.State {
	s := pb.State{TagId: stateHandler.TagId}

	stateHandler.lock()
	for _, reading := range stateHandler.readingsMap {
		s.Readings = append(s.Readings, reading)
	}
	stateHandler.unLock()
	return &s
}
