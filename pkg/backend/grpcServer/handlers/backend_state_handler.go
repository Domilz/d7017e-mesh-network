package handlers

import (
	"fmt"
	"log"
	"sync"

	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"google.golang.org/protobuf/proto"
)

type BackendStateHandler struct {
	TagId                string
	readingsMap          map[string]*pb.Reading
	mutex                sync.RWMutex
	directHandler        *DirectHandler
	indirectHandler      *IndirectHandler
	stateDatabaseHandler *StateDatabaseHandler
}

func (stateHandler *BackendStateHandler) InitStateHandler(id string, sLogServer *sentLog.SentLogServer, sDatabaseHandler *StateDatabaseHandler) {
	stateHandler.lock()
	stateHandler.directHandler = InitDirectHandler(sLogServer)
	stateHandler.indirectHandler = InitIndirectHandler(sLogServer)
	sLogServer.SetReferencePointChache(stateHandler.indirectHandler.rpCache)
	sLogServer.StartGUIPlotter()
	stateHandler.TagId = id
	stateHandler.readingsMap = make(map[string]*pb.Reading)
	stateHandler.stateDatabaseHandler = sDatabaseHandler
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

func (stateHandler *BackendStateHandler) GetState() *pb.State {

	stateHandler.lock()
	s := pb.State{TagId: stateHandler.TagId}
	for _, reading := range stateHandler.readingsMap {
		s.Readings = append(s.Readings, reading)
	}

	stateHandler.unLock()
	return &s
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
	log.Printf("InsertMultipleReadings call with tagId :  %v", state.TagId)
	for _, reading := range state.Readings {
		stateHandler.InsertSingleReading(reading)

	}

}

func (stateHandler *BackendStateHandler) InsertSingleReading(reading *pb.Reading) {
	stateHandler.lock()
	value, keyExist := stateHandler.readingsMap[reading.TagId]

	if !keyExist || findLatestTimestamp(value, reading) {
		stateHandler.stateDatabaseHandler.Save(reading)
		stateHandler.readingsMap[reading.TagId] = reading
		if reading.IsDirect == 0 {
			println("Recieved indirect reading for tag: ", reading.TagId)
			//Send to indirectHandler in go routine
			go stateHandler.indirectHandler.FillOutAndSendForm(reading)
		} else if reading.IsDirect == 1 {

			go stateHandler.directHandler.FillOutAndSendForm(reading)

		}

	} else {
		fmt.Println("Old reading recived for tag: ", reading.TagId, " exist: ", keyExist)
	}
	stateHandler.unLock()
}

func findLatestTimestamp(reading *pb.Reading, otherReading *pb.Reading) bool {
	return reading.Ts.Seconds <= otherReading.Ts.Seconds
}

func (stateHandler *BackendStateHandler) insertOne(reading *pb.Reading) {
	stateHandler.lock()
	stateHandler.readingsMap[reading.TagId] = reading
	stateHandler.unLock()

}

func (stateHandler *BackendStateHandler) InsertDataFromDB(readings []pb.Reading) {

	for i := 0; i < len(readings); i++ {
		stateHandler.insertOne(&readings[i])
	}

}
