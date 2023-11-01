package forms

type TimeStruct struct {
	Seconds int `json:"seconds"`
	Nanos   int `json:"nanos"`
}

type XYZStruct struct {
	X int `json:"x"`
	Y int `json:"y"`
	Z int `json:"z"`
}
type WifiPropertiesStruct struct {
	Online         bool   `json:"online"`
	LocationStatus string `json:"locationStatus"`
	IP             string `json:"ip"`
}

type BLEPropertiesStruct struct {
	Active            bool `json:"active"`
	BatteryPercentage int  `json:"batteryPercentage"`
}

type ErrorStruct struct {
	Code int `json:"code"`
}

type ReferencePointStruct struct {
	Timestamp     TimeStruct `json:"timestamp"`
	CorrelationId string     `json:"correlationId"`
}
