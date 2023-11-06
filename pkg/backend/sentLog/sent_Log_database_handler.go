package sentLog

import (
	"database/sql"
	"log"
	"time"

	_ "github.com/mattn/go-sqlite3" // Import SQLite methods.
)

type SentLogDatabaseHandler struct {
	database *sql.DB
}

type SentLogData struct {
	ReceivedDate string `json:"receivedDate"`
	FormType     string `json:"formType"`
	Size         int    `json:"size"`
	JsonString   string `json:"jsonString"`
}

func InitSentLogDatabase(path string) *SentLogDatabaseHandler {
	debugLogDatabaseHandler := &SentLogDatabaseHandler{}
	db, err := sql.Open("sqlite3", "file:"+path+"?cache=shared&_journal=WAL&_foreign_keys=on")
	if err != nil {
		log.Printf("Encountered during at DebugLogDatabaseHandler when opening the SentLogDatabase: %v", err)
		panic(err.Error())

	}

	if err := db.Ping(); err != nil {
		log.Printf("Encountered during at SentLogDatabaseHandler when pinging the SentLogDatabase: %v", err)
		panic(err.Error())

	}
	debugLogDatabaseHandler.database = db
	log.Printf("Successfully connected to SentLogDatabase")
	return debugLogDatabaseHandler
}

func (sentLogDatabaseHandler *SentLogDatabaseHandler) saveToDB(formType string, size int, jsonString string) {
	currentTime := time.Now().Format("2006-01-02T15:04:05.000Z")
	stmt, err := sentLogDatabaseHandler.database.Prepare("INSERT INTO SentLog (receivedDate, formType, size, jsonString) VALUES (?,?,?,?)")
	_, err = stmt.Exec(currentTime, formType, size, jsonString)
	defer stmt.Close()
	if err != nil {
		log.Printf("Encountered during at SentLogDatabaseHandler when saving to SentLogDatabase: %v", err)
		panic(err.Error())
	}
}

func (debugLogDatabaseHandler *SentLogDatabaseHandler) GetSentLog() []SentLogData {

	data := []SentLogData{}

	var rows *sql.Rows
	//var err error
	rows, _ = debugLogDatabaseHandler.database.Query("SELECT * FROM SentLog")
	defer rows.Close()

	for rows.Next() {
		recivedDate := ""
		formType := ""
		size := 0
		jsonString := ""

		err := rows.Scan(&recivedDate, &formType, &size, &jsonString)
		if err != nil {
			log.Printf("Encountered during at SentLogDatabaseHandler when querying the SentLogDatabase for data: %v", err)
			panic(err.Error())
		} else {

		}
		data = append(data, SentLogData{recivedDate, formType, size, jsonString})

	}
	return data

}
