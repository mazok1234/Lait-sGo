document.addEventListener("DOMContentLoaded", function () {
    const input = document.querySelector(".filter-search");
    const table = document.getElementById("vaccinTable");
    if (!table) return;

    const tbody = table.querySelector("tbody");
    const originalRows = Array.from(tbody.querySelectorAll("tr")).filter(row => row.children.length > 1);

    const emptyRow = document.createElement('tr');
    emptyRow.innerHTML = '<td colspan="4" class="text-center" style="padding:20px;">Aucun vaccin critique</td>';

    let filteredRows = [...originalRows];
    let currentPage = 1;
    const rowsPerPage = 5;
    let dateAscending = true;

    const headers = table.querySelectorAll("thead th");
    [1, 2].forEach(index => {
        if (headers[index]) {
            headers[index].style.cursor = "pointer";
            headers[index].innerHTML += ' <i class="fas fa-sort" style="margin-left:5px; opacity:0.5;"></i>';
            headers[index].addEventListener("click", () => toggleSortDate(index));
        }
    });

    const paginationContainer = document.createElement("div");
    paginationContainer.className = "pagination-container";
    paginationContainer.style.cssText = "display:flex; justify-content:space-between; align-items:center; margin-top:15px; padding:0 10px;";
    table.parentNode.insertBefore(paginationContainer, table.nextSibling);

    function renderTable() {
        tbody.innerHTML = "";

        if (filteredRows.length === 0) {
            tbody.appendChild(emptyRow);
            paginationContainer.innerHTML = "";
            return;
        }

        const totalPages = Math.ceil(filteredRows.length / rowsPerPage);
        if (currentPage > totalPages) currentPage = totalPages || 1;

        const start = (currentPage - 1) * rowsPerPage;
        const end = start + rowsPerPage;
        const pageRows = filteredRows.slice(start, end);

        pageRows.forEach(row => tbody.appendChild(row));

        renderPaginationControls(totalPages);
    }

    function renderPaginationControls(totalPages) {
        paginationContainer.innerHTML = `
                        <div style="display:flex; gap:8px;margin:0 auto; padding:10px 0;">
                            <button id="prevPage" class="btn" style="padding:5px 12px; border:1px solid #D1D5DB; background:white; border-radius:6px; cursor:pointer;" ${currentPage === 1 ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>Précédent</button>
                            <span style="font-size:0.9rem; color:#6B7280;">
                                Pages ${currentPage}/${totalPages} (${filteredRows.length} vaccin${filteredRows.length > 1 ? 's' : ''})
                            </span>
                            <button id="nextPage" class="btn" style="padding:5px 12px; border:1px solid #D1D5DB; background:white; border-radius:6px; cursor:pointer;" ${currentPage === totalPages ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>Suivant</button>
                        </div>
                    `;

        document.getElementById("prevPage").addEventListener("click", () => { if (currentPage > 1) { currentPage--; renderTable(); } });
        document.getElementById("nextPage").addEventListener("click", () => { if (currentPage < totalPages) { currentPage++; renderTable(); } });
    }

    function parseDate(dateStr) {
        if (!dateStr) return new Date(0);
        if (dateStr.includes('-')) return new Date(dateStr);
        const parts = dateStr.trim().split('/');
        if (parts.length === 3) return new Date(parts[2], parts[1] - 1, parts[0]);
        return new Date(dateStr);
    }

    function toggleSortDate(colIndex) {
        dateAscending = !dateAscending;

        headers.forEach((th, idx) => {
            const icon = th.querySelector('.fa-sort, .fa-sort-up, .fa-sort-down');
            if (icon) {
                if (idx === colIndex) {
                    icon.className = dateAscending ? "fas fa-sort-up" : "fas fa-sort-down";
                    icon.style.opacity = "1";
                } else {
                    icon.className = "fas fa-sort";
                    icon.style.opacity = "0.5";
                }
            }
        });

        filteredRows.sort((a, b) => {
            const dateA = parseDate(a.children[colIndex].textContent);
            const dateB = parseDate(b.children[colIndex].textContent);
            return dateAscending ? dateA - dateB : dateB - dateA;
        });

        currentPage = 1;
        renderTable();
    }

    if (input) {
        input.addEventListener("keyup", function () {
            const filter = this.value.toLowerCase();

            filteredRows = originalRows.filter(row => {
                return row.textContent.toLowerCase().includes(filter);
            });

            currentPage = 1;
            renderTable();
        });
    }

    renderTable();

    const exportBtn = document.getElementById('exportBtn');
    const closeBtn = document.getElementById('closeBtn');
    const modalOverlay = document.getElementById('modalOverlay');
    const modalOverlay1 = document.getElementById('modalOverlay1');
    const mainContent = document.getElementById('mainContent');
    const importBtn = document.getElementById('importBtn');
    const closeBtn1 = document.getElementById('closeBtn1');

    if (exportBtn && closeBtn && modalOverlay && modalOverlay1 && mainContent && importBtn && closeBtn1) {
        exportBtn.addEventListener('click', function () {
            modalOverlay.classList.remove('hidden');
            mainContent.classList.add('blur-background');
        });

        closeBtn.addEventListener('click', function () {
            modalOverlay.classList.add('hidden');
            mainContent.classList.remove('blur-background');
        });

        importBtn.addEventListener('click', function () {
            modalOverlay1.classList.remove('hidden');
            mainContent.classList.add('blur-background');
        });

        closeBtn1.addEventListener('click', function () {
            modalOverlay1.classList.add('hidden');
            mainContent.classList.remove('blur-background');
        });
    }
});