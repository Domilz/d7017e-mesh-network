// Import the Three.js library (make sure you have it in your project folder)
import { OrbitControls } from "https://unpkg.com/three@0.112/examples/jsm/controls/OrbitControls.js";

let socket;
// Initialize Three.js scene
const scene = new THREE.Scene();

// Create a camera
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
camera.position.z = 5;


// Create a WebGL renderer
const renderer = new THREE.WebGLRenderer({ canvas: document.getElementById('plot') });

renderer.setSize(document.getElementById('plot-container').clientWidth, document.getElementById('plot-container').clientHeight);



//Setup for Materials
const defaultDirectTagReadingMaterial = new THREE.MeshBasicMaterial({ color: 0x3A7500 });
const highlightedDirectTagReadingMaterial = new THREE.MeshBasicMaterial({ color: 0x7AF300 });
const defaultIndirectTagReadingMaterial = new THREE.MeshBasicMaterial({ color: 0x724800 });
const highlightedIndirectTagReadingMaterial = new THREE.MeshBasicMaterial({ color: 0xFFB73E });

const defaultBeaconMaterial = new THREE.MeshBasicMaterial({ color: 0x00328A });
const highlightedBeaconMaterial = new THREE.MeshBasicMaterial({ color: 0x005CFF });

// other
var controls = new OrbitControls(camera, renderer.domElement);
const size = 10;
const divisions = 10;

const gridHelper = new THREE.GridHelper(size, divisions);
scene.add(gridHelper);
const axesHelper = new THREE.AxesHelper(5);
scene.add(axesHelper);


// Create empty arrays to hold tag and beacon objects
const tagsArray = [];
const beaconsArray = [];



class TagReading {
    constructor(tagId, rpId, accuracy, date, readingType, x, y, z) {
        updateLogHandler.writeToLog("Received reading for the new tag: " + tagId);
        this.tagId = tagId;
        this.rpId = rpId;
        this.accuracy = accuracy;
        this.date = date;
        this.readingType = readingType;
        this.geometry = new THREE.SphereGeometry(0.1);
        this.isHighlighted = false;
        this.setCorrectMaterial();
        if (readingType == "directReading") {
            console.log("inside directReading ");
            this.mesh = new THREE.Mesh(this.geometry, this.material);

            var pos = getPositionFromRpId(rpId);
            if (pos !== undefined) {
                this.mesh.position.set(pos.x, pos.y, pos.z);
                scene.add(this.mesh);
                tagsArray.push(this);
                this.addToTableRow();
                updateLogHandler.writeToLog("Added the received reading of the new tag: " + tagId);

            }
            else {
                updateLogHandler.writeToLog("Could not add new tag: " + tagId) + ". Could not find a beacon for the rpId: " + rpId + ", it is requierd for directReadings";
            }


        }
        if (readingType == "indirectReading") {
            console.log("inside indirectReading ")
            this.mesh = new THREE.Mesh(this.geometry, this.material);
            this.mesh.position.set(x, y, z);
            scene.add(this.mesh);
            console.log(this);
            tagsArray.push(this);
            this.addToTableRow();
            updateLogHandler.writeToLog("Added the received reading of the new tag: " + tagId);
        }

    }
    updateTagReading(rpId, accuracy, date, readingType, x, y, z) {
        updateLogHandler.writeToLog("Received reading for the existing tag: " + this.tagId);
        this.rpId = rpId;
        this.accuracy = accuracy;
        this.date = date;
        this.readingType = readingType;
        if (readingType == "directReading") {
            var pos = getPositionFromRpId(rpId);
            this.mesh.position.set(pos.x, pos.y, pos.z);


            var pos = getPositionFromRpId(rpId);
            if (pos !== undefined) {
                this.mesh.position.set(pos.x, pos.y, pos.z);
                this.setCorrectMaterial();
                this.mesh.material = this.material;
                this.mesh.material.needsUpdate = true;
                this.updateTableRow();
                updateLogHandler.writeToLog("Updated reading for the existing tag: " + this.tagId);

            }
            else {
                updateLogHandler.writeToLog("Could not update reading: " + tagId) + ". Could not find a beacon for the rpId: " + rpId + ", it is requierd for directReadings";
            }


        }
        if (readingType == "indirectReading") {

            this.mesh.position.set(x, y, z);
            this.setCorrectMaterial();
            this.mesh.material = this.material;
            this.mesh.material.needsUpdate = true;
            this.updateTableRow();
            updateLogHandler.writeToLog("Updated reading for the existing tag: " + this.tagId);

        }

    }
    addToTableRow() {
        var table = document.getElementById('tagsTableBody');
        if (this.readingType == "directReading") {
            console.log("adding tagReading of type direct to table, data: ",);
            var row = `<tr class="item-id-${this.tagId}">  
            <td>${this.tagId}</td>
            <td>${this.rpId}</td>
            <td>${this.accuracy}</td>
            <td>${this.date}</td>
            <td>${this.readingType}</td>
            <td colspan="3">${this.rpId}</td>
            <td><button type="button" class="highlightTag-button">Highlight</button></td>`
            table.innerHTML += row;
        }

        if (this.readingType == "indirectReading") {
            console.log("adding tagReading of type indirect to table, data: ",);
            var row = `<tr class="item-id-${this.tagId}">  
            <td>${this.tagId}</td>
            <td>${this.rpId}</td>
            <td>${this.accuracy}</td>
            <td>${this.date}</td>
            <td>${this.readingType}</td>
            <td>${this.mesh.position.x}</td>
            <td>${this.mesh.position.y}</td>
            <td>${this.mesh.position.z}</td>
            <td><button type="button" class="highlightTag-button">Highlight</button></td>`
            table.innerHTML += row;

        }



    }
    updateTableRow() {
        if (this.readingType == "directReading") {
            console.log("adding tagReading of type direct to table, data: ",);
            $(`.item-id-${this.tagId}`).replaceWith(`
            <tr class="item-id-${this.tagId}">
              <td>${this.tagId}</td>
              <td>${this.rpId}</td>
              <td>${this.accuracy}</td>
              <td>${this.date}</td>
              <td>${this.readingType}</td>
              <td colspan="3">${this.rpId}</td>

              <td><button type="button" class="highlightTag-button">Highlight</button></td>
            </tr>`);
        }

        if (this.readingType == "indirectReading") {
            console.log("adding tagReading of type indirect to table, data: ",);
            console.log("update tag for : ", this.tagId);
            console.log("update tag for : ", this);
            $(`.item-id-${this.tagId}`).replaceWith(`
              <tr class="item-id-${this.tagId}">
                <td>${this.tagId}</td>
                <td>${this.rpId}</td>
                <td>${this.accuracy}</td>
                <td>${this.date}</td>
                <td>${this.readingType}</td>
                <td>${this.mesh.position.x}</td>
                <td>${this.mesh.position.y}</td>
                <td>${this.mesh.position.z}</td>
                <td><button type="button" class="highlightTag-button">Highlight</button></td>
              </tr>`);

        }



    }

