let canvas = document.getElementById('radarCanvas');
let ctx = canvas.getContext('2d');

let radarRadius = canvas.width / 2;

let pointsMap = new Array(360).fill(null);
let perimeterMap = [];
let radarAngle = 0;
let centerPoint = {x: canvas.width / 2, y: canvas.height / 2};

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

    let borderAnglePoint = polarToCartesian(radarAngle, 1)
    drawLine("#2bf04b", centerPoint, borderAnglePoint);    
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
    ctx.arc(centerPoint.x, centerPoint.y, 2, 0, Math.PI * 2);
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

    // Dibujar el punto en el radar
    ctx.strokeStyle = "red";
    ctx.lineWidth = 4;
    ctx.fillStyle = "red";

    if (perimeterMap[angle] != 0) {
        let perimeterPoint = polarToCartesian(angle, perimeterMap[angle].distance);
        drawLine("#2bf04b", point, perimeterPoint);
    }
    else {
        let borderPoint = polarToCartesian(angle, 1);
        drawLine("#2bf04b", point, borderPoint);
    }
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

    let previousPoint = null;
    ctx.strokeStyle = "rgb(0 155 255)";
    ctx.lineWidth = 1;
    ctx.fillStyle = "rgb(0 155 255)";

    perimeterMap.forEach(({ angle, distance }) => {
        const point = polarToCartesian(angle, distance);

        if (distance !== 0) {
            if (previousPoint) {
                // If there is a previous point, draw a line between both of them
                ctx.beginPath();
                ctx.moveTo(previousPoint.x, previousPoint.y);
                ctx.lineTo(point.x, point.y);
                ctx.stroke(); // Pain the line
            }
            // Draw this point
            ctx.beginPath();
            ctx.arc(point.x, point.y, 1, 0, Math.PI * 2);
            ctx.fill();

            previousPoint = point;
        } else {
            previousPoint = null;
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


/*document.getElementById('selectPerimeterBtn').addEventListener('click', function () {
    fetch('/perimeters')
        .then(response => response.json())
        .then(data => {

            const perimeterOptions = data.map(perimeter => {
                // Modificar la cadena de fecha directamente si ya está en formato ISO
                const formattedDate = perimeter.date.slice(0, 19).replace("T", " ");
                return `ID: ${perimeter.perimeterID}, Name: ${perimeter.name}, Date: ${formattedDate}`;
            }).join('\n');

            const selectedPerimeterId = prompt(`Select a Perimeter:\n${perimeterOptions}`);

            if (selectedPerimeterId) {
                // Lógica para encontrar el perímetro correspondiente por ID
                const selectedPerimeter = data.find(perimeter => perimeter.perimeterID.toString() === selectedPerimeterId);
                if (selectedPerimeter) {
                    getAndDrawPerimeterPoints(selectedPerimeter); // Implementa esta función para dibujar el perímetro
                } else {
                    alert('Invalid perimeter ID selected.');
                }
            }
        })
        .catch(error => {
            console.error('Error fetching perimeters:', error);
        });
});*/

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

// Función para cerrar el modal
function closeModal() {
    document.getElementById('perimeterListModal').style.display = 'none';
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
            radarAngle = radarPoint.angle;
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
    radarAngle = perimeterMap[perimeterMap.length - 1].angle;
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