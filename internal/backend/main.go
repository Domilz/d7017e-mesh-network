package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
	//server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
)

func main() {
	//server.StartGrpcServer("Server Tag")
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")

	select {}
}
