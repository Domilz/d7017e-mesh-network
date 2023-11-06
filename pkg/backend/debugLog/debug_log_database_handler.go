package debuglog

import (
	"database/sql"
	"fmt"
	"time"

	_ "github.com/mattn/go-sqlite3" // Import SQLite methods.
)

type DebugLogDatabaseHandler struct {
	database *sql.DB
}

func InitDebugLogDatabase(path string) *DebugLogDatabaseHandler {
	//database/data/DB.db
	debugLogDatabaseHandler := &DebugLogDatabaseHandler{}
	db, err := sql.Open("sqlite3", "file:"+path+"?cache=shared&_journal=WAL&_foreign_keys=on")
	if err != nil {
		panic(err.Error())

	}

	if err := db.Ping(); err != nil {
		fmt.Println("could not connect to database:")
		fmt.Println(err)
		panic(err.Error())

	} else {
		debugLogDatabaseHandler.database = db
		return debugLogDatabaseHandler
	}

}

func (debugLogDatabaseHandler *DebugLogDatabaseHandler) Save(data []byte) {
	currentTime := time.Now().Format("2006-01-02T15:04:05.000Z")
	stmt, err := debugLogDatabaseHandler.database.Prepare("INSERT INTO DebugLog (date, data) VALUES (?,?)")
	_, err = stmt.Exec(currentTime, data)
	defer stmt.Close()
	if err != nil {
		println(err.Error())
		panic("Encounterd an error, debugLog failed to save")
	}
}

func (debugLogDatabaseHandler *DebugLogDatabaseHandler) GetDebugLog() ([]string, [][]byte) {

	var dates []string
	var dataList [][]byte

	var rows *sql.Rows
	//var err error
	rows, _ = debugLogDatabaseHandler.database.Query("SELECT * FROM DebugLog")
	defer rows.Close()

	for rows.Next() {
		address := ""
		var data []byte
		err := rows.Scan(&address, &data)
		if err != nil {
			println(err.Error())
			panic("Encounterd an error while quering the database table DebugLog")
		} else {

			dates = append(dates, address)
			dataList = append(dataList, data)
		}

	}
	return dates, dataList
}
