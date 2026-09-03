// Frontend for the Hyderabad Metro Navigator. All route-finding happens
// server-side in Graph_M.java / MetroData.java (via MetroServer.java) --
// this file only calls the JSON API and renders the response.

// Mirrors MetroData.java's corridors -- for display only (which "line"
// badge to show next to a station), not used for any routing logic.
const CORRIDORS = {
  "Red Line": [
    "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Dr. B.R. Ambedkar Balanagar",
    "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "S.R. Nagar", "Ameerpet",
    "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdikapul", "Assembly", "Nampally",
    "Gandhi Bhavan", "Osmania Medical College", "MG Bus Station", "New Market",
    "Musarambagh", "Dilsukhnagar", "Chaitanyapuri", "Victoria Memorial", "LB Nagar",
  ],
  "Green Line": [
    "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad",
    "RTC X Roads", "Chikkadpally", "Narayanguda", "Sultan Bazar", "MG Bus Station",
  ],
  "Blue Line": [
    "Nagole", "Uppal", "NGRI", "Habsiguda", "Mettuguda", "Secunderabad East", "Parade Grounds",
    "Paradise", "Rasoolpura", "Prakash Nagar", "Begumpet", "Ameerpet", "Madhura Nagar",
    "Yusufguda", "Road No.5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi",
    "Madhapur", "Durgam Cheruvu", "HITEC City", "Raidurg",
  ],
};

const INTERCHANGE_STATIONS = new Set(["Ameerpet", "MG Bus Station", "Parade Grounds", "JBS Parade Ground"]);

function lineForStation(station) {
  for (const [line, stations] of Object.entries(CORRIDORS)) {
    if (stations.includes(station)) return line;
  }
  return "Unknown";
}

const fromSelect = document.getElementById("from-select");
const toSelect = document.getElementById("to-select");
const findBtn = document.getElementById("find-btn");
const messageEl = document.getElementById("message");
const tabsEl = document.getElementById("tabs");
const resultEls = {
  distance: document.getElementById("result-distance"),
  time: document.getElementById("result-time"),
};

let activeMetric = "distance";
let lastResults = { distance: null, time: null };

async function loadStations() {
  const res = await fetch("/api/stations");
  const stations = await res.json();

  for (const select of [fromSelect, toSelect]) {
    select.innerHTML = "";
    for (const station of stations) {
      const option = document.createElement("option");
      option.value = station;
      option.textContent = station;
      select.appendChild(option);
    }
  }
  fromSelect.value = stations.includes("Miyapur") ? "Miyapur" : stations[0];
  toSelect.value = stations.includes("LB Nagar") ? "LB Nagar" : stations[stations.length - 1];

  document.getElementById("all-stations-list").textContent = stations.join(", ");
}

function showMessage(text, isError) {
  messageEl.textContent = text;
  messageEl.hidden = false;
  messageEl.classList.toggle("error", Boolean(isError));
}

function hideMessage() {
  messageEl.hidden = true;
}

function renderResultCard(container, result) {
  if (!result.found) {
    container.innerHTML = "";
    container.hidden = true;
    return;
  }

  const rows = result.path
    .map((station) => {
      const badge = INTERCHANGE_STATIONS.has(station)
        ? '<span class="interchange-badge">interchange</span>'
        : "";
      return (
        '<div class="station-step">' +
        `<span class="station-step-name">${station}${badge}</span>` +
        `<span class="station-step-line">${lineForStation(station)}</span>` +
        "</div>"
      );
    })
    .join("");

  container.innerHTML =
    `<span class="cost-pill">${result.totalCost.toFixed(1)} ${result.unit}</span>` + rows;
  container.hidden = false;
}

function setActiveTab(metric) {
  activeMetric = metric;
  for (const btn of document.querySelectorAll(".tab-btn")) {
    btn.classList.toggle("active", btn.dataset.metric === metric);
  }
  resultEls.distance.hidden = metric !== "distance";
  resultEls.time.hidden = metric !== "time";
}

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => setActiveTab(btn.dataset.metric));
});

async function findRoute() {
  const from = fromSelect.value;
  const to = toSelect.value;

  if (from === to) {
    showMessage("Source and destination are the same station.", false);
    tabsEl.hidden = true;
    resultEls.distance.hidden = true;
    resultEls.time.hidden = true;
    return;
  }

  hideMessage();

  for (const metric of ["distance", "time"]) {
    const params = new URLSearchParams({ from, to, metric });
    const res = await fetch(`/api/route?${params.toString()}`);
    const data = await res.json();

    if (!res.ok) {
      showMessage(data.error + (data.suggestions && data.suggestions.length ? ` Did you mean: ${data.suggestions.join(", ")}?` : ""), true);
      tabsEl.hidden = true;
      resultEls.distance.hidden = true;
      resultEls.time.hidden = true;
      return;
    }

    lastResults[metric] = data;
  }

  tabsEl.hidden = false;
  renderResultCard(resultEls.distance, lastResults.distance);
  renderResultCard(resultEls.time, lastResults.time);
  setActiveTab(activeMetric);

  if (
    lastResults.distance.found &&
    lastResults.time.found &&
    JSON.stringify(lastResults.distance.path) !== JSON.stringify(lastResults.time.path)
  ) {
    showMessage(
      "The fastest route and the shortest route differ for this pair -- fewer interchanges usually wins on time even if it's a longer ride.",
      false
    );
  }
}

findBtn.addEventListener("click", findRoute);
loadStations();
