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
	referencePointCache    *ReferencePointCache
}

type SentLogCollection struct {
	Items []SentLogData
}

type FormInterface interface {
}

func StartSentLogServer(dbPath string, htmlFormatPath string) *SentLogServer {

	guiPlotterData := sentLogServer.setupGuiPlotterData()

	sLogServer := &SentLogServer{
		SentLogDatabaseHandler: InitSentLogDatabase(dbPath),
		HTMLFormatPath:         htmlFormatPath,
		GuiPlotter:             guiPlotterData,
	}

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
func (sentLogServer *SentLogServer) setupGuiPlotterData() *guiPlotter.GUIPlotter {
	d := guiPlotter.Data{Beacons: *sentLogServer.prepInitialBeaconDataForGuiPlotter(), Tags: *sentLogServer.prepInitialTagDataForGuiPlotter()}
	gp := guiPlotter.StartGUIPlotter(d)
	return gp

}

func (sentLogServer *SentLogServer) prepInitialTagDataForGuiPlotter() *[]guiPlotter.Tag {
	return nil
}

func (sentLogServer *SentLogServer) prepInitialBeaconDataForGuiPlotter() *[]guiPlotter.Beacon {
	beaconData := []guiPlotter.Beacon{}

	beaconID, positions := sentLogServer.referencePointCache.GetAllReferencePoints()

	for i := 0; i < len(beaconID); i++ {
		pos := guiPlotter.Pos{
			X: int(positions[i].X),
			Y: int(positions[i].Y),
			Z: int(positions[i].Z),
		}

		beacon := guiPlotter.Beacon{
			MessageType: "beaconMessage",
			RpId:        beaconID[i],
			Position:    pos,
		}

		beaconData = append(beaconData, beacon)
	}

	return &beaconData
}
