package handlers

import (
	"fmt"
	"sort"
	"sync"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"google.golang.org/protobuf/proto"
)

type BackendStateHandler struct {
	TagId       string
	readingsMap map[string]*pb.Reading
	mutex       sync.RWMutex
}

func (stateHandler *BackendStateHandler) InitStateHandler(id string) {
	stateHandler.lock()
	stateHandler.TagId = id
	stateHandler.readingsMap = make(map[string]*pb.Reading)
	stateHandler.unLock()
}

func (stateHandler *BackendStateHandler) lock() {
	stateHandler.mutex.Lock()
}

func (stateHandler *BackendStateHandler) unLock() {
	stateHandler.mutex.Unlock()
}

func (stateHandler *BackendStateHandler) GetReading(id string) *pb.Reading {
	stateHandler.lock()
	r := stateHandler.readingsMap[id]
	stateHandler.unLock()
	return r
}

func (stateHandler *BackendStateHandler) GetState() ([]byte, error) {

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

func (stateHandler *BackendStateHandler) getStateSorted() (*pb.State, error) {
	serializedState, err := stateHandler.GetState()
	if err != nil {
		return nil, err
	}

	state, err := DeserializeState(serializedState)
	if err != nil {
		return nil, err
	}

	sort.SliceStable(state.Readings, func(i, j int) bool {
		return state.Readings[i].TagId < state.Readings[j].TagId
	})
	return state, nil

}

func (stateHandler *BackendStateHandler) getStatesReadingLimit(limit int) ([]*pb.State, error) {

	states := []*pb.State{}
	stateWhole, err := stateHandler.getStateSorted() //Not sure how to implement a test for GetStatesReadingLimit if this is not sorted (using the GetState function)
	if err != nil {
		return nil, err
	}
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
	return states, nil

}

func SerializeState(state *pb.State) ([]byte, error) {
	marshaledState, err := proto.Marshal(state)
	if err == nil {
		return marshaledState, nil
	}

	return nil, err
}

func DeserializeState(stateArray []byte) (*pb.State, error) {
	stateMessage := &pb.State{}
	err := proto.Unmarshal(stateArray, stateMessage)
	if err == nil {
		return stateMessage, nil
	}

	return nil, err
}

func (stateHandler *BackendStateHandler) InsertMultipleReadings(state *pb.State) {
	for _, reading := range state.Readings {
		stateHandler.InsertSingleReading(reading)

	}

}

func (stateHandler *BackendStateHandler) InsertSingleReading(reading *pb.Reading) {
	stateHandler.lock()
	value, keyExist := stateHandler.readingsMap[reading.TagId]

	if !keyExist || findLatestTimestamp(value, reading) {

		stateHandler.readingsMap[reading.TagId] = reading
		if reading.IsDirect == 0 {
			println("Recieved indirect")
			//Send to indirectHandler in go routine
		} else if reading.IsDirect == 1 {
			println("Recieved direct")
			//Send to directHandler in go routine
		}

	}
	stateHandler.unLock()
}

func findLatestTimestamp(reading *pb.Reading, otherReading *pb.Reading) bool {
	return reading.Ts.Seconds <= otherReading.Ts.Seconds
}
