package debuglog

import (
	"encoding/json"
	"fmt"
	"html/template"
	"io/ioutil"
	"net/http"
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
	fmt.Println("Starting sentLog server")
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
		debugLogServer.debugLogDatabaseHandler.Save(body)

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
