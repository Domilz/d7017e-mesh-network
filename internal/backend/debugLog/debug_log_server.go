package debuglog

import (
	"encoding/json"
	"fmt"
	"html/template"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"

	server "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer"
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
	fmt.Println("Started debuggLogServer")
	http.HandleFunc("/debuglog", PostLog)
	http.ListenAndServe(":4242", nil)

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

		}
		req.Body.Close()
		str := string(body[:])
		b := stringTOByteArr(str)
		fmt.Println(b)

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

func stringTOByteArr(input string) []byte {
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
			fmt.Printf("Error parsing part at index %d: %v\n", i, err)
			return nil
		}
		byteArray[i] = byte(num)
	}

	// Print the resulting byte array
	fmt.Println(byteArray)
	return byteArray
}
