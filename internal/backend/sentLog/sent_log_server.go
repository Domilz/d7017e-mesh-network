package sentLog

import (
	"encoding/json"
	"fmt"
	"html/template"
	"net/http"

	structs "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer/forms"
	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var sentLogServer *SentLogServer

type SentLogServer struct {
	sentLogDatabaseHandler *SentLogDatabaseHandler
	HTMLFormatPath         string
}
type SentLogStruct struct {
	FormType string `json:"formType"`
	Size     int    `json:"size"`
	Data     string `json:"data"`
}
type SentLogCollection struct {
	Items []SentLogStruct
}

type FormInterface interface {
}

func StartSentLogServer(dbPath string, htmlFormatPath string) {
	//sLogServer := &SentLogServer{InitDebugLogDatabase(dbPath), htmlFormatPath}
	sLogServer := &SentLogServer{HTMLFormatPath: htmlFormatPath}

	sentLogServer = sLogServer
	http.HandleFunc("/sentlog", SentLog)
	http.ListenAndServe(":4242", nil)

}

func SentLog(w http.ResponseWriter, req *http.Request) {
	switch req.Method {
	case "GET":
		form, _ := TestGet()
		jsonData, err := json.Marshal(form)
		if err != nil {
			fmt.Println("error marshaling")
		}

		jsonString := string(jsonData)

		//fmt.Println(jsonString)

		sentLogCollection := SentLogCollection{}

		sentLogCollection.Items = append(sentLogCollection.Items, SentLogStruct{FormType: "type A", Size: 1, Data: jsonString})
		sentLogCollection.Items = append(sentLogCollection.Items, SentLogStruct{FormType: "type b", Size: 1, Data: jsonString})
		sentLogCollection.Items = append(sentLogCollection.Items, SentLogStruct{FormType: "type c", Size: 1, Data: jsonString})

		tmpl, _ := template.ParseFiles(sentLogServer.HTMLFormatPath)

		tmpl.Execute(w, sentLogCollection)

	default:
		println("Method not supported: ", req.Method)

	}
}

func createRssiForm(readingID string) *structs.RssiForm {
	reading := &pb.Reading{
		TagId: readingID,
		RpId:  "RpId not set ",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 1,
	}
	timeForSentRecived := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds),
		Nanos:   int(timestamppb.Now().Nanos),
	}

	newChainDelayStruct := &structs.Chain_delay{
		Name:     "tagbackend",
		Sent:     *timeForSentRecived,
		Received: *timeForSentRecived,
	}

	newReadingStruct := &structs.Reading{
		Rp_id:       reading.RpId,
		Rssi:        int(reading.Rssi),
		Tag_id:      reading.TagId,
		Type:        "BLE",
		Chain_delay: []structs.Chain_delay{*newChainDelayStruct},
	}

	newRssiForm := &structs.RssiForm{
		Readings: []structs.Reading{*newReadingStruct},
	}
	return newRssiForm
}

func TestGet() (*structs.RssiForm, string) {
	form := createRssiForm("111")
	/*
		b, err := json.Marshal(form)
		if err != nil {
			fmt.Println("Error marshalling")
		}
		dst := &bytes.Buffer{}
	*/
	a, err := json.MarshalIndent(form, "", "\t")
	if err != nil {
		fmt.Println("error:", err)
	}
	fmt.Println("before")
	//os.Stdout.Write(a)
	s := string(a[:])
	fmt.Println(s)
	/*
		if err := json.Indent(dst, b, "", "  "); err != nil {
			panic(err)
		}
	*/
	fmt.Println("done")
	//fmt.Println(dst.String())
	return form, s

}
