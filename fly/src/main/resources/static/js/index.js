// Show perimeters list
/*document.getElementById('selectPerimeterBtn').addEventListener('click', function () {
    let chosenPerimeterItem = document.querySelector('.perimeterItem_chosen');
    
    // Verify if the element was found
    if (chosenPerimeterItem) {
        let id = chosenPerimeterItem.id.slice(1);
        getAndDrawPerimeterPoints(id);
    }
});*/

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