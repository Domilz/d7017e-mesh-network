package guiPlotter

import (
	"encoding/json"
	"fmt"
	"html/template"
	"log"
	"net/http"
	"sync"

	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

type GUIPlotter struct {
	clients     map[*WebSocketClient]bool
	clientsLock sync.Mutex
	initData    *[]Data
	dataLock    sync.Mutex
}
type WebSocketClient struct {
	conn *websocket.Conn
}
type Tag struct {
	MessageType string `json:"messageType"`
	TagId       string `json:"tagId"`
	RpId        string `json:"rpId"`
	Accuracy    int    `json:"accuracy"`
	Date        string `json:"date"`
	ReadingType string `json:"readingType"`
	Position    Pos    `json:"position"`
}

type Beacon struct {
	MessageType string `json:"messageType"`
	RpId        string `json:"rpId"`
	Position    Pos    `json:"position"`
}
type Pos struct {
	X int `json:"x"`
	Y int `json:"y"`
	Z int `json:"z"`
}

type Data struct {
	Beacons []Beacon `json:"beacons"`
	Tags    []Tag    `json:"tags"`
}

var (
	guiPlotter *GUIPlotter
)

func StartGUIPlotter(data Data) *GUIPlotter {
	guiP := &GUIPlotter{}
	guiP.clients = make(map[*WebSocketClient]bool)
	guiPlotter = guiP
	fs := http.FileServer(http.Dir("pkg/backend/guiPlotter/static"))
	http.Handle("/static/", http.StripPrefix("/static/", fs))

	tmpl := template.Must(template.ParseFiles("pkg/backend/guiPlotter/index.html"))
	http.HandleFunc("/plot", func(w http.ResponseWriter, r *http.Request) {
		tmpl.Execute(w, nil) // Pass nil as data
	})
	fmt.Println("before sock")
	http.HandleFunc("/websocket", WebSocketHandler)

	// Start the web server
	port := ":4242"
	println("Server is running on port", port)
	go http.ListenAndServe(port, nil)
	return guiP

}
func WebSocketHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("error during upgrade at guiPlotter %v", err)
		// Handle error
		return
	}
	defer conn.Close()

	client := &WebSocketClient{conn: conn}
	guiPlotter.clientsLock.Lock()
	guiPlotter.clients[client] = true
	guiPlotter.clientsLock.Unlock()

	defer func() {
		log.Printf("closing connection")
		// Remove the client from the clients map when the connection is closed
		guiPlotter.clientsLock.Lock()
		delete(guiPlotter.clients, client)
		guiPlotter.clientsLock.Unlock()
	}()

	// Handle initial data request
	for {
		messageType, p, err := conn.ReadMessage()
		if err != nil {
			// Handle error or connection closed
			return
		}

		if messageType == websocket.TextMessage {
			request := string(p)
			if request == "getInitialData" {
				log.Printf("Establishing connection")
				// Handle the initial data request here
				data := Data{Beacons: getBeaconsForStartup(), Tags: getTagsForStartup()}

				// Marshal the initial data to JSON and send it to the client
				jsonData, _ := json.Marshal(data)
				if err := conn.WriteMessage(websocket.TextMessage, jsonData); err != nil {
					// Handle error
					return
				}
			}
		}
	}
}

func SendTagUpdate(tagId string, rpId string, accuracy int, date string, readingType string, x int, y int, z int) {

	pos := Pos{x, y, z}
	tag := &Tag{
		MessageType: "tagMessage",
		TagId:       tagId,
		RpId:        rpId,
		Accuracy:    accuracy,
		Date:        date,
		ReadingType: readingType,
		Position:    pos}
	tags := []Tag{*tag}
	data := Data{Beacons: []Beacon{}, Tags: tags}
	bytes, _ := json.Marshal(data)
	fmt.Println("updating with tag")
	sendToClients(bytes)

}
func SendBeaconUpdate(rpId string, x int, y int, z int) {
	//Not currently used, look over when updating beacon is requierd
	pos := Pos{x, y, z}
	beacon := &Beacon{
		MessageType: "beaconMessage",
		RpId:        rpId,
		Position:    pos,
	}
	bytes, _ := json.Marshal(beacon)
	sendToClients(bytes)

}

func sendToClients(bytes []byte) {

	for client := range guiPlotter.clients {
		log.Print("Sending to client")
		err := client.conn.WriteMessage(websocket.TextMessage, bytes)
		if err != nil {
			log.Printf("guiPlotter encountered error during sendToClients, err: %v", err)
		}
	}

}

func getBeaconsForStartup() []Beacon {
	list := []Beacon{}
	pos1 := Pos{0, -1, -2}
	beacon1 := &Beacon{
		RpId:     "rpId1",
		Position: pos1,
	}
	list = append(list, *beacon1)
	pos2 := Pos{-3, -2, 2}
	beacon2 := &Beacon{
		RpId:     "rpId2",
		Position: pos2,
	}
	list = append(list, *beacon2)
	pos3 := Pos{2, -1, 2}
	beacon3 := &Beacon{
		RpId:     "rpId3",
		Position: pos3,
	}
	list = append(list, *beacon3)
	pos4 := Pos{0, -1, 0}
	beacon4 := &Beacon{
		RpId:     "rpId4",
		Position: pos4,
	}
	list = append(list, *beacon4)
	pos5 := Pos{5, -6, 2}
	beacon5 := &Beacon{
		RpId:     "rpId5",
		Position: pos5,
	}
	list = append(list, *beacon5)
	return list
}
func getTagsForStartup() []Tag {
	list := []Tag{}
	pos1 := Pos{0, -1, -2}
	tag1 := &Tag{
		MessageType: "tagMessage",
		TagId:       "aa",
		RpId:        "rpId1",
		Accuracy:    5,
		Date:        "someDate",
		ReadingType: "indirectReading",
		Position:    pos1}
	list = append(list, *tag1)
	pos2 := Pos{-3, -2, 2}
	tag2 := &Tag{
		MessageType: "tagMessage",
		TagId:       "bb",
		RpId:        "rpId2",
		Accuracy:    5,
		Date:        "someDate",
		ReadingType: "indirectReading",
		Position:    pos2}
	list = append(list, *tag2)
	pos3 := Pos{10, 10, 10}
	tag3 := &Tag{
		MessageType: "tagMessage",
		TagId:       "cc",
		RpId:        "rpId3",
		Accuracy:    5,
		Date:        "someDate",
		ReadingType: "directReading",
		Position:    pos3}
	list = append(list, *tag3)

	return list
}

func (guiPlotter *GUIPlotter) getTagDataFromSentLog() []Tag {
	//guiPlotter.sentLog.G
	return nil
}