    toggleHighlight() {
        if (this.isHighlighted) {
            this.isHighlighted = false;
        }
        else {
            this.isHighlighted = true;
        }
        this.setCorrectMaterial();
        this.mesh.material = this.material;
        this.mesh.material.needsUpdate = true;

    }
    setCorrectMaterial() {
        if (this.readingType == "directReading") {


            if (this.isHighlighted) {
                this.material = highlightedDirectTagReadingMaterial;


            }
            else {
                this.material = defaultDirectTagReadingMaterial;

            }

        }
        if (this.readingType == "indirectReading") {

            if (this.isHighlighted) {
                this.material = highlightedIndirectTagReadingMaterial;
            }
            else {
                this.material = defaultIndirectTagReadingMaterial;
            }

        }

    }


}

class Beacon {
    constructor(rpId, x, y, z) {
        this.rpId = rpId;
        this.geometry = new THREE.CylinderGeometry(.2, .2, .1, 32);
        this.isHighlighted = false;
        this.setCorrectMaterial();
        this.mesh = new THREE.Mesh(this.geometry, this.material);
        this.mesh.position.set(x, y, z);

        scene.add(this.mesh);
        beaconsArray.push(this);
        this.addToTableRow();


    }

    updateBeacon(x, y, z) {
        this.mesh.position.set(x, y, z);
        this.updateTableRow();

    }

    addToTableRow() {
        var table = document.getElementById('beaconsTableBody');
        console.log("adding beacon to the table, ",);
        var row = `<tr class="item-id-${this.rpId}">  
            <td>${this.rpId}</td>
            <td>${this.mesh.position.x}</td>
            <td>${this.mesh.position.y}</td>
            <td>${this.mesh.position.z}</td>
            <td><button type="button" class="highlightBeacon-button">Highlight</button></td>`
        table.innerHTML += row;

    }

    updateTableRow() {
        console.log("updateing table row for beacon ");
        $(`.item-id-${this.rpId}`).replaceWith(`
              <tr class="item-id-${this.rpId}">
                <td>${this.rpId}</td>
                <td>${this.mesh.position.x}</td>
                <td>${this.mesh.position.y}</td>
                <td>${this.mesh.position.z}</td>
                <td><button type="button" class="highlightBeacon-button">Highlight</button></td>
              </tr>`);

    }

    toggleHighlight() {
        if (this.isHighlighted) {
            this.isHighlighted = false;
        }
        else {
            this.isHighlighted = true;
        }
        this.setCorrectMaterial();
        this.mesh.material = this.material;
        this.mesh.material.needsUpdate = true;

    }
    setCorrectMaterial() {
        if (this.isHighlighted) {
            this.material = highlightedBeaconMaterial;


        }
        else {
            this.material = defaultBeaconMaterial;

        }

    }

}

