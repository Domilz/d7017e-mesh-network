package guiPlotter

import (
	"encoding/json"
	"fmt"
	"html/template"
	"log"
	"net/http"
	"os"
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
	initData    Data
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

type Config struct {
	WebSocketURL string `json:"websocket_url"`
}

var (
	guiPlotter *GUIPlotter
)

func SetupGUIPlotter(data Data) *GUIPlotter {
	guiP := &GUIPlotter{}
	guiP.initData = data
	guiP.clients = make(map[*WebSocketClient]bool)
	guiPlotter = guiP
	fs := http.FileServer(http.Dir("pkg/backend/guiPlotter/static"))
	http.Handle("/static/", http.StripPrefix("/static/", fs))

	tmpl := template.Must(template.ParseFiles("pkg/backend/guiPlotter/index.html"))
	http.HandleFunc("/plot", func(w http.ResponseWriter, r *http.Request) {
		tmpl.Execute(w, nil) // Pass nil as data
	})
	SERVER_ADDRESS := os.Getenv("SERVER_ADDRESS")
	SERVER_PORT := os.Getenv("SERVER_PORT")
	http.HandleFunc("/websocket", WebSocketHandler)
	http.HandleFunc("/config", func(w http.ResponseWriter, r *http.Request) {

		json.NewEncoder(w).Encode(map[string]string{"websocketUrl": "ws://" + SERVER_ADDRESS + ":" + SERVER_PORT + "/websocket"})
	})
	// Start the web server

	println("Server is running on port: ", SERVER_PORT)
	go http.ListenAndServe(SERVER_PORT, nil)
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
				guiPlotter.dataLock.Lock()
				data := guiPlotter.initData
				guiPlotter.dataLock.Unlock()
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
	guiPlotter.dataLock.Lock()
	guiPlotter.initData.Tags = append(guiPlotter.initData.Tags, *tag)
	guiPlotter.dataLock.Unlock()
	tags := []Tag{*tag}
	data := Data{Beacons: []Beacon{}, Tags: tags}
	bytes, _ := json.Marshal(data)
	fmt.Println("updating with tag")
	sendToClients(bytes)

}
func SendBeaconUpdate(rpId string, x int, y int, z int) {
	//Not currently used, look over when updating beacon is requierd, follow the flow SendTagUpdate.
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
