package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
)

func main() {
	go StartGrpcServer()
	StartDebugLog()
	select {}
}
func StartGrpcServer() {
	server.StartGrpcServer()
}
func StartDebugLog() {
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")
}
