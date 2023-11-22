package debuglog

import (
	"encoding/json"
	"html/template"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"

	server "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer"
)

var debugLogServer *DebugLogServer

type DebugLogServer struct {
	debugLogDatabaseHandler *DebugLogDatabaseHandler
	HTMLFormatPath          string
}
type DebugLogStruct struct {
	Date string `json:"date"`
	Data []byte `json:"data"`
}
type DebugLogCollection struct {
	Items []DebugLogStruct
}

func StartDebugLogServer(dbPath string, htmlFormatPath string) {

	dbuggLogServer := &DebugLogServer{InitDebugLogDatabase(dbPath), htmlFormatPath}
	debugLogServer = dbuggLogServer

	http.HandleFunc("/debuglog", PostLog)
	log.Println("Starting debugLog server")
	SERVER_PORT := os.Getenv("SERVER_PORT")
	go http.ListenAndServe(":"+SERVER_PORT, nil)

}

func PostLog(w http.ResponseWriter, req *http.Request) {
	switch req.Method {
	case "GET":
		debugLogCollection := DebugLogCollection{}
		dates, data := debugLogServer.debugLogDatabaseHandler.GetDebugLog()

		for i := 0; i < len(dates); i++ {
			debugLogLine := DebugLogStruct{
				Date: dates[i],
				Data: data[i],
			}
			debugLogCollection.Items = append(debugLogCollection.Items, debugLogLine)
		}

		tmpl, _ := template.ParseFiles(debugLogServer.HTMLFormatPath)

		tmpl.Execute(w, debugLogCollection)
	case "POST":
		body, err := ioutil.ReadAll(req.Body)
		if err != nil {
			log.Printf("error reading request body: %v", err)
		}

		req.Body.Close()
		str := string(body[:])
		b := stringToByteArr(str)

		debugLogServer.debugLogDatabaseHandler.Save(b)
		server.SideStepGRPCServer(b)
		w.Header().Set("Content-Type", "application/json")
		jsonResp, err := json.Marshal("Added")

		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		} else {
			w.WriteHeader(http.StatusCreated)
		}
		w.Write(jsonResp)
		return
	default:
		println("Method not supported: ", req.Method)

	}
}

func stringToByteArr(input string) []byte {
	// Remove "[" and "]" from the input string
	input = strings.TrimPrefix(input, "[")
	input = strings.TrimSuffix(input, "]")

	// Split the string by commas
	parts := strings.Split(input, ", ")

	// Create a byte array and parse each part into an integer
	byteArray := make([]byte, len(parts))
	for i, part := range parts {
		num, err := strconv.Atoi(part)
		if err != nil {
			log.Printf("Encountered during at DebugLogServer during stringTOByteArr : %v", err)
			return nil
		}
		byteArray[i] = byte(num)
	}

	return byteArray
}
