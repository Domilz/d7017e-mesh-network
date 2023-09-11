# Mesh Networking for Distributed Underground Positioning Applications 
- [Mesh Networking for Distributed Underground Positioning Applications](#mesh-networking-for-distributed-underground-positioning-applications)
  - [Background](#background)
  - [Target](#target)
  - [Mission](#mission)
  - [Technical objectives](#technical-objectives)
    - [Protocol](#protocol)
    - [Transport](#transport)
    - [Android App](#android-app)
    - [Backend](#backend)
  - [Acceptance tests](#acceptance-tests)
    - [Test 1 Offline Tag to RP positioning](#test-1-offline-tag-to-rp-positioning)
    - [Test 2 Estimated Offline tag positioning](#test-2-estimated-offline-tag-positioning)
    - [Test 3 Chained offline tag positioning](#test-3-chained-offline-tag-positioning)
- [APIs](#apis)
  - [State propagation (example)](#state-propagation-example)
  - [reference points](#reference-points)
  - [rssi](#rssi)
  - [xyz](#xyz)

## Background
Many mines today are currently in an expansive digitalization process. This includes using digital communication tools for decision support and situational awareness. Epiroc Mining Intelligence provides a product portfolio in this area that enables tracking the real-time position of persons and vehicles in the mine for improved mine safety and productivity. Approximate positions are calculated based on real time position telemetry from some type of tags (usually a smartphone app or hardware-based alternative) that are always connected. A prerequisite for these tags to work is that the mine must investment heavily into either a Wi-Fi or LTE based networking infrastructure and even then, there will inevitably be blind spots in the network.  

## Target
This project is intended to investigate the possibility of using Mesh Networking techniques to enable communication of mission critical telemetry used for locating people, vehicles and other devices in underground areas with poor or no network coverage. 

## Mission
Your mission will be to investigate the concept of Mesh Networking as a complement to always-connected tags for positioning. Mesh enabled tags would, unlike the always-connected tags, work offline by storing a state of the latest observed position telemetry that can then be propagated wirelessly to nearby tags. Position telemetry will mainly include readings of different IoT devices like Bluetooth Low Energy (BLE) beacons. Once a tag reaches a central access point it can report its local state of positioning telemetry to the backend where the actual position is determined. Some of the BLEs will have fixed positions in the mine and can therefore be used as reference points when calculating the position of moving entities such as persons, vehicles and other machinery. Using this method, it is possible to estimate the position of persons and assets in areas without network coverage as long as some tag eventually reaches a connected access point.

## Technical objectives
The project contains some key technical components which can be investigated. This list includes suggestions of sub-objectives that could be implemented in parallel.

### Protocol
Design a protocol for peer-to-peer positioning state propagation. This includes coming up with an algorithm for efficient synchronization and filtration of a distributed state of position telemetry between tags and the central positioning service. 

### Transport
Investigate different transport layer alternatives like [Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview) or [Wi-Fi Aware](https://developer.android.com/guide/topics/connectivity/wifi-aware) and pick one that can best fulfill the requirements of the protocol while staying as battery efficient as possible. This includes coming up with a new or existing peer discovery method for spontaneously establishing two-way communications between two devices or a cluster of nearby devices.

### Android App
Creating a POC (Proof of concept) android application. The app should: 
- implement the [state propagation protocol](#state-propagation-example)
- use the chosen method of [transport](#transport) to continuously discover peers for peer-to-peer state propagation
- always try to connect to the central [backend service](#backend) for central state propagation
- use a [foreground service](https://developer.android.com/guide/components/foreground-services) to remain active in background even if the screen is locked or the application is closed.
- implement a simple BLE scanner module used by the foreground service to [scan for nearby BLE devices](https://developer.android.com/guide/topics/connectivity/bluetooth/find-ble-devices).
- persist the current state on the device by utilizing some android persistance layer like [room](https://developer.android.com/training/data-storage/room)
- be portable and documented enough to include in some other application.  

Each component should have a clear separation of concern to allow for reuse of modules and parallelize work. It is also a good idea to estimate the spatial complexity as a function of the number of tags and reference points.

### Backend
Create a poc backend that should: 
- implement the [state propagation protocol](#state-propagation-example)
- serve a simple web-based front-end for displaying the current state
- Implement a client to fetch reference point data from our [reference point api](#reference-points)
- implement a client for publishing rssi-data to our [rssi api](#rssi) based on direct readings of reference points
- implement a position estimation algorithm for indirect tag readings
- implement a client for publishing the estimated positions through our [xyz api](#xyz)
- add persistance for the latest state

## Acceptance tests
A completed project should be able to pass the following acceptance tests. The positions could be verified either through the simple web-based front-end or through Situational awareness (SA) given that the backend integrates with our open positioning apis.

### Test 1 Offline Tag to RP positioning
This test aims to prove that it is possible to correctly position an offline tag at an offline reference point. At least two tags are required. 
1. Place a tag outside of network coverage (or equivalently disable its networking capabilities) and in range of an offline reference point like a BLE.
2. Move another tag into the range of the first tag.
3. Now move the second tag into network coverage and verify that it correctly reports its own position as well as the first tag position at the offline reference point.

### Test 2 Estimated Offline tag positioning
The goal of this test is to see that we can estimate the position of an offline tag based on indirect readings of reference points. At least two tags are required. 
1. Place a tag outside of network coverage (or equivalently disable its networking capabilities) and outside the range of any reference points.
2. Move another tag into the range of the first tag.
3. Now move the second tag into network coverage and make sure that it correctly reports its own position as well as the first tag position at the offline reference point but with a lower accuracy than the first test.

### Test 3 Chained offline tag positioning
The goal of this test is to see that we can estimate the position of an offline tag based on indirect readings of reference points made by some other offline tag. At least three tags are required for this test.
1. Place a tag (tag 1) outside of network coverage (or equivalently disable its networking capabilities) and within range of an offline reference point.
2. Move another tag (tag 2) into the range of the first tag.
3. Move tag 2 outside the range of the first tag and into the range of another offline reference point.
4. Move a third tag inside the range of tag 2 with both tags outside of network coverage.
5. Now move the third tag into network coverage and make sure that it correctly reports its own position as well as the position of tag 1 at the first reference point and tag 2 at the second reference point.

# APIs
Here is a list of APIs that may be relevant for the project. 

## State propagation (example)
To be defined. An api for synchronizing readings of different devices by two tags described as a [protobuf](https://protobuf.dev) service. Protocol buffers are an efficient way of sending data but other formats like json are also possible.

```protobuf
syntax = "proto3";

// Service for exchanging reading state between two tags
service StatePropagation {
    rpc Propagate(stream State) returns (stream State) {}
}

message State {
    string tag_id               = 1; // ID of tag
    repeated Reading readings   = 2; // List of readings made by reporter
}

message Reading {
    string tag_id                = 1; // ID of the tag
    string device_id             = 2; // ID of the reported device
    int rssi                     = 3; // Received signal strength indicator
    google.protobuf.Timestamp ts = 4; // Time reading was received
}
```

## reference points
[api specification](./asyncapi/api/reference-point/reference-point.yml)

```json
{
  "timestamp": {
    "seconds": 1639658351,
    "nanos": 329000000
  },
  "correlationId": "48fb4cd3-2ef6-4479-bea1-7c92721b988c",
  "operation": "POST",
  "operands": [
    {
      "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
      "rpId": "aa:bb:cc:dd:ee:f1",
      "name": "string",
      "description": "string",
      "createTime": {
        "seconds": 1639658351,
        "nanos": 329000000
      },
      "updateTime": {
        "seconds": 1639658351,
        "nanos": 329000000
      },
      "location": {
        "x": 0,
        "y": 0,
        "z": 0
      },
      "type": "WIFI",
      "wifiProperties": {
        "online": true,
        "locationStatus": "NORMAL",
        "ip": "192.168.0.1"
      },
      "bleProperties": {
        "active": true,
        "batteryPercentage": 100
      },
      "lteProperties": {},
      "vendor": "string",
      "vendorProperties": {}
    }
  ],
  "error": {
    "code": 0,
    "message": "string"
  }
}
```

## rssi
[api specification](./asyncapi/api/location_engine/rssi/rssi.yml)

**Example:**
```json
{
  "readings": [
    {
      "rp_id": "aa:bb:cc:dd:ee:f0",
      "rssi": -78,
      "tag_id": "aa:bb:cc:dd:ee:f1",
      "type": "BLE",
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
    }
  ]
}
```

## xyz
[api specification](./asyncapi/api/location_engine/xyz/xyz.yml)

**Example:**
```json
{
  "position": [
    {
      "x": 800.5,
      "y": 550,
      "z": -350.83,
      "accuracy": 5,
      "tag_id": "aa:bb:cc:dd:ee:f1",
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
    }
  ]
}
```