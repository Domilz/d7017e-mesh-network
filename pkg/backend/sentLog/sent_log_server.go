package sentLog

import (
	"encoding/json"
	"html/template"
	"log"
	"net/http"

	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
)

var sentLogServer *SentLogServer

type SentLogServer struct {
	SentLogDatabaseHandler *SentLogDatabaseHandler
	HTMLFormatPath         string
}

type SentLogCollection struct {
	Items []SentLogData
}

type FormInterface interface {
}

func StartSentLogServer(dbPath string, htmlFormatPath string) *SentLogServer {

	sLogServer := &SentLogServer{InitSentLogDatabase(dbPath), htmlFormatPath}

	sentLogServer = sLogServer
	http.HandleFunc("/sentlog", SentLog)
	log.Printf("Starting sentLog server")
	go http.ListenAndServe(":4242", nil)
	return sentLogServer
}

func SentLog(w http.ResponseWriter, req *http.Request) {
	switch req.Method {
	case "GET":
		sentLogCollection := SentLogCollection{Items: sentLogServer.SentLogDatabaseHandler.GetSentLog()}
		tmpl, _ := template.ParseFiles(sentLogServer.HTMLFormatPath)

		tmpl.Execute(w, sentLogCollection)

	default:
		log.Printf("Got a request for a unsupported method : %v", req.Method)

	}
}

func (sentLogServer *SentLogServer) SaveRssiForm(form structs.RssiForm) {
	jsonData, err := json.Marshal(form)
	if err != nil {
		log.Printf("SentLogServer encountered a during json marshal at SaveRssiForm")

	} else {
		jsonString := string(jsonData)
		sentLogServer.SentLogDatabaseHandler.saveToDB("RssiForm", len(form.Readings), jsonString)
	}

}

func (sentLogServer *SentLogServer) SaveReferencePointForm(form structs.ReferencePointForm) {
	jsonData, err := json.Marshal(form)
	if err != nil {
		log.Printf("SentLogServer encountered a during json marshal at SaveReferencePointForm")
	} else {
		jsonString := string(jsonData)
		sentLogServer.SentLogDatabaseHandler.saveToDB("ReferencePointForm", len(form.Operands), jsonString)
	}
}
