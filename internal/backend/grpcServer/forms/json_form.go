package forms

type ReferencePointForm struct {
	Timestamp     TimeStruct       `json:"timestamp"`
	CorrelationId string           `json:"correlationId"`
	Operation     string           `json:"operation"`
	Operands      []OperandsStruct `json:"operands"`
	Error         ErrorStruct      `json:"error"`
}

type RssiForm struct {
	Readings ReadingStruct `json:"readings"`
}

type XYZForm struct {
	X           float32             `json:"x"`
	Y           float32             `json:"y"`
	Z           float32             `json:"z"`
	Accuracy    int                 `json:"accuracy"`
	Tag_id      string              `json:"tag_id"`
	Chain_delay []Chain_delayStruct `json:"chain_delay"`
}
type TimeStruct struct {
	Seconds int `json:"seconds"`
	Nanos   int `json:"nanos"`
}

type OperandsStruct struct {
	Uuid             string                 `json:"uuid"`
	RpId             string                 `json:"rpId"`
	Name             string                 `json:"name"`
	Description      string                 `json:"description"`
	CreateTime       TimeStruct             `json:"createTime"`
	UpdateTime       TimeStruct             `json:"updateTime"`
	Location         XYZStruct              `json:"location"`
	Type             string                 `json:"type"`
	WifiProperties   WifiPropertiesStruct   `json:"wifiProperties"`
	BLEProperties    BLEPropertiesStruct    `json:"bleProperties"`
	LteProperties    LtePropertiesStruct    `json:"lteProperties"`
	Vendor           string                 `json:"vendor"`
	VendorProperties VendorPropertiesStruct `json:"vendorProperties"`
}

type XYZStruct struct {
	X float32 `json:"x"`
	Y float32 `json:"y"`
	Z float32 `json:"z"`
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

type LtePropertiesStruct struct {
}

type VendorPropertiesStruct struct {
}

type ErrorStruct struct {
	Code int `json:"code"`
}

type ReadingStruct struct {
	Rp_id       string              `json:"rp_id"`
	Rssi        int                 `json:"rssi"`
	Tag_id      string              `json:"tag_id"`
	Type        string              `json:"BLE"`
	Chain_delay []Chain_delayStruct `json:"chain_delay"`
}

type Chain_delayStruct struct {
	Name     string     `json:"tagbackend"`
	Sent     TimeStruct `json:"sent"`
	Received TimeStruct `json:"received"`
}
