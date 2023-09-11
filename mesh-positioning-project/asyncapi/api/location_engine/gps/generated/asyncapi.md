# Mobilaris GPS API 1.0.0 documentation

External GPS interface for publishing GPS location data to Mobilaris Location Engine.

##### Specification tags

| Name | Description | Documentation |
|---|---|---|
| External | - | - |
| GPS | - | - |


## Table of Contents

* [Servers](#servers)
  * [RabbitMQ](#rabbitmq-server)
* [Operations](#operations)
  * [PUB GPS](#pub-gps-operation)

## Servers

### `RabbitMQ` Server

* URL: `mq.{site}:{mqtt_port}`
* Protocol: `mqtt 1.0.0`

Mobilaris RabbitMQ Broker

#### URL Variables

| Name | Description | Default value | Allowed values |
|---|---|---|---|
| site | Site URL | _None_ | _Any_ |
| mqtt_port | MQTT Port | `1883` | _Any_ |

#### Security

##### Security Requirement 1

* Type: `User/Password`





#### `mqtt` Server specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| cleanSession | - | - | `"true"` | - | - |


## Operations

### PUB `GPS` Operation

Endpoint for GPS data

#### `mqtt` Channel specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| topic | - | - | `"gps"` | - | - |

#### Message `Request`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

Root message object

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| readings | array<object> | - | - | - | **required** |
| readings.id | string | MAC address of device / tag. Formatted aa:bb.cc:dd:ee:f1 | - | - | **required** |
| readings.latitude | number | GPS latitude value | - | - | **required** |
| readings.longitude | number | GPS longitude value | - | - | **required** |
| readings.altitude | number | GPS altitude value | - | - | **required** |
| readings.accuracy | object | Accuracy of reading. | - | - | **additional properties are allowed** |
| readings.accuracy.horizontal_accuracy_meters | number | Horizontal accuracy in meters | - | - | **required** |
| readings.accuracy.vertical_accuracy_meters | number | Vertical accuracy in meters | - | - | **required** |
| readings.chain_delay | array<object> | - | - | - | - |
| readings.chain_delay.name | string | Name of service / device introducing delay | - | - | **required** |
| readings.chain_delay.sent | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.sent.nanos | number | - | - | - | **required** |
| readings.chain_delay.sent.seconds | number | - | - | - | **required** |
| readings.chain_delay.received | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.received.nanos | number | - | - | - | **required** |
| readings.chain_delay.received.seconds | number | - | - | - | **required** |

> Examples of payload _(generated)_

```json
{
  "readings": [
    {
      "id": "aa:bb:cc:dd:ee:f1",
      "latitude": 65.58686312151238,
      "longitude": 22.166388265414902,
      "altitude": 15.12345,
      "accuracy": {
        "horizontal_accuracy_meters": 3.14,
        "vertical_accuracy_meters": 3.14
      },
      "chain_delay": [
        {
          "name": "gpstag",
          "sent": {
            "nanos": 3299600,
            "seconds": 1639658351
          },
          "received": {
            "nanos": 3299600,
            "seconds": 1639658351
          }
        }
      ]
    }
  ]
}
```



