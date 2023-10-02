# d7017e - Mesh network for position propagation in underground mines
Implementing a mesh network for Epiroc in the course D7017E.

# Project overview
Our project seeks to support Epiroc in its efforts to improve mine safety and productivity in the quickly developing sector of mine digitalization. They want us to investigate Mesh Networking's potential for enabling vehicle and person tracking in underground spaces. Our project is supposed to be designed specifically for mines that has not yet fully digitized meaning that a connection over WiFi is not available through out the whole mine. To solve this a peer-to-peer positioning protocol is being developed as a part of this project, which also entails looking into options for an efficient transport layer, and developing a backend system with position estimation capabilities and an Android application to tie this all together.

<p align="center">
  <img src="https://github.com/Domilz/d7017e-mesh-network/blob/main/img/mesh.png" />
</p>

# Techical objectives
Briefly mentioned in project overiew, our project is devided into four parts.

## Protocol
Create a protocol for sharing of peer-to-peer positioning state. Develop an algorithm to effectively synchronize and filter the shared state of position information between tags and the centralized positioning service.

## Transport
Choose a transport layer choice that can best meet the protocol's needs while using the least amount of battery, such as Bluetooth Low Energy or Wi-Fi Aware. This comprises developing a fresh or established peer discovery technique to enable two devices or a group of nearby devices to spontaneously start two-way conversations.

## Android application
Create an Android POC (Proof of Concept) application. The app ought to:

- Put the state propagation mechanism into practice.
- Continually find partners using the specified transmission technique to enable peer-to-peer state propagation
- For central state propagation, try to connect to the backend service at all times.
- Even if the screen is locked or the program is closed, employ a foreground service to stay active in the background.
- Create a straightforward BLE scanner module that the foreground service may use to look for adjacent BLE gadgets.
- By leveraging an Android persistence layer like room, the current state may be kept on the device and is portable and well-documented enough to be used in other applications.

## Backend 
Create a poc backend that should:
- implement the state propagation protocol
- create a client to access Epirocs reference point API and retrieve reference point data.
- based on direct readings of reference points, create a client for posting RSS-data to Epirocs rssi API.
- a position estimate mechanism should be implemented for indirect tag readings.
- create a client for Epirocs xyz api to publish the estimated positions
- extend persistence to the most recent state

# Roadmap
This project is planned to be finished by mid December when a presentaion will be made at LTU and a reprot will be submitted describing how the project was made.