class UpdateLogHandler {
    constructor() {
        this.enabled = false;
    }
    enableLog() {
        this.enabled = true;
    }
    writeToLog(data) {
        if (this.enabled) {
            var table = document.getElementById('updateLogTableBody');
            var row = ` 
            <td>${data}</td>`
            table.innerHTML += row;

        }

    }
}

const updateLogHandler = new UpdateLogHandler();
// Function to update the position of a tag or add it if it does not exist
function setTagPosition(data) {
    console.log("setTagPosition got data:", data)
    const tag = tagsArray.find((t) => t.tagId === data.tagId);
    if (tag) {
        if (data.readingType == "directReading") {
            tag.updateTagReading(data.rpId, data.accuracy, data.date, data.readingType, data.position.x, data.position.y, data.position.z);
        }
        if (data.readingType == "indirectReading") {

            tag.updateTagReading(data.rpId, data.accuracy, data.date, data.readingType, data.position.x, data.position.y, data.position.z);
        }


    }


    //No tag with a matching tagId exists
    else {
        console.log("creating new tagreading")
        new TagReading(data.tagId, data.rpId, data.accuracy, data.date, data.readingType, data.position.x, data.position.y, data.position.z);


    }
}


// Function to update the position of a beacon or add it if it does not exist
function setBeaconPosition(data) {
    console.log("setBeaconPosition got data:", data)
    const beacon = beaconsArray.find((b) => b.beaconId === data.rpId);
    if (beacon) {
        beacon.updateBeacon(data.position.x, data.position.y, data.position.z);

    }

    //No beacon with a matching rpId exists
    else {
        new Beacon(data.rpId, data.position.x, data.position.y, data.position.z);
    }
}


function getPositionFromRpId(beaconId) {
    const beacon = beaconsArray.find((b) => b.rpId === beaconId);
    if (beacon) {
        return beacon.mesh.position;
    }
    else {
        console.log("no beacons found for beaconId: ", beaconId);
    }
}






// Event listener for tab clicks Hides the beacons table and shows the tags table
document.getElementById('tagsTab').addEventListener('click', () => {

    document.getElementById("beaconsTable").style.display = "none";

    document.getElementById("tagsTable").style.display = "inline";



});

// Event listener for tab clicks Hides the tags table and shows the beacons table
document.getElementById('beaconsTab').addEventListener('click', () => {
    document.getElementById("tagsTable").style.display = "none";
    document.getElementById("beaconsTable").style.display = "inline";

});


//EventListener for when highlight button for tags is pressed
document.getElementById('tagsTableBody').addEventListener('click', function (e) {

    console.log("inside listener");
    if (!e.target.classList.contains('highlightTag-button'))
        return;
    var str = String(e.target.closest('tr').className);
    var tName = str.replace("item-id-", "");
    console.log("comp ", tName);
    const tag = tagsArray.find((t) => t.tagId === tName);
    if (tag) {
        console.log("Highlighting tag", tag);
        tag.toggleHighlight();
    }
    else {
        console.log("Could not find tagID in tagsArray ");

    }

});

//EventListener for when highlight button for beacons is pressed
document.getElementById('beaconsTableBody').addEventListener('click', function (e) {

    if (!e.target.classList.contains('highlightBeacon-button'))
        return;
    var str = String(e.target.closest('tr').className);
    var tName = str.replace("item-id-", "");
    console.log("comp ", tName);
    const beacon = beaconsArray.find((t) => t.rpId === tName);
    if (beacon) {
        console.log("Highlighting beacon", beacon);
        beacon.toggleHighlight();




    }
    else {
        console.log("Could not find rpId in beaconsArray ");

    }

});







// Function to handle window resize
function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(document.getElementById('plot-container').clientWidth, document.getElementById('plot-container').clientHeight);

}

window.addEventListener('resize', onWindowResize);

// Animation loop
function animate() {
    requestAnimationFrame(animate);
    renderer.render(scene, camera);
}




function setupWebSockets() {
    const socket = new WebSocket("ws://83.233.46.128:4242/websocket");
    socket.onopen = (event) => {
        // WebSocket connection is open
        console.log("WebSocket connection opened.");


        socket.send("getInitialData"); //Asks to get initial data from the server


        socket.onmessage = (ev) => {

            const data = JSON.parse(ev.data);
            // Process the initial data here
            var beacons = data.beacons;
            if (beacons != undefined) {
                for (var i = 0; i < beacons.length; i++) {
                    setBeaconPosition(beacons[i]);
                }
            }
            var tags = data.tags;
            if (tags != undefined) {
                for (var i = 0; i < tags.length; i++) {
                    setTagPosition(tags[i]);
                }
            }
            updateLogHandler.enableLog();


        };
    };



    socket.onclose = (event) => {
        console.log("WebSocket connection closed");
        /*
        console.log("WebSocket connection closed. Attempting to reconnect...");
        setTimeout(setupWebSockets, 1000); // Reconnect after a delay (e.g., 1 second)
        */
    };

}

setupWebSockets();



animate();

