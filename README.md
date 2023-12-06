# d7017e - Mesh network for position propagation in underground mines
Implementing a mesh network for Epiroc in the course D7017E.

 # Table of content:
 1. [Installation guide](#installation-guide)
    1. [Prerequisites](#prerequisites)
    2. [Local/Dev Backend Setup](#backend-setup)
    3. [Local/Dev Android App Setup](#app-setup)
 3. [Project overview](#project-overview)
 4. [Technical objectives](#technical-objectives)
    1. [Protocol](#protocol)
    2. [Transport](#transport)
    3. [Android Application](#android-application)
    4. [Backend](#backend)
  5. [Roadmap](#roadmap)

## Installation guide

### Prerequisites <a id="prerequisites"></a>
1. [Go](https://go.dev/)
2. Android 12L(API level 32) mobile.
3. Wi-Fi antennas compatible with [Wi-Fi Aware](https://www.wi-fi.org/discover-wi-fi/wi-fi-aware)

### Local/Dev Backend Setup <a id="backend-setup"></a>

1. Clone repository and navigate to correct folder:

   `$ git clone https://github.com/Domilz/d7017e-mesh-network`

   `$ cd d7017e-mesh-network`

2. Setting up required `.env` file.   

   `$ touch .env`

   Add these following fields to the created `.env` file:

   `SERVER_ADDRESS=Required` The address of the **server**. This can be set up to run on your server or `localhost`, though the Android application requires communication with an actual server to function. <br />
   `SERVER_PORT=Required` The port of the **server**. <br />
   `CLIENT_PORT=Required` The port of the **client**. <br />

3. Building and running the application.

   To create a binary a Windows operating system, the Go compiler relies on the MinGW-w64 toolchain, which includes GCC (GNU Compiler Collection). Download it [here](https://www.msys2.org/) and follow the instructions on that page. If you are running the application on a **Linux** operating system this is not necessary. Then simply run the script `build_and_run.sh` with the following line in the terminal: 

   [Gitbash](https://git-scm.com/downloads) and Linux system command lines :
   `./build_and_run.sh`

   Powershell:
   `.\build_and_run.sh`

   Command Prompt:
   `bash build_and_run.sh`

4. After running step three your terminal should output something like this:

   ```
   Building the application...
   Build successful.
   Running the executable...
   2023/12/04 15:15:46 Starting Backend
   2023/12/04 15:15:46 Successfully connected to DebugLogDatabase
   2023/12/04 15:15:46 Starting debugLog server
   2023/12/04 15:15:46 Successfully connected to SentLogDatabase
   2023/12/04 15:15:46 Starting sentLog server
   2023/12/04 15:15:46 Successfully connected to StateDatabase
   2023/12/04 15:15:46 StateDatabaseHandler loaded 5 readings from DB, loaded readings: [{{{} [] [] <nil>} 0 [] tagId1 rpId1 69 seconds:1699882836 nanos:107021700 1} {{{} [] [] <nil>} 0 [] tagId2 rpId2 69 seconds:1699882836 nanos:107021700 1} {{{} [] [] <nil>} 0 [] tagId3 rpId3 69 seconds:1699886406 nanos:202200100 1} {{{} [] [] <nil>} 0 [] tagId4 rpId14 69 seconds:1700486949 nanos:996846200 1} {{{} [] [] <nil>} 0 [] tagId5 rpId6 69 seconds:1700480573 nanos:348733600 1}]
   2023/12/04 15:15:46 Starting state server
   Server is running on port:  4242
   2023/12/04 15:15:46 grpcServer listening at port : 50051
   ```

### Local/dev Android App Setup <a id="app-setup"></a>

1. Setting up the required config.json file:
   Add information to the following fields to the config.json file located under ...\Android\app\src\main\res\raw
    ```
    {
   "aware_discovery_passphrase": ,
   "aware_service_name": ,
   "local_storage_file_name": ,
   "backend_ip": ,
   "server_address":
    }
    ```
2. Generating the `.apk` file:
    To create the `.apk` file based on the config file, change directory to the root of the project and run the following commands:

    Commands on Linux system command lines:
    ```
    $ ./gradlew assembleDebug
    ```

    Commands on Windows system command lines:
    ```
    > ./gradlew.bat assembleDebug
    ```
3. Finding the generated `.apk` file:
   After running step two, your terminal should output something like this:
   ```
   BUILD SUCCESSFUL in 52s
   ```
   if that is the case, the generated .apk file can be found under
   ```
   ...\Android\app\build\outputs\apk\debug
   ```
   Otherwise, follow try solving the error in the terminal.
  
5. Installing the `.apk` on the phone:
   Connect the phone to the computer where the .apk file exists. Open the file explorer and
   navigate to the phone directory. Drag and drop the .apk file into the directory. Now
   disconnect the phone from the computer and navigate to the .apk on the phone. Click the
   .apk file and install the app. When the installation is finished, simply open the app.


## Project overview <a id="project-overview"></a>
Our project seeks to support Epiroc in its efforts to improve mine safety and productivity in the quickly developing sector of mine digitalization. They want us to investigate Mesh Networking's potential for enabling vehicle and person tracking in underground spaces. Our project is supposed to be designed specifically for mines that has not yet fully digitized meaning that a connection over WiFi is not available through out the whole mine. To solve this a peer-to-peer positioning protocol is being developed as a part of this project, which also entails looking into options for an efficient transport layer, and developing a backend system with position estimation capabilities and an Android application to tie this all together.

<p align="center">
  <img src="https://github.com/Domilz/d7017e-mesh-network/blob/main/img/mesh.png" />
</p>

## Techical objectives <a id="technical-objectives"></a>
Briefly mentioned in project overiew, our project is devided into four parts.

### Protocol <a id="protocol"></a>
Create a protocol for sharing of peer-to-peer positioning state. Develop an algorithm to effectively synchronize and filter the shared state of position information between tags and the centralized positioning service.

### Transport <a id="transport"></a>
Choose a transport layer choice that can best meet the protocol's needs while using the least amount of battery, such as Bluetooth Low Energy or Wi-Fi Aware. This comprises developing a fresh or established peer discovery technique to enable two devices or a group of nearby devices to spontaneously start two-way conversations.

### Android application <a id="android-application"></a>
Create an Android POC (Proof of Concept) application. The app ought to:

- Put the state propagation mechanism into practice.
- Continually find partners using the specified transmission technique to enable peer-to-peer state propagation
- For central state propagation, try to connect to the backend service at all times.
- Even if the screen is locked or the program is closed, employ a foreground service to stay active in the background.
- Create a straightforward BLE scanner module that the foreground service may use to look for adjacent BLE gadgets.
- By leveraging an Android persistence layer like room, the current state may be kept on the device and is portable and well-documented enough to be used in other applications.

## Backend <a id="backend"></a>
Create a poc backend that should:
- implement the state propagation protocol
- create a client to access Epirocs reference point API and retrieve reference point data.
- based on direct readings of reference points, create a client for posting RSS-data to Epirocs rssi API.
- a position estimate mechanism should be implemented for indirect tag readings.
- create a client for Epirocs xyz api to publish the estimated positions
- extend persistence to the most recent state

## Roadmap <a id="roadmap"></a>
This project is planned to be finished by mid December when a presentaion will be made at LTU and a reprot will be submitted describing how the project was made.
