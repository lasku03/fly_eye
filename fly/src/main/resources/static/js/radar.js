let canvas = document.getElementById('radarCanvas');
let ctx = canvas.getContext('2d');

let radarRadius = canvas.width / 2;

let pointsMap = new Array(360).fill(null);
let perimeterMap = [];
let radarAngle = 0;
let centerPoint = {x: canvas.width / 2, y: canvas.height / 2};
let radarAngles = [];

function addAngle(newAngle) {
    radarAngles.push(newAngle);
    if (radarAngles.length > 50) radarAngles.shift();
}

// Función para dibujar el radar
function drawRadar() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Black backgroun
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Dibujar el círculo del radar
    ctx.beginPath();
    ctx.arc(canvas.width / 2, canvas.height / 2, radarRadius - 2, 0, Math.PI * 2); // Ajustar el radio para el borde
    ctx.strokeStyle = "green";
    ctx.lineWidth = 3;
    ctx.stroke();

    // Dibujar los círculos concéntricos (líneas de distancia)
    for (let i = 1; i <= 4; i++) {
        ctx.beginPath();
        ctx.arc(canvas.width / 2, canvas.height / 2, (i * (radarRadius - 2)) / 4, 0, Math.PI * 2); // Ajustar el radio para el borde
        ctx.strokeStyle = "green";
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    // Dibujar líneas divisorias (ángulos principales)
    for (let i = 0; i < 360; i += 45) {
        const angleRad = (i * Math.PI) / 180;
        const x = canvas.width / 2 + (radarRadius - 2) * Math.cos(angleRad);
        const y = canvas.height / 2 + (radarRadius - 2) * Math.sin(angleRad);

        ctx.beginPath();
        ctx.moveTo(canvas.width / 2, canvas.height / 2);
        ctx.lineTo(x, y);
        ctx.strokeStyle = "green";
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    drawLines();
}

function drawLines() {
    for (let i = 0; i < radarAngles.length; i++) {
        let j = radarAngles.length - i;
        let borderAnglePoint = polarToCartesian(radarAngles[j], 1)
        let opacity = 0.02 * (50 - i);
        let color = "rgba(43, 240, 75, " + opacity + ")";
        drawLine(color, centerPoint, borderAnglePoint); 
    }
}

function drawLine(color, initialPoint, endPoint) {
    ctx.strokeStyle = color;
    ctx.fillStyle = color;
    ctx.lineWidth = 4;

    ctx.beginPath();
    ctx.moveTo(initialPoint.x, initialPoint.y);
    ctx.lineTo(endPoint.x, endPoint.y);
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(initialPoint.x, initialPoint.y, 2, 0, Math.PI * 2);
    ctx.fill();
}

// Convert polar coordenates to cartesians
function polarToCartesian(angle, distance) {
    const angleRad = (angle * Math.PI) / 180;
    return {
        x: canvas.width / 2 + distance * (radarRadius - 2) * Math.cos(angleRad),
        y: canvas.height / 2 - distance * (radarRadius - 2) * Math.sin(angleRad) // Invertir Y para que crezca hacia arriba
    };
}

function paintPoints() {
    drawRadar();
    drawPerimeter();
    for (let angle = 0; angle < 360; angle++) {
        const distance = pointsMap[angle];
        if (distance !== null && distance > 0) {
            paintPoint(angle, distance);
        }
    }
}

function paintPoint(angle, distance) {
    // Convertir a coordenadas cartesianas
    const point = polarToCartesian(angle, distance);

    let endPoint;
    if (perimeterMap[angle].distance != 0) {
        endPoint = polarToCartesian(angle, perimeterMap[angle].distance);
    }
    else {
        endPoint = polarToCartesian(angle, 1);
    }
    let opacity = calculatePointOpacity(angle);
    let color = "rgba(255, 0, 0, " + opacity + ")"
    drawLine(color, point, endPoint);
}

function calculatePointOpacity(angle) {
    let i = 0;
    let opacity = 0;
    while (i < radarAngles.length && opacity == 0) {
        let j = radarAngles.length - i;
        if (angle == radarAngles[j]) {
            opacity = 0.2 + 0.016 * (50 - i);
        }
        i++;
    }
    if (opacity == 0) {
        opacity = 0.2;
    }
    return opacity;
}

function scanPerimeter() {
    // Solicitar el nombre del perímetro
    const perimeterName = prompt("Please enter the perimeter name:");

    // Si el usuario cancela o no ingresa un nombre, no proceder
    if (perimeterName === null || perimeterName.trim() === "") {
        alert("Scan canceled. No name was provided.");
        return;
    }

    perimeterMap = [];
    // Generar 360 valores aleatorios entre 0.8 y 1
    for (let angle = 0; angle < 360; angle++) {
        let randomDistance = (Math.random() * (1.05 - 0.8) + 0.8).toFixed(2); // Value between 0.8 and 1
        if (randomDistance > 1) {
            randomDistance = 0;
        }
        perimeterMap.push({ angle, distance: randomDistance });
    }

    drawPerimeter();

    saveScan(perimeterName);
}

function drawPerimeter() {
    drawRadar();

    perimeterMap.forEach(({ angle, distance }) => {
        if (distance !== 0) {
            const perimeterPoint = polarToCartesian(angle, distance);
            let borderAnglePoint = polarToCartesian(angle, 1)
            drawLine("rgb(0 155 255)", perimeterPoint, borderAnglePoint);
        }
    });
}


function saveScan(perimeterName) {
    const perimeterData = {
        name: perimeterName, // Nombre del perímetro
        points: perimeterMap // Lista de puntos
    };

    fetch('/perimeters/save-scan', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(perimeterData)
    })
        .then(response => response.json())
        .then(data => {
            console.log('Perimeter saved:', data);
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

// Mostrar lista de perímetros
document.getElementById('selectPerimeterBtn').addEventListener('click', function () {
    let chosenPerimeterItem = document.querySelector('.perimeterItem_chosen');
    
    // Verifica si se encontró el elemento
    if (chosenPerimeterItem) {
        let id = chosenPerimeterItem.id.slice(1);
        getAndDrawPerimeterPoints(id); // Devuelve el id del li encontrado
    }
});

function getAndDrawPerimeterPoints(id) {
    fetch(`/perimeters/${id}/points`)
        .then(response => response.json())
        .then(points => {
            perimeterMap = points
            drawPerimeter();
        })
        .catch(error => {
            console.error('Error fetching perimeter points:', error);
        });
}

// Función para seleccionar un perímetro
function selectPerimeter(perimeter) {
    // Aquí puedes hacer el dibujado de los puntos en el radar
    drawPerimeter(perimeter);
    closeModal();
}

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

function addListener() {
    let perimeterItems = document.querySelectorAll(".perimeterItem");

    perimeterItems[perimeterItems.length - 1].addEventListener("click", () => {
        choosePerimeterItem(perimeterItems.length - 1);
    });
}

function addListeners() {
    let perimeterItems = document.querySelectorAll(".perimeterItem");

    for (let i = 0; i < perimeterItems.length; i++) {        
        perimeterItems[i].addEventListener("click", () => {
            choosePerimeterItem(i);
        });
    }
}

function choosePerimeterItem(num) {
    let perimeterItems = document.querySelectorAll(".perimeterItem");
    for (let i = 0; i < perimeterItems.length; i++) {
        if (i == num) {
            perimeterItems[i].classList.toggle("perimeterItem_chosen");
        }
        else {
            perimeterItems[i].classList.remove("perimeterItem_chosen");
        }
    }
}


// Paint the radar
drawRadar();
addListeners();
connectWebSocket();