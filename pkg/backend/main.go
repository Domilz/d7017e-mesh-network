package backend

import (
	"fmt"

	debugLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/debugLog"
	handler "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/handlers"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func Main() {
	StartBackend()
	select {}
}

func StartBackend() {
	StartDebugLog()
	sentLogServer := StartSentLog()
	StartGRPCServer(sentLogServer)

}

func StartDebugLog() {
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")
}

func StartSentLog() *sentLog.SentLogServer {
	sentLogServer := sentLog.StartSentLogServer("sentLog/database/SentLogDatabase.db", "./sentLog/sentLogFormat.html")
	return sentLogServer

}

func StartGRPCServer(sLogServer *sentLog.SentLogServer) {

	//server.StartGrpcServer(sLogServer)

}

func TestStateDb() {

	stateDB := handler.InitStateDatabase("grpcServer/database_state/state.db")
	mockReading := &pb.Reading{
		TagId: "MOCKTAG",
		RpId:  "321",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}
	stateDB.Save(mockReading)
	s := stateDB.LoadFromDB()
	fmt.Println(s)

}
