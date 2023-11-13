package stateLog

import (
	"html/template"
	"log"
	"net/http"

	"github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/handlers"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var stateLogServer *StateLogServer

type StateLogServer struct {
	BackendStateHandler *handlers.BackendStateHandler
	HTMLFormatPath      string
}

type StateLogCollection struct {
	Items []pb.Reading
}

type FormInterface interface {
}

func StartStateLogServer(backendStateHandler *handlers.BackendStateHandler, htmlFormatPath string) *StateLogServer {

	sLogServer := &StateLogServer{backendStateHandler, htmlFormatPath}

	stateLogServer = sLogServer
	http.HandleFunc("/statelog", StateLog)
	log.Printf("Starting state server")

	mockReading := &pb.Reading{
		TagId: "Mocky Mock",
		RpId:  "666",
		Rssi:  222,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 1,
	}

	backendStateHandler.InsertSingleReading(mockReading)

	go http.ListenAndServe(":4242", nil)
	return stateLogServer
}

func StateLog(w http.ResponseWriter, req *http.Request) {
	switch req.Method {
	case "GET":
		readings := stateLogServer.BackendStateHandler.GetState().Readings

		r := []pb.Reading{}

		for _, reading := range readings {
			r = append(r, *reading)
		}

		stateLogCollection := StateLogCollection{Items: r}
		tmpl, _ := template.ParseFiles(stateLogServer.HTMLFormatPath)
		tmpl.Execute(w, stateLogCollection)

	default:
		log.Printf("Got a request for a unsupported method: %v", req.Method)

	}
}
