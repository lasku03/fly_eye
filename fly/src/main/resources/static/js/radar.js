let canvas = document.getElementById('radarCanvas');
let ctx = canvas.getContext('2d');

let radarRadius = canvas.width / 2;

let pointsMap = new Array(360).fill(null);
let perimeterMap = [];
let centerPoint = { x: canvas.width / 2, y: canvas.height / 2 };
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

function choosePerimeterItem(num) {
    let perimeterItems = document.querySelectorAll(".perimeterItem");
    for (let i = 0; i < perimeterItems.length; i++) {
        if (i == num) {
            perimeterItems[i].classList.add("perimeterItem_chosen");
        }
        else {
            perimeterItems[i].classList.remove("perimeterItem_chosen");
        }
    }
}


// Paint the radar
drawRadar();