let scanButton = document.getElementById('scanPerimeterBtn');
let stopButton = document.getElementById('stopRadarBtn');
let deleteButton = document.getElementById('deletePerimeterBtn');

scanButton.addEventListener("click", () => {
    // Show a dialog to introduce the name
    const perimeterName = prompt("Introduce the new perimeter name:");

    // If the user clicked "Acept" and didn't leave the name in blank
    if (perimeterName && perimeterName.trim() !== "") {
        if (perimeterName && perimeterName.trim() !== "") {
            fetch(`/perimeters/start/${perimeterName}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Server error: ${response.statusText}`);
                    }
                    pointsMap = new Array(360).fill(null);
                    return response.text();
                })
                .then(result => {
                    console.log("Perimeter scan started:", result);
                })
                .catch(error => {
                    console.error("Error starting perimeter scan:", error);
                    alert("Failed to start perimeter scan: " + error.message);
                });
        } else {
            alert("Please provide a valid name for the perimeter.");
        }
    }
});

stopButton.addEventListener("click", () => {
    stopDetections();
})

async function stopDetections() {
    try {
        const response = await fetch(`/stopRadar`); // Esperar la respuesta del servidor
        if (!response.ok) {
            throw new Error(`Server error: ${response.statusText}`); // Lanzar error si la respuesta no es OK
        }
        const result = await response.text(); // Procesar la respuesta como texto
        console.log("Radar stopped: ", result);
    } catch (error) {
        console.error("Error stopping the radar:", error);
        alert("Error stopping the radar: " + error.message);
    }
}


deleteButton.addEventListener("click", () => {
    let selectedPerimeter = document.querySelector(".perimeterItem_chosen");

    if (selectedPerimeter != null) {
        let id = selectedPerimeter.id.slice(1);

        fetch(`/perimeters/delete/${id}`, {
                method: 'DELETE',  // Usamos el método DELETE
                headers: {
                    'Content-Type': 'application/json',
                },
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Server error: ${response.statusText}`);
                }
                selectedPerimeter.remove();
                console.log(response.text());
                return response.text();
            })
            .catch(error => {
                console.error("Error deleting perimeter");
            });
    }
});

// WEB SOCKET
function connectWebSocket() {
    var socket = new SockJS('/radar-detection');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/points', function (message) {
            const radarPoint = JSON.parse(message.body);
            pointsMap[radarPoint.angle] = radarPoint.distance;
            addAngle(radarPoint.angle);
            paintPoints();
        });

        stompClient.subscribe('/topic/newPerimeter', function (message) {
            const radarPerimeter = JSON.parse(message.body);
            addPerimeterToList(radarPerimeter.perimeterID, radarPerimeter.name, radarPerimeter.date);
            perimeterMap = []
        });

        stompClient.subscribe('/topic/newPerimeterPoint', function (message) {
            const radarPoint = JSON.parse(message.body);
            fillPerimeterMap(radarPoint);
            drawPerimeter();
        });
    });
}

function fillPerimeterMap(radarPoint) {
    const angle = radarPoint.angle;
    const distance = radarPoint.distance;

    perimeterMap.push({ angle, distance: distance });
    addAngle(perimeterMap[perimeterMap.length - 1].angle);
}

function addPerimeterToList(id, name, date) {
    const perimeterList = document.getElementById("perimeterList");

    // Create li
    const newPerimeterItem = document.createElement("li");
    newPerimeterItem.id = `p${id}`;
    newPerimeterItem.className = 'perimeterItem';

    // Create ID and name span
    const idNameSpan = document.createElement("span");
    idNameSpan.innerHTML = `ID: ${id} &nbsp;&nbsp;Name: ${name}`;

    // Crea date span
    const formattedDate = date.slice(0, 19).replace("T", " ");
    const dateSpan = document.createElement("span");
    dateSpan.innerHTML = `&nbsp;&nbsp;Date: ${formattedDate}`;

    // Add spans to the li
    newPerimeterItem.appendChild(idNameSpan);
    newPerimeterItem.appendChild(document.createElement("br")); // Salto de línea
    newPerimeterItem.appendChild(dateSpan);

    // Add li to the list
    perimeterList.appendChild(newPerimeterItem);

    // Add listener
    addListener();
}


function addListeners() {
    let perimeterItems = document.querySelectorAll(".perimeterItem");

    for (let i = 0; i < perimeterItems.length; i++) {
        perimeterItems[i].addEventListener("click", async () => {
            choosePerimeterItem(i);
            let id = perimeterItems[i].id.slice(1);
            getAndDrawPerimeterPoints(id);
            stopDetections();
            await sleep(500);
            startDetections();
        });
    }
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}


function addListener() {
    let perimeterItems = document.querySelectorAll(".perimeterItem");

    perimeterItems[perimeterItems.length - 1].addEventListener("click", () => {
        choosePerimeterItem(perimeterItems.length - 1);
    });

    choosePerimeterItem(perimeterItems.length - 1);
}

async function startDetections() {
    pointsMap = new Array(360).fill(null);

    try {
        const response = await fetch(`/detections/start`); // Espera la respuesta
        const result = await response.json(); // Espera a procesar el JSON
        console.log("Resul: ", result);
    } catch (error) {
        console.error('Error starting detections: ', error);
    }
}


addListeners();
connectWebSocket();