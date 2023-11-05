package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
	sentLog "github.com/Domilz/d7017e-mesh-network/internal/backend/sentLog"
)

func main() {
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

	server.StartGrpcServer(sLogServer)

}
