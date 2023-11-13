package handlers

import (
	"database/sql"
	"encoding/json"
	"log"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	_ "github.com/mattn/go-sqlite3" // Import SQLite methods.
)

type StateDatabaseHandler struct {
	database *sql.DB
}

func InitStateDatabase(path string) *StateDatabaseHandler {
	stateDatabaseHandler := &StateDatabaseHandler{}
	db, err := sql.Open("sqlite3", "file:"+path+"?cache=shared&_journal=WAL&_foreign_keys=on")
	if err != nil {
		log.Printf("Encountered during at StateDatabaseHandler when opening the StateDatabase: %v", err)
		panic(err.Error())

	}

	if err := db.Ping(); err != nil {
		log.Printf("Encountered during at StateDatabaseHandler when pinging the StateDatabase: %v", err)
		panic(err.Error())

	}
	stateDatabaseHandler.database = db
	log.Print("Successfully connected to StateDatabase")
	return stateDatabaseHandler

}

func (stateDatabaseHandler *StateDatabaseHandler) Save(reading *pb.Reading) {
	jsonData, err := json.Marshal(reading)
	if err != nil {
		log.Print("StateDatabaseHandler encountered a during json marshal at Save")

	} else {
		jsonString := string(jsonData)

		stmt, err := stateDatabaseHandler.database.Prepare("INSERT INTO Readings (tagId, readingJson) VALUES (?,?)")

		_, err = stmt.Exec(reading.TagId, jsonString)
		defer stmt.Close()
		if err != nil {
			log.Print("Found duplicate when inserting in State DB, updating instead")
			stmt, err := stateDatabaseHandler.database.Prepare("UPDATE Readings SET (tagId, readingJson) = (?,?) WHERE tagId = ?")

			_, err = stmt.Exec(reading.TagId, jsonString, reading.TagId)
			defer stmt.Close()
			if err != nil {
				println(err.Error())
				panic("Encounterd an error during registration while inserting service, (updateService)")
			}

		}

	}

}

func (stateDatabaseHandler *StateDatabaseHandler) LoadFromDB() []pb.Reading {
	readings := []pb.Reading{}

	var rows *sql.Rows
	//var err error
	rows, _ = stateDatabaseHandler.database.Query("SELECT * FROM Readings")
	defer rows.Close()

	for rows.Next() {
		tagID := ""
		readingJsonString := ""
		reading := pb.Reading{}

		err := rows.Scan(&tagID, &readingJsonString)
		if err != nil {
			log.Printf("Encountered during at StateDatabaseHandler when querying the StateDatabase for data: %v", err)
			panic(err.Error())
		} else {

		}
		err = json.Unmarshal([]byte(readingJsonString), &reading)

		if err != nil {
			log.Println(err)

		}

		readings = append(readings, reading)

	}
	log.Print("Loaded from DB:", readings)
	return readings
}
