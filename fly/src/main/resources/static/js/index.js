let scanButton = document.getElementById('scanPerimeterBtn');
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
        });

        stompClient.subscribe('/topic/newPerimeterPoint', function (message) {
            const radarPerimeter = JSON.parse(message.body);
            fillPerimeterMap(radarPerimeter)
            drawPerimeter();
        });
    });
}

function fillPerimeterMap(radarPerimeter) {
    perimeterMap = [];
    radarPerimeter.points.forEach(radarPoint => {
        const angle = radarPoint.angle;
        const distance = radarPoint.distance;

        perimeterMap.push({ angle, distance: distance });
    });
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
        perimeterItems[i].addEventListener("click", () => {
            choosePerimeterItem(i);
            let id = perimeterItems[i].id.slice(1);
            getAndDrawPerimeterPoints(id);
        });
    }
}

function addListener() {
    let perimeterItems = document.querySelectorAll(".perimeterItem");

    perimeterItems[perimeterItems.length - 1].addEventListener("click", () => {
        choosePerimeterItem(perimeterItems.length - 1);
    });

    choosePerimeterItem(perimeterItems.length - 1);
}

addListeners();
connectWebSocket();