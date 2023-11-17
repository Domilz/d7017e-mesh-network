package sentLog

import (
	"encoding/json"
	"fmt"
	"html/template"
	"log"
	"net/http"

	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
	guiPlotter "github.com/Domilz/d7017e-mesh-network/pkg/backend/guiPlotter"
)

var sentLogServer *SentLogServer

type SentLogServer struct {
	SentLogDatabaseHandler *SentLogDatabaseHandler
	HTMLFormatPath         string
	GuiPlotter             *guiPlotter.GUIPlotter
}

type SentLogCollection struct {
	Items []SentLogData
}

type FormInterface interface {
}

func StartSentLogServer(dbPath string, htmlFormatPath string) *SentLogServer {

	sLogServer := &SentLogServer{InitSentLogDatabase(dbPath), htmlFormatPath, guiPlotter.StartGUIPlotter()}

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

		//SendTagUpdate(tagId string, rpId string, accuracy int, date string, readingType string, x int, y int, z int)
		jsonString := string(jsonData)
		fmt.Println("before send")
		sentLogServer.SentLogDatabaseHandler.saveToDB("RssiForm", len(form.Readings), jsonString)
		for i := 0; i < len(form.Readings); i++ {
			fmt.Println("sending")
			guiPlotter.SendTagUpdate(form.Readings[i].Tag_id, form.Readings[i].Rp_id, 0, "someTime", "directReading", 0, 0, 0)
		}
	}

}

func (sentLogServer *SentLogServer) SaveReferencePointForm(form structs.ReferencePointForm) {
	jsonData, err := json.Marshal(form)
	if err != nil {
		log.Printf("SentLogServer encountered a during json marshal at SaveReferencePointForm")
	} else {
		jsonString := string(jsonData)
		sentLogServer.SentLogDatabaseHandler.saveToDB("ReferencePointForm", len(form.Operands), jsonString)
		for i := 0; i < len(form.Operands); i++ {
			guiPlotter.SendTagUpdate(form.Operands[i].Uuid, form.Operands[i].RpId, 0, "someTime", "indirectReading", int(form.Operands[i].Location.X), int(form.Operands[i].Location.Y), int(form.Operands[i].Location.Z))
		}
	}
}
