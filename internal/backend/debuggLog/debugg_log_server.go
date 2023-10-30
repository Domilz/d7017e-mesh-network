package debugglog

import (
	"encoding/json"
	"html/template"
	"io/ioutil"
	"net/http"
)

var debuggLogServer *DebuggLogServer

type DebuggLogServer struct {
	debuggLogDatabaseHandler *DebuggLogDatabaseHandler
	HTMLFormatPath           string
}
type DebuggLogStruct struct {
	Date string `json:"date"`
	Data []byte `json:"data"`
}
type DebuggLogCollection struct {
	Items []DebuggLogStruct
}

func StartDebuggLogServer(dbPath string, htmlFormatPath string) {

	dbuggLogServer := &DebuggLogServer{InitDebuggLogDatabase(dbPath), htmlFormatPath}

	debuggLogServer = dbuggLogServer
	http.HandleFunc("/debugglog", PostLog)
	http.ListenAndServe(":4242", nil)

}

func PostLog(w http.ResponseWriter, req *http.Request) {
	switch req.Method {
	case "GET":
		debuggLogCollection := DebuggLogCollection{}
		dates, data := debuggLogServer.debuggLogDatabaseHandler.GetDebuggLog()

		for i := 0; i < len(dates); i++ {

			debuggLogLine := DebuggLogStruct{
				Date: dates[i],
				Data: data[i],
			}
			debuggLogCollection.Items = append(debuggLogCollection.Items, debuggLogLine)
		}

		tmpl, _ := template.ParseFiles(debuggLogServer.HTMLFormatPath)

		tmpl.Execute(w, debuggLogCollection)
	case "POST":
		body, err := ioutil.ReadAll(req.Body)
		if err != nil {

		}
		req.Body.Close()
		debuggLogServer.debuggLogDatabaseHandler.Save(body)

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
