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

	sLogServer := &SentLogServer{
		SentLogDatabaseHandler: InitSentLogDatabase(dbPath),
		HTMLFormatPath:         htmlFormatPath,
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
			guiPlotter.SendTagUpdate(form.Readings[i].Tag_id, form.Readings[i].Rp_id, form.Readings[i].Rssi, form.Readings[i].Chain_delay[0].Sent.Seconds, "directReading", 0, 0, 0)
		}
	}

}

func (sentLogServer *SentLogServer) SaveXYZForm(form structs.XYZForm) error {
	jsonData, err := json.Marshal(form)
	if err != nil {
		log.Printf("SentLogServer encountered a during json marshal at SaveXYZForm")
		return err
	} else {
		jsonString := string(jsonData)
		sentLogServer.SentLogDatabaseHandler.saveToDB("XYZForm", len(form.Chain_delay), jsonString)
		guiPlotter.SendTagUpdate(form.Tag_id, "", 0, form.Chain_delay[0].Sent.Seconds, "indirectReading", int(form.X), int(form.Y), int(form.Z))
		return nil
	}
}

func (sentLogServer *SentLogServer) SetReferencePointChache(rpChache *ReferencePointCache) {
	sentLogServer.referencePointCache = rpChache
}

// SetReferencePointChache needs be be called before StartGUIPlotter
func (sentLogServer *SentLogServer) StartGUIPlotter() {
	d := guiPlotter.Data{
		Beacons: *sentLogServer.prepInitialBeaconDataForGuiPlotter(),
		Tags:    *sentLogServer.prepInitialTagDataForGuiPlotter()}

	gp := guiPlotter.SetupGUIPlotter(d)
	sentLogServer.GuiPlotter = gp
}

func (sentLogServer *SentLogServer) prepInitialTagDataForGuiPlotter() *[]guiPlotter.Tag {
	tagData := []guiPlotter.Tag{}
	sentLogData := sentLogServer.SentLogDatabaseHandler.GetSentLog()
	for _, data := range sentLogData {

		if data.FormType == "RssiForm" {
			formStruct := &structs.RssiForm{}
			err := json.Unmarshal([]byte(data.JsonString), &formStruct)
			if err != nil {
				log.Println("Error at prepInitialTagDataForGuiPlotter, error: ", err)
			}

			for _, reading := range formStruct.Readings {
				pos := guiPlotter.Pos{X: 0, Y: 0, Z: 0}

				tag := guiPlotter.Tag{
					MessageType: "tagMessage",
					TagId:       reading.Tag_id,
					RpId:        reading.Rp_id,
					Accuracy:    reading.Rssi,
					Date:        reading.Chain_delay[0].Received.Seconds,
					ReadingType: "directReading",
					Position:    pos}
				tagData = append(tagData, tag)

			}
		}
		if data.FormType == "XYZForm" {
			formStruct := structs.XYZForm{}
			err := json.Unmarshal([]byte(data.JsonString), &formStruct)
			if err != nil {
				log.Println("Error at prepInitialTagDataForGuiPlotter, error: ", err)
			}

			pos := guiPlotter.Pos{X: int(formStruct.X), Y: int(formStruct.Y), Z: int(formStruct.Z)}
			tag := guiPlotter.Tag{
				MessageType: "tagMessage",
				TagId:       formStruct.Tag_id,
				RpId:        "",
				Accuracy:    formStruct.Accuracy,
				Date:        formStruct.Chain_delay[0].Received.Seconds,
				ReadingType: "indirectReading",
				Position:    pos}
			tagData = append(tagData, tag)

		}
	}

	return &tagData
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
