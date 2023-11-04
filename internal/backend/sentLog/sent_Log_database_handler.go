package sentLog

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"time"

	structs "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer/forms"
	_ "github.com/mattn/go-sqlite3" // Import SQLite methods.
)

type SentLogDatabaseHandler struct {
	database *sql.DB
}

type SentLogData struct {
	receivedDate string
	formType     string
	size         int
	jsonString   string
}

func InitSentLogDatabase(path string) *SentLogDatabaseHandler {
	//database/data/DB.db
	debugLogDatabaseHandler := &SentLogDatabaseHandler{}
	db, err := sql.Open("sqlite3", "file:"+path+"?cache=shared&_journal=WAL&_foreign_keys=on")
	if err != nil {
		panic(err.Error())

	}

	if err := db.Ping(); err != nil {
		fmt.Println("Failed to ping sentLog database")
		fmt.Println(err)
		panic(err.Error())

	} else {
		debugLogDatabaseHandler.database = db
		return debugLogDatabaseHandler
	}
	return nil

}

func (sentLogDatabaseHandler *SentLogDatabaseHandler) SendRssiForm(form structs.RssiForm) {
	jsonData, err := json.Marshal(form)
	if err != nil {
		fmt.Println("error during json marshal")
	} else {
		jsonString := string(jsonData)
		sentLogDatabaseHandler.saveToDB("RssiForm", len(form.Readings), jsonString)
	}

}
func (sentLogDatabaseHandler *SentLogDatabaseHandler) SendReferencePointForm(form structs.ReferencePointForm) {
	jsonData, err := json.Marshal(form)
	if err != nil {
		fmt.Println("error during json marshal")
	} else {
		jsonString := string(jsonData)
		sentLogDatabaseHandler.saveToDB("ReferencePointForm", len(form.Operands), jsonString)
	}
}

func (sentLogDatabaseHandler *SentLogDatabaseHandler) saveToDB(formType string, size int, jsonString string) {
	currentTime := time.Now().Format("2006-01-02T15:04:05.000Z")
	stmt, err := sentLogDatabaseHandler.database.Prepare("INSERT INTO SentLog (receivedDate, formType, size, jsonString) VALUES (?,?,?,?)")
	_, err = stmt.Exec(currentTime, formType, size, jsonString)
	defer stmt.Close()
	if err != nil {
		println(err.Error())
		panic("Encounterd an error, SentLog failed to save")
	}
}

func (debugLogDatabaseHandler *SentLogDatabaseHandler) GetDebugLog() []SentLogData {

	data := []SentLogData{}
	data.a
	var rows *sql.Rows
	//var err error
	rows, _ = debugLogDatabaseHandler.database.Query("SELECT * FROM DebugLog")
	defer rows.Close()

	for rows.Next() {
		recivedDate := ""
		formType := ""
		size := 0
		jsonString := ""

		err := rows.Scan(&recivedDate, &formType, &size, &jsonString)
		if err != nil {
			println(err.Error())
			panic("Encounterd an error while quering the database table DebugLog")
		} else {

		}
		data = append(data, SentLogData{re})

	}
	return data

}
