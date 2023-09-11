# Mobilaris XYZ API 1.0.0 documentation

External XYZ interface for publishing xyz data to Mobilaris Location Engine.

##### Specification tags

| Name | Description | Documentation |
|---|---|---|
| External | - | - |
| XYZ | - | - |


## Table of Contents

* [Servers](#servers)
  * [RabbitMQ](#rabbitmq-server)
* [Operations](#operations)
  * [PUB xyz](#pub-xyz-operation)

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

### PUB `xyz` Operation

Endpoint for xyz data

#### `mqtt` Channel specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| topic | - | - | `"xyz"` | - | - |

#### Message `Request`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

Root message object

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| position | array<object> | - | - | - | **required** |
| position.chain_delay | array<object> | - | - | - | - |
| position.chain_delay.name | string | Name of service / device introducing delay | - | - | **required** |
| position.chain_delay.sent_time | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| position.chain_delay.sent_time.nanos | integer | - | - | - | **required** |
| position.chain_delay.sent_time.seconds | integer | - | - | - | **required** |
| position.chain_delay.received_time | object | Unix time(UTC) divided into seconds and nano seconds. | - | - | **required**, **additional properties are allowed** |
| position.chain_delay.received_time.nanos | integer | - | - | - | **required** |
| position.chain_delay.received_time.seconds | integer | - | - | - | **required** |
| position.x | number | x coordinate | - | - | **required** |
| position.y | number | y coordinate | - | - | **required** |
| position.z | number | z coordinate | - | - | **required** |
| position.accuracy | integer | Accuracy of position. | - | - | **required** |
| position.tag_id | string | MAC address of device / tag. Formatted aa:bb.cc:dd:ee:f1 | - | - | **required** |

> Examples of payload _(generated)_

```json
{
  "position": [
    {
      "chain_delay": [
        {
          "name": "ext_pos_src",
          "sent_time": {
            "nanos": 3299600,
            "seconds": 1639658351
          },
          "received_time": {
            "nanos": 3299600,
            "seconds": 1639658351
          }
        }
      ],
      "x": 800.5,
      "y": 550,
      "z": -350.83,
      "accuracy": 5,
      "tag_id": "aa:bb:cc:dd:ee:f1"
    }
  ]
}
```



