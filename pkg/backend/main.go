package backend

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
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

	server.StartGrpcServer(sLogServer, "grpcServer/database_state/state.db")

}
