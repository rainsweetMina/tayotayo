// ✅ 날짜 포맷 함수 (전역)
function formatDateForInput(datetime) {
    const date = new Date(datetime);
    return date.toISOString().slice(0, 16);
}

document.addEventListener("DOMContentLoaded", () => {
    loadAdCompanies();
    loadAds();

    document.getElementById("adForm").addEventListener("submit", handleAdSubmit);
    document.getElementById("companyForm").addEventListener("submit", handleCompanySubmit);
});

// ✅ 광고 등록/수정 (FormData 방식으로 변경됨)
function handleAdSubmit(e) {
    e.preventDefault();
    const id = document.getElementById("adId").value;

    const adDTO = {
        title: document.getElementById("title").value,
        linkUrl: document.getElementById("linkUrl").value,
        startDateTime: document.getElementById("startDateTime").value,
        endDateTime: document.getElementById("endDateTime").value,
        companyId: document.getElementById("companySelect").value,
        showPopup: document.getElementById("showPopup").checked
    };

    const imageFile = document.getElementById("imageFile").files[0];
    const formData = new FormData();
    formData.append("dto", new Blob([JSON.stringify(adDTO)], { type: "application/json" }));
    if (imageFile) formData.append("image", imageFile);

    const method = id ? "PUT" : "POST";
    const url = id ? `/api/ad/${id}` : "/api/ad";

    fetch(url, {
        method,
        body: formData
    }).then(() => {
        e.target.reset();
        document.getElementById("adId").value = "";
        document.getElementById("adSubmitBtn").textContent = "등록";
        document.getElementById("adEditStatus").style.display = "none";
        loadAds();
    });
}

// ✅ 광고 수정 모드 (기존 그대로 유지하되 imageUrl 제거됨)
function editAd(id) {
    fetch(`/api/ad/${id}`)
        .then(res => res.json())
        .then(ad => {
            document.getElementById("adId").value = ad.id;
            document.getElementById("title").value = ad.title;
            document.getElementById("linkUrl").value = ad.linkUrl;
            document.getElementById("startDateTime").value = formatDateForInput(ad.startDateTime);
            document.getElementById("endDateTime").value = formatDateForInput(ad.endDateTime);
            document.getElementById("companySelect").value = ad.companyId || "";
            document.getElementById("showPopup").checked = ad.showPopup || false;

            document.getElementById("adSubmitBtn").textContent = "수정";
            document.getElementById("adEditStatus").style.display = "inline";
        });
}

// ✅ 광고 삭제
function deleteAd(id) {
    if (!confirm("광고를 삭제하시겠습니까?")) return;
    fetch(`/api/ad/${id}`, { method: "DELETE" }).then(loadAds);
}

// ✅ 광고 목록 불러오기
function loadAds() {
    fetch("/api/ad")
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("adList");
            tbody.innerHTML = "";

            data.forEach(ad => {
                let statusText = "-";
                switch (ad.status) {
                    case "SCHEDULED": statusText = "예정"; break;
                    case "ONGOING": statusText = "진행중"; break;
                    case "ENDED": statusText = "종료됨"; break;
                    case "DELETED": statusText = "삭제됨"; break;
                    case "ENDING_SOON": statusText = "곧 종료"; break;
                    default: statusText = ad.status || "-"; break;
                }

                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${ad.id}</td>
                    <td>${ad.title}</td>
                    <td>${ad.companyName || '-'}</td>
                    <td>${ad.startDateTime?.slice(0, 10) || '-'}</td>
                    <td>${ad.endDateTime?.slice(0, 10) || '-'}</td>
                    <td>${statusText}</td>
                    <td><button onclick="editAd(${ad.id})">수정</button></td>
                    <td><button onclick="deleteAd(${ad.id})">삭제</button></td>
                `;
                tbody.appendChild(row);
            });
        });
}

// ✅ 광고회사 관련 함수 (기존과 동일)
function loadAdCompanies() {
    fetch("/api/ad-company")
        .then(res => res.json())
        .then(data => {
            const dropdown = document.getElementById("companySelect");
            const companyList = document.getElementById("companyList");
            dropdown.innerHTML = "";
            companyList.innerHTML = "";

            data.forEach(c => {
                const option = document.createElement("option");
                option.value = c.id;
                option.textContent = c.name;
                dropdown.appendChild(option);

                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${c.id}</td>
                    <td>${c.name}</td>
                    <td>${c.managerName || '-'}</td>
                    <td>${c.contactNumber || '-'}</td>
                    <td>${c.email || '-'}</td>
                    <td><button onclick="editCompany(${c.id})">수정</button></td>
                    <td><button onclick="deleteCompany(${c.id})">삭제</button></td>
                `;
                companyList.appendChild(row);
            });
        });
}

function handleCompanySubmit(e) {
    e.preventDefault();
    const id = document.getElementById("companyId").value;
    const payload = {
        name: document.getElementById("companyName").value,
        managerName: document.getElementById("managerName").value,
        contactNumber: document.getElementById("contactNumber").value,
        email: document.getElementById("email").value
    };

    const method = id ? "PUT" : "POST";
    const url = id ? `/api/ad-company/${id}` : `/api/ad-company`;

    fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    }).then(() => {
        e.target.reset();
        document.getElementById("companyId").value = "";
        document.getElementById("companySubmitBtn").textContent = "등록";
        document.getElementById("companyEditStatus").style.display = "none";
        loadAdCompanies();
    });
}

function editCompany(id) {
    fetch("/api/ad-company")
        .then(res => res.json())
        .then(data => {
            const company = data.find(c => c.id === id);
            if (!company) return;

            document.getElementById("companyId").value = company.id;
            document.getElementById("companyName").value = company.name || '';
            document.getElementById("managerName").value = company.managerName || '';
            document.getElementById("contactNumber").value = company.contactNumber || '';
            document.getElementById("email").value = company.email || '';

            document.getElementById("companySubmitBtn").textContent = "수정";
            document.getElementById("companyEditStatus").style.display = "inline";
        });
}

function deleteCompany(id) {
    if (!confirm("회사 삭제하시겠습니까?")) return;

    fetch(`/api/ad-company/${id}`, {
        method: "DELETE"
    }).then(() => {
        loadAdCompanies();
    });
}
