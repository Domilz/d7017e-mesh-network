package forms

type ReferencePointForm struct {
	Timestamp     Time       `json:"timestamp"`
	CorrelationId string     `json:"correlationId"`
	Operation     string     `json:"operation"`
	Operands      []Operands `json:"operands"`
	Error         Error      `json:"error"`
}

type RssiForm struct {
	Readings []Reading `json:"readings"`
}

type XYZForm struct {
	X           float32      `json:"x"`
	Y           float32      `json:"y"`
	Z           float32      `json:"z"`
	Accuracy    int          `json:"accuracy"`
	Tag_id      string       `json:"tag_id"`
	Chain_delay []ChainDelay `json:"chain_delay"`
}

type Time struct {
	Seconds int `json:"seconds"`
	Nanos   int `json:"nanos"`
}

type Operands struct {
	Uuid             string           `json:"uuid"`
	RpId             string           `json:"rpId"`
	Name             string           `json:"name"`
	Description      string           `json:"description"`
	CreateTime       Time             `json:"createTime"`
	UpdateTime       Time             `json:"updateTime"`
	Location         XYZ              `json:"location"`
	Type             string           `json:"type"`
	WifiProperties   WifiProperties   `json:"wifiProperties"`
	BLEProperties    BLEProperties    `json:"bleProperties"`
	LteProperties    LteProperties    `json:"lteProperties"`
	Vendor           string           `json:"vendor"`
	VendorProperties VendorProperties `json:"vendorProperties"`
}

type XYZ struct {
	X float32 `json:"x"`
	Y float32 `json:"y"`
	Z float32 `json:"z"`
}

type WifiProperties struct {
	Online         bool   `json:"online"`
	LocationStatus string `json:"locationStatus"`
	IP             string `json:"ip"`
}

type BLEProperties struct {
	Active            bool `json:"active"`
	BatteryPercentage int  `json:"batteryPercentage"`
}

type LteProperties struct {
}

type VendorProperties struct {
}

type Error struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}

type Reading struct {
	Rp_id       string       `json:"rp_id"`
	Rssi        int          `json:"rssi"`
	Tag_id      string       `json:"tag_id"`
	Type        string       `json:"BLE"`
	Chain_delay []ChainDelay `json:"chain_delay"`
}

type ChainDelay struct {
	Name     string `json:"tagbackend"`
	Sent     Time   `json:"sent"`
	Received Time   `json:"received"`
}
