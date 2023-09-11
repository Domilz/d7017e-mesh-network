
  package asyncapi

  import (
    "encoding/json"

    "github.com/ThreeDotsLabs/watermill/message"
  )
  
    
    // ReferencePointModifyCmd represents a ReferencePointModifyCmd model.
type ReferencePointModifyCmd struct {
  Timestamp Timestamp `json:"timestamp"`
  ReplyTo string `json:"replyTo"`
  CorrelationId string `json:"correlationId"`
  Operand ReferencePoint `json:"operand"`
}
    
    
    // Timestamp represents a Timestamp model.
type Timestamp struct {
  Seconds int `json:"seconds"`
  Nanos int `json:"nanos"`
}
    
    
    // ReferencePoint represents a ReferencePoint model.
type ReferencePoint struct {
  Uuid string `json:"uuid"`
  RpId string `json:"rpId"`
  Name string `json:"name"`
  Description string `json:"description"`
  CreateTime Timestamp `json:"createTime"`
  UpdateTime Timestamp `json:"updateTime"`
  Location Location `json:"location"`
  ReservedType ReferencePointType `json:"type"`
  WifiProperties ReferencePointWifiProperties `json:"wifiProperties"`
  BleProperties ReferencePointBleProperties `json:"bleProperties"`
  LteProperties map[string]interface{} `json:"lteProperties"`
  Vendor string `json:"vendor"`
  VendorProperties map[string]interface{} `json:"vendorProperties"`
}
    
    
    // Location represents a Location model.
type Location struct {
  X float64 `json:"x"`
  Y float64 `json:"y"`
  Z float64 `json:"z"`
}
    
    
    // ReferencePointType represents an enum of ReferencePointType.
type ReferencePointType uint

const (
  ReferencePointTypeWifi ReferencePointType = iota
  ReferencePointTypeBle
  ReferencePointTypeLte
  ReferencePointTypeUnknown
)

// Value returns the value of the enum.
func (op ReferencePointType) Value() any {
	if op >= ReferencePointType(len(ReferencePointTypeValues)) {
		return nil
	}
	return ReferencePointTypeValues[op]
}

var ReferencePointTypeValues = []any{"WIFI","BLE","LTE","UNKNOWN"}
var ValuesToReferencePointType = map[any]ReferencePointType{
  ReferencePointTypeValues[ReferencePointTypeWifi]: ReferencePointTypeWifi,
  ReferencePointTypeValues[ReferencePointTypeBle]: ReferencePointTypeBle,
  ReferencePointTypeValues[ReferencePointTypeLte]: ReferencePointTypeLte,
  ReferencePointTypeValues[ReferencePointTypeUnknown]: ReferencePointTypeUnknown,
}

    
    
    // ReferencePointWifiProperties represents a ReferencePointWifiProperties model.
type ReferencePointWifiProperties struct {
  Online bool `json:"online"`
  LocationStatus ReferencePointWifiLocationStatus `json:"locationStatus"`
  Ip string `json:"ip"`
}
    
    
    // ReferencePointWifiLocationStatus represents an enum of ReferencePointWifiLocationStatus.
type ReferencePointWifiLocationStatus uint

const (
  ReferencePointWifiLocationStatusNormal ReferencePointWifiLocationStatus = iota
  ReferencePointWifiLocationStatusEstimated
  ReferencePointWifiLocationStatusWrong
)

// Value returns the value of the enum.
func (op ReferencePointWifiLocationStatus) Value() any {
	if op >= ReferencePointWifiLocationStatus(len(ReferencePointWifiLocationStatusValues)) {
		return nil
	}
	return ReferencePointWifiLocationStatusValues[op]
}

var ReferencePointWifiLocationStatusValues = []any{"NORMAL","ESTIMATED","WRONG"}
var ValuesToReferencePointWifiLocationStatus = map[any]ReferencePointWifiLocationStatus{
  ReferencePointWifiLocationStatusValues[ReferencePointWifiLocationStatusNormal]: ReferencePointWifiLocationStatusNormal,
  ReferencePointWifiLocationStatusValues[ReferencePointWifiLocationStatusEstimated]: ReferencePointWifiLocationStatusEstimated,
  ReferencePointWifiLocationStatusValues[ReferencePointWifiLocationStatusWrong]: ReferencePointWifiLocationStatusWrong,
}

    
    
    // ReferencePointBleProperties represents a ReferencePointBleProperties model.
type ReferencePointBleProperties struct {
  Active bool `json:"active"`
  BatteryPercentage float64 `json:"batteryPercentage"`
}
    
    
    // ReferencePointSnapshotCmd represents a ReferencePointSnapshotCmd model.
type ReferencePointSnapshotCmd struct {
  ReplyTo string `json:"replyTo"`
  CorrelationId string `json:"correlationId"`
  ReservedType ReferencePointType `json:"type"`
  Prefix string `json:"prefix"`
  Timestamp Timestamp `json:"timestamp"`
}
    
    
    // ReferencePointUpdateEvent represents a ReferencePointUpdateEvent model.
type ReferencePointUpdateEvent struct {
  Timestamp Timestamp `json:"timestamp"`
  CorrelationId string `json:"correlationId"`
  Operation Operation `json:"operation"`
  Operands []interface{} `json:"operands"`
  Error Error `json:"error"`
}
    
    
    // Operation represents an enum of Operation.
type Operation uint

const (
  OperationPost Operation = iota
  OperationPut
  OperationPatch
  OperationDelete
)

// Value returns the value of the enum.
func (op Operation) Value() any {
	if op >= Operation(len(OperationValues)) {
		return nil
	}
	return OperationValues[op]
}

var OperationValues = []any{"POST","PUT","PATCH","DELETE"}
var ValuesToOperation = map[any]Operation{
  OperationValues[OperationPost]: OperationPost,
  OperationValues[OperationPut]: OperationPut,
  OperationValues[OperationPatch]: OperationPatch,
  OperationValues[OperationDelete]: OperationDelete,
}

    
    
    // Error represents a Error model.
type Error struct {
  Code int `json:"code"`
  Message string `json:"message"`
}
    
    
    // ReferencePointModifyResponse represents a ReferencePointModifyResponse model.
type ReferencePointModifyResponse struct {
  Timestamp Timestamp `json:"timestamp"`
  CorrelationId string `json:"correlationId"`
  Result ReferencePoint `json:"result"`
  Error Error `json:"error"`
}
    

// PayloadToMessage converts a payload to watermill message
func PayloadToMessage(i interface{}) (*message.Message, error) {
  var m message.Message

  b, err := json.Marshal(i)
  if err != nil {
    return nil, nil
  }
  m.Payload = b

  return &m, nil
}
  