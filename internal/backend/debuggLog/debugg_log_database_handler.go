package debugglog

import (
	"database/sql"
	"fmt"
	"time"

	_ "github.com/mattn/go-sqlite3" // Import SQLite methods.
)

type DebuggLogDatabaseHandler struct {
	database *sql.DB
}

func InitDebuggLogDatabase(path string) *DebuggLogDatabaseHandler {
	//database/data/DB.db
	debuggLogDatabaseHandler := &DebuggLogDatabaseHandler{}
	db, err := sql.Open("sqlite3", "file:"+path+"?cache=shared&_journal=WAL&_foreign_keys=on")
	if err != nil {
		panic(err.Error())

	}

	if err := db.Ping(); err != nil {
		fmt.Println("could not connect to database:")
		fmt.Println(err)
		panic(err.Error())

	} else {
		debuggLogDatabaseHandler.database = db
		return debuggLogDatabaseHandler
	}

}

func (debuggLogDatabaseHandler *DebuggLogDatabaseHandler) Save(data []byte) {
	currentTime := time.Now().Format("2006-01-02T15:04:05.000Z")
	stmt, err := debuggLogDatabaseHandler.database.Prepare("INSERT INTO DebuggLog (date, data) VALUES (?,?)")
	_, err = stmt.Exec(currentTime, data)
	defer stmt.Close()
	if err != nil {
		println(err.Error())
		panic("Encounterd an error, debuggLog failed to save")
	}
}

func (debuggLogDatabaseHandler *DebuggLogDatabaseHandler) GetDebuggLog() ([]string, [][]byte) {

	var dates []string
	var dataList [][]byte

	var rows *sql.Rows
	//var err error
	rows, _ = debuggLogDatabaseHandler.database.Query("SELECT * FROM DebuggLog")
	defer rows.Close()

	for rows.Next() {
		address := ""
		var data []byte
		err := rows.Scan(&address, &data)
		if err != nil {
			println(err.Error())
			panic("Encounterd an error while quering the database table DebuggLog")
		} else {

			dates = append(dates, address)
			dataList = append(dataList, data)
		}

	}
	return dates, dataList
}
