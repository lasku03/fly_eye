let detectionMap = [];
let perimeterItems = document.querySelectorAll(".perimeterItem");
let goBackBtn = document.getElementById("goBackBtn");
let playBtn = document.getElementById("playBtn");
let stopBtn = document.getElementById("stopBtn");
let end = true;

goBackBtn.addEventListener("click", () => {
    let hiddenPerimeters = document.querySelectorAll(".visually-hidden.perimeterItem");

    if (hiddenPerimeters.length != 0) {
        togglePerimeters();
        toggleButtons();
        removeDetections();
        endAnimation();
        drawPerimeter();
    }
})

function removeDetections() {
    let detectionItems = document.querySelectorAll(".detectionItem");

    detectionItems.forEach(item => {
        item.remove();
    });

    detectionMap = [];
}

function addListeners() {
    for (let i = 0; i < perimeterItems.length; i++) {
        perimeterItems[i].addEventListener("click", () => {
            choosePerimeterItem(i);
            let id = perimeterItems[i].id.slice(1);
            getAndDrawPerimeterPoints(id);
            togglePerimeters();
            toggleButtons();
            getDetections(id);
        });
    }
}

function togglePerimeters() {
    for (let i = 0; i < perimeterItems.length; i++) {
        perimeterItems[i].classList.toggle("visually-hidden");
    }
}

function toggleButtons() {
    let buttonsDiv = document.getElementById("buttonsDiv");
    buttonsDiv.classList.toggle("visually-hidden");
}

function getDetections(perimeterID) {
    fetch(`/perimeters/${perimeterID}/detections`)
        .then(response => response.json())
        .then(detections => {
            detectionMap = detections;
            detectionMap.forEach(detection => {
                addDetectionToList(detection.detectionID, detection.date);
            })
        })
        .catch(error => {
            console.error('Error fetching perimeter detections:', error);
        });
}

function addDetectionToList(id, date) {
    const perimeterList = document.getElementById("perimeterList");

    // Create li
    const newDetectionItem = document.createElement("li");
    newDetectionItem.id = `d${id}`;
    newDetectionItem.className = 'detectionItem';

    // Create ID and name span
    const idNameSpan = document.createElement("span");
    const formattedDate = date.slice(0, 19).replace("T", " ");
    idNameSpan.innerHTML = `ID: ${id} &nbsp;&nbsp;Date: ${formattedDate}`;

    // Add span to the li
    newDetectionItem.appendChild(idNameSpan);

    // Add li to the list
    perimeterList.appendChild(newDetectionItem);

    // Add listener
    addDetectionListener(newDetectionItem);
}

function addDetectionListener(detectionItem) {
    detectionItem.addEventListener("click", () => {
        endAnimation();
        chooseDetectionItem(detectionItem, "detectionItem_chosen");
        let id = detectionItem.id.slice(1);
        drawDetectionPoints(id);
    });
}

function drawDetectionPoints(id) {
    let foundDetection = detectionMap.find(detection => String(detection.detectionID) === id);

    if (foundDetection != null) {
        pointsMap = new Array(360).fill(null);
        foundDetection.points.forEach(point => {
            pointsMap[point.angle] = point.distance; // Usa el índice necesario
        });
    }

    paintPoints();
}

function chooseDetectionItem(chosenDetectionItem, className) {
    let detectionItems = document.querySelectorAll(".detectionItem");
    detectionItems.forEach(detectionItem => {
        if (detectionItem == chosenDetectionItem) {
            detectionItem.classList.toggle(className);
        }
        else {
            detectionItem.classList.remove(className);
        }
    })
}

playBtn.addEventListener("click", async () => {
    let detectionItems = document.querySelectorAll(".detectionItem");

    if (detectionItems.length > 0) {
        if (!end) {
            endAnimation();
        }
        await sleep(20);
        if (document.querySelector(".detectionItem_chosen") != null) {
            for (let i = 0; i < detectionItems.length - 1; i++) {
                if (detectionItems[i].classList.contains("detectionItem_chosen")) {
                    end = false;
                    drawAnimation(detectionItems, i + 1);
                    break;
                }
            }
            if (end) {
                end = false;
                drawAnimation(detectionItems, 0);
            }
        }
        else {
            end = false;
            drawAnimation(detectionItems, 0);
        }
    }
})

async function drawAnimation(detectionItems, start) {
    let i = start;
    while (i < detectionMap.length && !end) {
        let j = 0;
        chooseDetectionItem(detectionItems[i], "animating");
        angleIndex = 0;
        if (detectionMap[i].direction == "COUNTERCLOCKWISE") {
            while (j < pointsMap.length && !end) {
                pointsMap[j] = findAngleDistance(detectionMap[i].points, j);
                addAngle(j);
                paintPoints();
                j++;
                await sleep(10);
            }
        }
        else {
            while (j < pointsMap.length && !end) {
                let inverseAngle = 359 - j;
                pointsMap[inverseAngle] = findAngleDistance(detectionMap[i].points, inverseAngle);
                addAngle(inverseAngle);
                paintPoints();
                j++;
                await sleep(10);
            }
        }        
        i++;
    }
    if (!end) {
        drawAnimation(detectionItems, 0);
    }
}

function findAngleDistance(points, angle) {
    for (let point of points) {
        if (point.angle == angle) {
            return point.distance;
        }
    }

    return 0;
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

stopBtn.addEventListener("click", () => {
    endAnimation();
})

function endAnimation() {
    end = true;
    radarAngles = [];
    pointsMap = new Array(360).fill(null);
}

addListeners();