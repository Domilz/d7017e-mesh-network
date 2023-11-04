package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
	sentLog "github.com/Domilz/d7017e-mesh-network/internal/backend/sentLog"
)

func main() {
	//StartGrpcServer()
	StartSentLog()
	select {}
}
func StartGrpcServer() {
	server.StartGrpcServer()
}
func StartDebugLog() {
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")
}

func StartSentLog() {
	sentLog.StartSentLogServer("debugLog/database/DebugLogDatabase.db", "./sentLog/sentLogFormat.html")
}
