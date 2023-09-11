# Mobilaris MCE Emergency Support for Tags 1.0.0 documentation

External tag interface for receiving and acknowledging emergency messages from Mobilaris Emergency Support.

### Overview
- Subscribe to emergency support messages.
- Publish messages as **delivered** when processed by the device.
- Publish messages as **acknowledged** after receving user input.

##### Specification tags

| Name | Description | Documentation |
|---|---|---|
| External API | - | - |
| Emergency Support | - | - |


## Table of Contents

* [Servers](#servers)
  * [RabbitMQ](#rabbitmq-server)
* [Operations](#operations)
  * [SUB amq_topic/json-messages/{tagId}](#sub-amq_topicjson-messagestagid-operation)
  * [PUB amq_topic/json-reports](#pub-amq_topicjson-reports-operation)

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

### SUB `amq_topic/json-messages/{tagId}` Operation

*Receive emergency messages*

#### Parameters

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| tagId | string | Recipient MAC Address | examples (`"aa:bb:cc:dd:ee:ff"`) | pattern (`^([A-F0-9]{2}:){5}[A-F0-9]{2}$`) | **required** |


#### `mqtt` Operation specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| qos | - | - | `"1"` | - | - |
| retain | - | - | `"false"` | - | - |

#### Message Emergency Message `message`

* Content type: [application/json](https://www.iana.org/assignments/media-types/application/json)

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| id | integer | Server generated ID, used in message reports. | - | format (`int64`) | **required** |
| groupMessageId | integer | Indicates if message is part of a group message with multiple recipients, -1 means not part of group message. | default (`-1`) | format (`int64`) | - |
| from | object | - | - | - | **additional properties are allowed** |
| from.type | string | Type of id | allowed (`"NO_TYPE"`, `"USER"`, `"MAC"`, `"EMAIL"`, `"PHONENUMBER"`) | - | - |
| from.id | string | Representation of the specified ID type | - | - | - |
| to | object | - | - | - | **required**, **additional properties are allowed** |
| to.type | string | Type of id | allowed (`"NO_TYPE"`, `"USER"`, `"MAC"`, `"EMAIL"`, `"PHONENUMBER"`) | - | - |
| to.id | string | Representation of the specified ID type | - | - | - |
| contentType | string | The data content type. How to decode data. "text/plain" is the only one currently used, this text is encoded with UTF-8. | default (`"text/plain"`), allowed (`"text/plain"`) | - | **required** |
| subject | string | Subject of message | - | - | - |
| data | array<integer> | Content of message | - | - | - |
| data (single item) | integer | - | - | format (`byte`) | - |
| priority | string | Priority of message, Messages from Mobilaris Emergency Support will have `EMERGENCY_PRIORITY`. Messages with other priorities are used by other applications and should be disregarded for an emergency support integration. | allowed (`"NO_PRIORITY"`, `"LOW_PRIORITY"`, `"MEDIUM_PRIORITY"`, `"HIGH_PRIORITY"`, `"EMERGENCY_PRIORITY"`) | - | **required** |
| status | string | A message can have the following statuses:  \|Status\|Description\| \|---\|---\| \| NOT_SENT \| Mobilaris Message Adapter has received the message \| \| SENT \| Mobilaris Message Adapter has verified that RabbitMQ has received and stored the message \| \| DELIVERED \| Mobilaris Message Adapter has received a MessageReport from the client with the state DELIVERED \| \| ACKNOWLEDGED \| Mobilaris Message Adapter has received the message \| | allowed (`"NOT_SENT"`, `"SENT"`, `"DELIVERED"`, `"ACKNOWLEDGED"`) | - | **required** |
| statusReady | string | The sender of the message can set the expected actions of a client:  \|Status\|Description\| \|---\|---\| \|SENT\|No MessageReport expected from the client\| \|DELIVERED\|The client is expected to send a MessageReport with state DELIVERED after successfully reading a message\| \|ACKNOWLEDGED\|The client is expected to send a MessageReport with state ACKNOWLEDGED after receiving user input\| | allowed (`"SENT"`, `"DELIVERED"`, `"ACKNOWLEDGED"`) | - | **required** |
| errorCode | integer | Clients should always look for errors and disregard content if it is a non-zero value.  \|Value\|Description\| \|---\|---\| \|0\|No error\| \|1\|Internal Error\| \|100\|Server publish failed, Rejected by RabbitMQ\| \|101\|Timeout when publishing to Rabbit MQ\| | allowed (`0`, `1`, `100`, `101`) | - | **required** |
| createdTime | integer | Time of message creation, Unix timestamp | - | format (`int64`) | **required** |
| statusChangedTime | integer | Time of latest status update to message, Unix timestamp | - | format (`int64`) | - |

> Examples of payload _(generated)_

```json
{
  "id": 1,
  "groupMessageId": -1,
  "from": {
    "type": "MAC",
    "id": "aa:bb:cc:11:22:33"
  },
  "to": {
    "type": "MAC",
    "id": "aa:bb:cc:11:22:33"
  },
  "contentType": "text/plain",
  "subject": "Subject",
  "data": [
    84,
    69,
    83,
    84,
    32,
    84,
    69,
    83,
    84,
    32,
    84,
    69,
    83,
    84
  ],
  "priority": "EMERGENCY_PRIORITY",
  "status": "DELIVERED",
  "statusReady": "ACKNOWLEDGED",
  "errorCode": 0,
  "createdTime": 1622706641452,
  "statusChangedTime": 1622706641470
}
```



### PUB `amq_topic/json-reports` Operation

*Receive emergency messages*

#### `mqtt` Operation specific information

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| qos | - | - | `"1"` | - | - |
| retain | - | - | `"false"` | - | - |

#### Message Message Report `messageReport`

##### Payload

| Name | Type | Description | Value | Constraints | Notes |
|---|---|---|---|---|---|
| (root) | object | - | - | - | **additional properties are allowed** |
| id | integer | ID of the message the report is about | - | - | - |
| status | string | A message can have the following statuses:  \|Status\|Description\| \|---\|---\| \| NOT_SENT \| Mobilaris Message Adapter has received the message \| \| SENT \| Mobilaris Message Adapter has verified that RabbitMQ has received and stored the message \| \| DELIVERED \| Mobilaris Message Adapter has received a MessageReport from the client with the state DELIVERED \| \| ACKNOWLEDGED \| Mobilaris Message Adapter has received the message \| | allowed (`"NOT_SENT"`, `"SENT"`, `"DELIVERED"`, `"ACKNOWLEDGED"`) | - | - |

> Examples of payload _(generated)_

```json
{
  "id": 1,
  "status": "DELIVERED"
}
```



