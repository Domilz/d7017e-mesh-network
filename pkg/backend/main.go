package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer"
)

func main() {
	StartGrpcServer()
	select {}
}
func StartGrpcServer() {
	server.StartGrpcServer()
}
func StartDebugLog() {
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")
}