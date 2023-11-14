package backend

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
)

var backendPath = "pkg/backend/"

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
	debugLog.StartDebugLogServer(backendPath+"debugLog/database/DebugLogDatabase.db", backendPath+"debugLog/debugLogFormat.html")
}

func StartSentLog() *sentLog.SentLogServer {
	sentLogServer := sentLog.StartSentLogServer(backendPath+"sentLog/database/SentLogDatabase.db", backendPath+"sentLog/sentLogFormat.html")
	return sentLogServer

}

func StartGRPCServer(sLogServer *sentLog.SentLogServer) {

	server.StartGrpcServer(sLogServer, backendPath+"stateLog/database/state.db", backendPath+"stateLog/stateLogFormat.html")

}
