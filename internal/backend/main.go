package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
	server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
	sentLog "github.com/Domilz/d7017e-mesh-network/internal/backend/sentLog"
)

func main() {
	go StartGrpcServer()
	StartGRPCServerAndSentLog()
	//s := StartSentLog()
	//f := sentLog.CreateRssiForm("testid")
	//s.SaveRssiForm(*f)
	select {}
}
func StartGrpcServer() {

}
func StartDebugLog() {
	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")
}

func StartGRPCServerAndSentLog() *sentLog.SentLogServer {
	s := sentLog.StartSentLogServer("sentLog/database/SentLogDatabase.db", "./sentLog/sentLogFormat.html")
	server.StartGrpcServer(s.SentLogDatabaseHandler)
	return s
}
