# Mobilaris RSSI API 1.0.0 documentation

External RSSI interface for publishing RSSI based data to Mobilaris Location Engine.

##### Specification tags

| Name | Description | Documentation |
|---|---|---|
| External | - | - |
| RSSI | - | - |
| Wifi | - | - |
| BLE | - | - |
| LTE | - | - |


## Table of Contents

* [Servers](#servers)
  * [RabbitMQ](#rabbitmq-server)
* [Operations](#operations)
  * [PUB Wifi](#pub-wifi-operation)
  * [PUB BLE](#pub-ble-operation)
  * [PUB LTE](#pub-lte-operation)

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

### PUB `Wifi` Operation

Endpoint for wifi data

#### `mqtt` Channel specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| topic | - | - | `"rssi/ap"` | - | - |

#### Message `Request`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

Root message object

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| readings | array<object> | - | - | - | **required** |
| readings.chain_delay | array<object> | - | - | - | **required** |
| readings.chain_delay.name | string | Name of service / device introducing delay | - | - | **required** |
| readings.chain_delay.sent | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.sent.nanos | integer | - | - | - | **required** |
| readings.chain_delay.sent.seconds | integer | - | - | - | **required** |
| readings.chain_delay.received | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.received.nanos | integer | - | - | - | **required** |
| readings.chain_delay.received.seconds | integer | - | - | - | **required** |
| readings.rp_id | string | MAC address of reference point (ap, ble) or cellID for lte. | - | - | **required** |
| readings.rssi | integer | - | - | - | **required** |
| readings.tag_id | string | MAC address of device / tag. Formatted aa:bb.cc:dd:ee:f1 | - | - | **required** |
| readings.type | string | Type of reading, could be AP, BLE or LTE. | - | - | **required** |

> Examples of payload _(generated)_

```json
{
  "readings": [
    {
      "chain_delay": [
        {
          "name": "tagbackend",
          "sent": {
            "nanos": 3299600,
            "seconds": 1639658351
          },
          "received": {
            "nanos": 3299600,
            "seconds": 1639658351
          }
        }
      ],
      "rp_id": "aa:bb:cc:dd:ee:f0",
      "rssi": -78,
      "tag_id": "aa:bb:cc:dd:ee:f1",
      "type": "BLE"
    }
  ]
}
```



### PUB `BLE` Operation

Endpoint for BLE data

#### `mqtt` Channel specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| topic | - | - | `"rssi/ble"` | - | - |

#### Message `Request`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

Root message object

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| readings | array<object> | - | - | - | **required** |
| readings.chain_delay | array<object> | - | - | - | **required** |
| readings.chain_delay.name | string | Name of service / device introducing delay | - | - | **required** |
| readings.chain_delay.sent | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.sent.nanos | integer | - | - | - | **required** |
| readings.chain_delay.sent.seconds | integer | - | - | - | **required** |
| readings.chain_delay.received | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.received.nanos | integer | - | - | - | **required** |
| readings.chain_delay.received.seconds | integer | - | - | - | **required** |
| readings.rp_id | string | MAC address of reference point (ap, ble) or cellID for lte. | - | - | **required** |
| readings.rssi | integer | - | - | - | **required** |
| readings.tag_id | string | MAC address of device / tag. Formatted aa:bb.cc:dd:ee:f1 | - | - | **required** |
| readings.type | string | Type of reading, could be AP, BLE or LTE. | - | - | **required** |

> Examples of payload _(generated)_

```json
{
  "readings": [
    {
      "chain_delay": [
        {
          "name": "tagbackend",
          "sent": {
            "nanos": 3299600,
            "seconds": 1639658351
          },
          "received": {
            "nanos": 3299600,
            "seconds": 1639658351
          }
        }
      ],
      "rp_id": "aa:bb:cc:dd:ee:f0",
      "rssi": -78,
      "tag_id": "aa:bb:cc:dd:ee:f1",
      "type": "BLE"
    }
  ]
}
```



### PUB `LTE` Operation

Endpoint for LTE data

#### `mqtt` Channel specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| topic | - | - | `"rssi/lte"` | - | - |

#### Message `Request`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

Root message object

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| readings | array<object> | - | - | - | **required** |
| readings.chain_delay | array<object> | - | - | - | **required** |
| readings.chain_delay.name | string | Name of service / device introducing delay | - | - | **required** |
| readings.chain_delay.sent | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.sent.nanos | integer | - | - | - | **required** |
| readings.chain_delay.sent.seconds | integer | - | - | - | **required** |
| readings.chain_delay.received | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| readings.chain_delay.received.nanos | integer | - | - | - | **required** |
| readings.chain_delay.received.seconds | integer | - | - | - | **required** |
| readings.rp_id | string | MAC address of reference point (ap, ble) or cellID for lte. | - | - | **required** |
| readings.rssi | integer | - | - | - | **required** |
| readings.tag_id | string | MAC address of device / tag. Formatted aa:bb.cc:dd:ee:f1 | - | - | **required** |
| readings.type | string | Type of reading, could be AP, BLE or LTE. | - | - | **required** |

> Examples of payload _(generated)_

```json
{
  "readings": [
    {
      "chain_delay": [
        {
          "name": "tagbackend",
          "sent": {
            "nanos": 3299600,
            "seconds": 1639658351
          },
          "received": {
            "nanos": 3299600,
            "seconds": 1639658351
          }
        }
      ],
      "rp_id": "aa:bb:cc:dd:ee:f0",
      "rssi": -78,
      "tag_id": "aa:bb:cc:dd:ee:f1",
      "type": "BLE"
    }
  ]
}
```



