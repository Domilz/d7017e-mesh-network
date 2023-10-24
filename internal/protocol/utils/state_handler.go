package utils

import (
	"fmt"
	"sort"
	"sync"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
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
	s := pb.State{TagId: stateHandler.TagId}
	for _, reading := range stateHandler.readingsMap {
		s.Readings = append(s.Readings, reading)
	}
	stateHandler.unLock()
	return &s
}

func (stateHandler *StateHandler) getStateSorted() *pb.State {
	state := stateHandler.GetState()
	sort.SliceStable(state.Readings, func(i, j int) bool {
		return state.Readings[i].TagId < state.Readings[j].TagId
	})
	return state

}

func (stateHandler *StateHandler) getStatesReadingLimit(limit int) []*pb.State {

	states := []*pb.State{}
	stateWhole := stateHandler.getStateSorted() //Not sure how to implement a test for GetStatesReadingLimit if this is not sorted (using the GetState function)
	stateChunk := &pb.State{TagId: stateWhole.TagId, Readings: []*pb.Reading{}}
	i := 0
	for _, reading := range stateWhole.Readings {
		stateChunk.Readings = append(stateChunk.Readings, reading)
		fmt.Println(reading)
		i++
		if i >= limit {
			states = append(states, stateChunk)
			stateChunk = &pb.State{TagId: stateWhole.TagId, Readings: []*pb.Reading{}}
			i = 0
		}

	}
	if len(stateChunk.Readings) != 0 {
		states = append(states, stateChunk)
	}
	return states

}
