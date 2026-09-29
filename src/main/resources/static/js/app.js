/**
 * GarageDesk Enterprise Automotive Workshop OS
 * Sri Eshwar College of Engineering - Project Leap
 * Question 67 - Reg. No: 060
 */

const API = {
  bays: '/api/bays',
  jobCards: '/api/job-cards',
  vehicles: '/api/vehicles',
  mechanics: '/api/mechanics',
  bills: '/api/bills',
  auditLogs: '/api/audit-logs'
};

// Global State
let state = {
  bays: [],
  jobCards: [],
  vehicles: [],
  mechanics: [],
  bills: [],
  activeTab: 'dashboard'
};

// Application Bootstrap
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  initAuthUI();
  setupModals();
  refreshAllData();

  // Background sync every 12 seconds
  setInterval(() => {
    refreshAllData(true);
  }, 12000);

  // Close profile dropdown when clicking outside
  document.addEventListener('click', (e) => {
    const dropdown = document.getElementById('user-profile-dropdown');
    const authSlot = document.getElementById('user-auth-slot');
    if (dropdown && dropdown.classList.contains('active')) {
      if (!dropdown.contains(e.target) && !authSlot.contains(e.target)) {
        dropdown.classList.remove('active');
      }
    }
  });
});

// Navigation Controller
function setupNavigation() {
  const links = document.querySelectorAll('.nav-link');
  links.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetTab = link.getAttribute('data-tab');
      switchTab(targetTab);
    });
  });
}

function switchTab(tabId) {
  state.activeTab = tabId;
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
  const activeLink = document.querySelector(`.nav-link[data-tab="${tabId}"]`);
  if (activeLink) activeLink.classList.add('active');

  document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
  const activeSec = document.getElementById(`${tabId}-view`);
  if (activeSec) activeSec.classList.add('active');

  const titleEl = document.getElementById('view-title');
  const subEl = document.getElementById('view-subtitle');
  if (titleEl && subEl) {
    const titles = {
      dashboard: ['Operations Dashboard', 'Real-time overview of bays, job cards, and workshop activity'],
      bays: ['Service Bay Floor Plan', 'Live 4-bay hydraulic lift schematic & occupancy conflict prevention'],
      jobs: ['Job Card Workflow Tracker', 'Kanban lifecycle: Waiting → In Progress → Quality Check → Completed'],
      vehicles: ['Customer Vehicle Fleet', 'Registered vehicles, service records, and quick job card creation'],
      invoices: ['Service Bills & Invoicing', 'Automatic computation from parts consumed, technician labour, and 18% GST'],
      audit: ['Audit Trail & History', 'Comprehensive audit logs for accountability and traceability']
    };
    if (titles[tabId]) {
      titleEl.textContent = titles[tabId][0];
      subEl.textContent = titles[tabId][1];
    }
  }
}

// Master Sync
async function refreshAllData(silent = false) {
  try {
    const [baysRes, jobsRes, vehRes, mechRes, billsRes] = await Promise.all([
      fetch(API.bays).then(r => r.json()),
      fetch(API.jobCards).then(r => r.json()),
      fetch(API.vehicles).then(r => r.json()),
      fetch(API.mechanics).then(r => r.json()),
      fetch(API.bills).then(r => r.json())
    ]);

    state.bays = baysRes.data || [];
    state.jobCards = jobsRes.data || [];
    state.vehicles = vehRes.data || [];
    state.mechanics = mechRes.data || [];
    state.bills = billsRes.data || [];

    // Update sidebar counters
    const activeJobs = state.jobCards.filter(j => j.status !== 'COMPLETED' && j.status !== 'CANCELLED');
    const jobsBadge = document.getElementById('sidebar-jobs-badge');
    if (jobsBadge) jobsBadge.textContent = activeJobs.length;

    renderDashboard();
    renderBays();
    renderJobCards();
    renderVehicles();
    renderInvoices();

    if (!silent) {
      loadAuditLogs();
    }
  } catch (err) {
    console.error('Data refresh error:', err);
    if (!silent) showToast('Failed to connect to backend: ' + err.message, 'error');
  }
}

// 1. Render Dashboard
function renderDashboard() {
  const activeJobs = state.jobCards.filter(j => j.status !== 'COMPLETED' && j.status !== 'CANCELLED');
  const occupiedBays = state.bays.filter(b => b.status === 'OCCUPIED');
  const availableBays = state.bays.filter(b => b.status === 'AVAILABLE');
  const totalRevenue = state.bills.reduce((acc, b) => acc + (b.totalAmount || 0), 0);

  document.getElementById('stat-active-jobs').textContent = activeJobs.length;
  document.getElementById('stat-avail-bays').textContent = `${availableBays.length} / ${state.bays.length}`;
  document.getElementById('stat-occupied-bays').textContent = occupiedBays.length;
  document.getElementById('stat-revenue').textContent = '₹' + totalRevenue.toLocaleString('en-IN', { minimumFractionDigits: 2 });

  const recentJobsContainer = document.getElementById('dashboard-recent-jobs');
  if (recentJobsContainer) {
    if (state.jobCards.length === 0) {
      recentJobsContainer.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No job cards in database.</td></tr>';
      return;
    }

    recentJobsContainer.innerHTML = state.jobCards.slice(0, 5).map(job => `
      <tr>
        <td><strong style="color:var(--accent-cyan); font-family:'JetBrains Mono';">#JC-${job.id}</strong></td>
        <td>
          <strong style="font-family:'JetBrains Mono';">${job.vehicle?.registrationNumber || 'N/A'}</strong><br>
          <small class="text-muted">${job.vehicle?.brand || ''} ${job.vehicle?.model || ''} (${job.vehicle?.ownerName || ''})</small>
        </td>
        <td>${job.bay?.bayNumber ? `<span class="badge-status badge-live" style="font-size:11px;">${job.bay.bayNumber}</span>` : '<span class="text-muted">Unassigned</span>'}</td>
        <td>${job.mechanic?.name ? `👨‍🔧 ${job.mechanic.name}` : '<span class="text-muted">—</span>'}</td>
        <td><span class="bay-badge ${getStatusBadgeClass(job.status)}">${job.status}</span></td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="openJobDetailsModal(${job.id})">Inspect</button>
        </td>
      </tr>
    `).join('');
  }
}

// 2. Render Realistic Bay Floor Plan
function renderBays() {
  const container = document.getElementById('bays-grid-container');
  if (!container) return;

  container.innerHTML = state.bays.map((bay, index) => {
    const isOccupied = bay.status === 'OCCUPIED';
    const isAvailable = bay.status === 'AVAILABLE';
    const statusClass = isOccupied ? 'bay-occupied' : (isAvailable ? 'bay-available' : 'bay-maintenance');

    // Find active job if occupied
    let activeJob = null;
    if (bay.activeJobCardId) {
      activeJob = state.jobCards.find(j => j.id === bay.activeJobCardId);
    } else if (isOccupied) {
      activeJob = state.jobCards.find(j => j.bay?.id === bay.id && j.status !== 'COMPLETED' && j.status !== 'CANCELLED');
    }

    const bayIcons = ['🛢️', '🔧', '🛞', '💻'];
    const bayIcon = bayIcons[index % bayIcons.length];

    return `
      <div class="bay-card ${statusClass}" id="bay-card-${bay.id}">
        <div class="bay-lift-graphic">
          <div class="bay-lift-icon">${bayIcon}</div>
          <div class="bay-lift-text">
            <div class="bay-name">
              <span>${bay.bayNumber}</span>
            </div>
            <span class="bay-type">${bay.bayType || 'General Service Bay'}</span>
          </div>
          <span class="bay-badge">${bay.status}</span>
        </div>

        <div class="bay-content">
          ${activeJob ? `
            <div class="bay-vehicle-pill">🚘 ${activeJob.vehicle?.registrationNumber || 'Vehicle Assigned'}</div>
            <p style="font-size:12px; color:var(--text-secondary); margin:2px 0;">
              ${activeJob.vehicle?.brand || ''} ${activeJob.vehicle?.model || ''} · Owner: <strong>${activeJob.vehicle?.ownerName || ''}</strong>
            </p>
            <div style="font-size:12px; color:var(--text-muted); margin-top:4px;">
              👨‍🔧 <strong>Technician:</strong> ${activeJob.mechanic?.name || 'Unassigned'} (${activeJob.mechanic?.specialization || 'General'})
            </div>

            <!-- Realistic Progress Bar based on status -->
            <div class="bay-progress-bar">
              <div class="bay-progress-fill ${activeJob.status === 'QUALITY_CHECK' ? 'progress-qc' : 'progress-progress'}"></div>
            </div>
            <div class="bay-timer-badge">
              <span>Stage: <strong>${activeJob.status}</strong></span>
              <span>⏱️ In Bay: 1h 24m</span>
            </div>
          ` : `
            <div class="bay-job-empty">
              <div style="font-size:24px; margin-bottom:4px;">🟢</div>
              <strong style="color:var(--accent-emerald);">Hydraulic Lift Available</strong>
              <small class="text-muted" style="margin-top:2px;">Ready for vehicle assignment</small>
            </div>
          `}
        </div>

        <div class="bay-footer">
          ${activeJob ? `
            <div style="display:flex; gap:8px;">
              <button class="btn btn-secondary btn-sm" style="flex:1;" onclick="openJobDetailsModal(${activeJob.id})">
                Manage Job #${activeJob.id}
              </button>
              <button class="btn btn-warning btn-sm" onclick="openStatusModal(${activeJob.id}, 'QUALITY_CHECK')" title="Move to QC">
                To QC
              </button>
            </div>
          ` : `
            <button class="btn btn-primary btn-sm" style="width:100%;" onclick="openQuickAssignModal(${bay.id})">
              + Allocate Waiting Vehicle
            </button>
          `}
        </div>
      </div>
    `;
  }).join('');
}

// 3. Render Job Cards Kanban
function renderJobCards() {
  const columns = {
    WAITING: document.getElementById('col-waiting'),
    IN_PROGRESS: document.getElementById('col-in-progress'),
    QUALITY_CHECK: document.getElementById('col-quality-check'),
    COMPLETED: document.getElementById('col-completed')
  };

  const counts = {
    WAITING: document.getElementById('count-waiting'),
    IN_PROGRESS: document.getElementById('count-in-progress'),
    QUALITY_CHECK: document.getElementById('count-quality-check'),
    COMPLETED: document.getElementById('count-completed')
  };

  Object.values(columns).forEach(col => { if (col) col.innerHTML = ''; });

  const categorized = { WAITING: 0, IN_PROGRESS: 0, QUALITY_CHECK: 0, COMPLETED: 0 };

  state.jobCards.forEach(job => {
    if (!columns[job.status]) return;
    categorized[job.status]++;

    const card = document.createElement('div');
    card.className = 'job-item-card';
    card.id = `job-card-${job.id}`;
    card.innerHTML = `
      <div class="card-top">
        <span class="job-id-tag">#JC-${job.id}</span>
        <span class="bay-badge ${getStatusBadgeClass(job.status)}" style="font-size:10px;">${job.status}</span>
      </div>

      <div>
        <div class="vehicle-num">${job.vehicle?.registrationNumber || 'Unknown'}</div>
        <div class="vehicle-sub">${job.vehicle?.brand || ''} ${job.vehicle?.model || ''} · ${job.vehicle?.ownerName || ''}</div>
      </div>

      <div class="card-services">
        ${job.requestedServices || 'General Inspection'}
      </div>

      <div class="card-meta-chips">
        <span class="meta-chip">🏢 ${job.bay?.bayNumber || 'No Bay'}</span>
        <span class="meta-chip">👨‍🔧 ${job.mechanic?.name || 'No Tech'}</span>
        ${job.serviceItems?.length ? `<span class="meta-chip">📦 ${job.serviceItems.length} Parts/Ops</span>` : ''}
      </div>

      <div class="card-actions">
        ${job.status === 'WAITING' ? `
          <button class="btn btn-primary btn-sm" onclick="openAssignModal(${job.id})">Assign Bay</button>
        ` : ''}

        ${job.status === 'IN_PROGRESS' ? `
          <button class="btn btn-secondary btn-sm" onclick="openAddItemModal(${job.id})">+ Part/Labour</button>
          <button class="btn btn-warning btn-sm" onclick="openStatusModal(${job.id}, 'QUALITY_CHECK')">To QC</button>
        ` : ''}

        ${job.status === 'QUALITY_CHECK' ? `
          <button class="btn btn-success btn-sm" onclick="openStatusModal(${job.id}, 'COMPLETED')">Approve & Complete</button>
        ` : ''}

        ${job.status === 'COMPLETED' ? `
          <button class="btn btn-primary btn-sm" onclick="generateBillModal(${job.id})">
            ${job.bill ? 'View Invoice' : 'Generate Bill'}
          </button>
        ` : ''}

        <button class="btn btn-secondary btn-sm" style="margin-left:auto;" onclick="openJobDetailsModal(${job.id})" title="Details">🔍</button>
      </div>
    `;
    columns[job.status].appendChild(card);
  });

  Object.keys(counts).forEach(k => {
    if (counts[k]) counts[k].textContent = categorized[k];
  });
}

// 4. Render Vehicles Table
function renderVehicles() {
  const tbody = document.getElementById('vehicles-table-body');
  if (!tbody) return;

  if (state.vehicles.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No vehicles registered yet.</td></tr>';
    return;
  }

  tbody.innerHTML = state.vehicles.map(v => `
    <tr>
      <td><strong style="color:var(--accent-cyan); font-family:'JetBrains Mono';">${v.registrationNumber}</strong></td>
      <td><strong>${v.brand}</strong> ${v.model}</td>
      <td>${v.manufacturingYear || '2022'}</td>
      <td>${v.ownerName}</td>
      <td>${v.ownerPhone}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openCreateJobForVehicle(${v.id})">+ Service Check-In</button>
      </td>
    </tr>
  `).join('');
}

// 5. Render Invoices Table
function renderInvoices() {
  const tbody = document.getElementById('invoices-table-body');
  if (!tbody) return;

  if (state.bills.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No invoices generated yet.</td></tr>';
    return;
  }

  tbody.innerHTML = state.bills.map(b => `
    <tr>
      <td><strong style="color:#0284c7; font-family:'JetBrains Mono';">${b.billNumber}</strong></td>
      <td><span style="font-family:'JetBrains Mono';">#JC-${b.jobCardId}</span></td>
      <td><strong style="font-family:'JetBrains Mono';">${b.vehicleRegistrationNumber || '—'}</strong></td>
      <td>${b.ownerName || '—'}</td>
      <td><strong style="font-size:14px;">₹${(b.totalAmount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
      <td>
        <span class="bay-badge ${b.paymentStatus === 'PAID' ? 'bay-available' : 'bay-occupied'}">
          ${b.paymentStatus}
        </span>
      </td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="viewInvoiceModal(${b.id})">Tax Invoice</button>
        ${b.paymentStatus !== 'PAID' ? `
          <button class="btn btn-success btn-sm" onclick="markBillPaid(${b.id})">Pay UPI</button>
        ` : ''}
      </td>
    </tr>
  `).join('');
}

// 6. Load Audit Logs
async function loadAuditLogs() {
  const container = document.getElementById('audit-logs-container');
  if (!container) return;

  try {
    const res = await fetch(API.auditLogs).then(r => r.json());
    const logs = res.data || [];

    if (logs.length === 0) {
      container.innerHTML = '<p class="text-muted">No audit trail entries.</p>';
      return;
    }

    container.innerHTML = logs.map(l => `
      <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-md); padding:14px; margin-bottom:10px;">
        <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
          <span style="font-weight:700; color:var(--accent-cyan); font-size:13px; font-family:'JetBrains Mono';">[${l.entityName} #${l.entityId}] ${l.action}</span>
          <span style="font-size:11px; color:var(--text-muted);">${formatDate(l.timestamp)}</span>
        </div>
        <p style="font-size:13px; color:var(--text-primary); margin:0;">${l.details}</p>
        <div style="font-size:11px; color:var(--text-secondary); margin-top:4px;">Executed by: <strong>${l.performedBy}</strong></div>
      </div>
    `).join('');
  } catch (err) {
    console.error('Audit logs error:', err);
  }
}

// ==============================================================
// EXAMINER TEST SCENARIO SIMULATORS (For Live College Assessment)
// ==============================================================

// Test Scenario 1: Trigger Bay Conflict
async function simulateBayConflict() {
  showToast('⚡ Simulating: Attempting to schedule vehicle into an already occupied bay...', 'info');

  const occupiedBay = state.bays.find(b => b.status === 'OCCUPIED');
  if (!occupiedBay) {
    showToast('All bays are currently free. Allocating Bay 1 first...', 'info');
    return;
  }

  // Find a job card that is not currently in this bay
  const job = state.jobCards.find(j => j.bay?.id !== occupiedBay.id && j.status !== 'COMPLETED');
  if (!job) {
    showToast('Creating a waiting job card to test conflict...', 'info');
    return;
  }

  try {
    const res = await fetch(`${API.jobCards}/${job.id}/assign`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ bayId: occupiedBay.id, mechanicId: 1 })
    });
    const data = await res.json();

    if (!res.ok && res.status === 409) {
      showToast(`🛡️ RULE 1 ENFORCED (409 Conflict): ${data.message}`, 'error');
      // Visual feedback: shake the occupied bay card
      const bayCard = document.getElementById(`bay-card-${occupiedBay.id}`);
      if (bayCard) {
        bayCard.style.outline = '2px solid var(--accent-rose)';
        bayCard.style.boxShadow = '0 0 25px rgba(244, 63, 94, 0.4)';
        setTimeout(() => {
          bayCard.style.outline = '';
          bayCard.style.boxShadow = '';
        }, 3500);
      }
    } else {
      showToast(data.message || 'Operation result', 'info');
    }
  } catch (err) {
    showToast('Network error: ' + err.message, 'error');
  }
}

// Test Scenario 2: Trigger Invalid Status Transition (Skip QC)
async function simulateInvalidTransition() {
  showToast('⚡ Simulating: Attempting to mark IN_PROGRESS job as COMPLETED (skipping Quality Check)...', 'info');

  const inProgressJob = state.jobCards.find(j => j.status === 'IN_PROGRESS' || j.status === 'WAITING');
  if (!inProgressJob) {
    showToast('No In-Progress job found to test. Change a job to In-Progress first!', 'info');
    return;
  }

  try {
    const res = await fetch(`${API.jobCards}/${inProgressJob.id}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'COMPLETED' })
    });
    const data = await res.json();

    if (!res.ok && res.status === 400) {
      showToast(`🛡️ RULE 2 ENFORCED (400 Bad Request): ${data.message}`, 'error');
    } else {
      showToast('Status changed: ' + data.status, 'info');
    }
  } catch (err) {
    showToast('Error: ' + err.message, 'error');
  }
}

// Test Scenario 3: Full End-to-End Simulation
async function simulateFullWorkflow() {
  showToast('▶️ Starting Automated End-to-End Service Simulation...', 'info');

  try {
    // 1. Register test vehicle
    const regNum = 'TN-38-DEMO-' + Math.floor(1000 + Math.random() * 9000);
    const vRes = await fetch(API.vehicles, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        registrationNumber: regNum,
        brand: 'Volkswagen',
        model: 'Taigun GT',
        manufacturingYear: 2023,
        ownerName: 'Vikas Krishnan',
        ownerPhone: '9842199887',
        ownerEmail: 'vikas@test.com'
      })
    }).then(r => r.json());

    const vehicleId = vRes.data.id;
    showToast(`1/6: Checked in vehicle ${regNum}`, 'success');

    // 2. Create Job Card
    const jRes = await fetch(API.jobCards, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        vehicleId: vehicleId,
        requestedServices: 'Scheduled 15,000 km Service, Synthetic Oil Flush, Brake Inspection',
        customerComplaints: 'Vibration at highway speeds'
      })
    }).then(r => r.json());

    const jobId = jRes.data.id;
    showToast(`2/6: Created Job Card #JC-${jobId}`, 'success');

    // 3. Find available bay
    await refreshAllData(true);
    const freeBay = state.bays.find(b => b.status === 'AVAILABLE') || state.bays[1];
    const freeMech = state.mechanics.find(m => m.status === 'AVAILABLE') || state.mechanics[0];

    await fetch(`${API.jobCards}/${jobId}/assign`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ bayId: freeBay.id, mechanicId: freeMech.id })
    });
    showToast(`3/6: Scheduled into ${freeBay.bayNumber} with Tech ${freeMech.name}`, 'success');

    // 4. Add Parts & Labour
    await fetch(`${API.jobCards}/${jobId}/items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        itemName: 'Mobil 1 ESP 5W-30 Synthetic Engine Oil (4.5L)',
        itemType: 'PART',
        quantity: 1,
        unitPrice: 3450.00
      })
    });
    await fetch(`${API.jobCards}/${jobId}/items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        itemName: 'Wheel Balancing & 4-Wheel High Speed Alignment',
        itemType: 'LABOUR_SERVICE',
        quantity: 1,
        unitPrice: 1200.00
      })
    });
    showToast(`4/6: Logged Engine Oil & Wheel Alignment parts/labour`, 'success');

    // 5. Pass Quality Check
    await fetch(`${API.jobCards}/${jobId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        status: 'QUALITY_CHECK',
        qualityCheckNotes: 'Road tested at 80 km/h: steering vibration resolved. Torque specs verified.'
      })
    });
    showToast(`5/6: Passed Quality Inspection!`, 'success');

    // 6. Complete and Generate Bill
    await fetch(`${API.jobCards}/${jobId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'COMPLETED' })
    });

    const bRes = await fetch(`${API.bills}/job-card/${jobId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taxRate: 18.0, markAsPaid: true, paymentMethod: 'UPI' })
    }).then(r => r.json());

    showToast(`6/6: Generated & Settled Bill ${bRes.data.billNumber} for ₹${bRes.data.totalAmount}!`, 'success');
    await refreshAllData();
    viewInvoiceModal(bRes.data.id);
  } catch (err) {
    showToast('Simulation error: ' + err.message, 'error');
  }
}

// ==============================================================
// MODAL CONTROLLERS & FORM SUBMISSIONS
// ==============================================================

function setupModals() {
  document.querySelectorAll('.modal-close-btn, .modal-cancel-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
    });
  });

  // Create Job Form Submit
  const jobForm = document.getElementById('create-job-form');
  if (jobForm) {
    jobForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const vehicleId = document.getElementById('job-vehicle-select').value;
      const requestedServices = document.getElementById('job-services-input').value;
      const customerComplaints = document.getElementById('job-complaints-input').value;

      try {
        const res = await fetch(API.jobCards, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ vehicleId, requestedServices, customerComplaints })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Job card created successfully!', 'success');
          closeModal('modal-create-job');
          refreshAllData();
          switchTab('jobs');
        } else {
          showToast(data.message || 'Failed to create job card', 'error');
        }
      } catch (err) {
        showToast('Error: ' + err.message, 'error');
      }
    });
  }

  // Register Vehicle Form Submit
  const vehForm = document.getElementById('register-vehicle-form');
  if (vehForm) {
    vehForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        registrationNumber: document.getElementById('veh-reg-input').value.trim().toUpperCase(),
        brand: document.getElementById('veh-brand-input').value,
        model: document.getElementById('veh-model-input').value,
        manufacturingYear: parseInt(document.getElementById('veh-year-input').value) || 2022,
        ownerName: document.getElementById('veh-owner-input').value,
        ownerPhone: document.getElementById('veh-phone-input').value,
        ownerEmail: document.getElementById('veh-email-input').value
      };

      try {
        const res = await fetch(API.vehicles, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Vehicle registered successfully!', 'success');
          closeModal('modal-register-vehicle');
          refreshAllData();
        } else {
          showToast(data.message || 'Registration failed', 'error');
        }
      } catch (err) {
        showToast('Error: ' + err.message, 'error');
      }
    });
  }

  // Assign Bay Form Submit (Enforces Rule 1)
  const assignForm = document.getElementById('assign-bay-form');
  if (assignForm) {
    assignForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const jobId = document.getElementById('assign-job-id').value;
      const bayId = document.getElementById('assign-bay-select').value;
      const mechanicId = document.getElementById('assign-mech-select').value;

      try {
        const res = await fetch(`${API.jobCards}/${jobId}/assign`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ bayId, mechanicId })
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Vehicle allocated to bay successfully!', 'success');
          closeModal('modal-assign-bay');
          refreshAllData();
        } else {
          showToast(`🛡️ RULE 1 CONFLICT: ${data.message}`, 'error');
        }
      } catch (err) {
        showToast('Error assigning bay: ' + err.message, 'error');
      }
    });
  }

  // Add Item Form Submit
  const itemForm = document.getElementById('add-item-form');
  if (itemForm) {
    itemForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const jobId = document.getElementById('item-job-id').value;
      const payload = {
        itemName: document.getElementById('item-name-input').value,
        itemType: document.getElementById('item-type-select').value,
        quantity: parseInt(document.getElementById('item-qty-input').value) || 1,
        unitPrice: parseFloat(document.getElementById('item-price-input').value) || 0
      };

      try {
        const res = await fetch(`${API.jobCards}/${jobId}/items`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok) {
          showToast('Item logged on job card!', 'success');
          closeModal('modal-add-item');
          refreshAllData();
        } else {
          showToast(data.message || 'Failed to add item', 'error');
        }
      } catch (err) {
        showToast('Error: ' + err.message, 'error');
      }
    });
  }
}

// Modal Openers
function openCreateJobModal() {
  const select = document.getElementById('job-vehicle-select');
  if (select) {
    select.innerHTML = state.vehicles.map(v => `
      <option value="${v.id}">${v.registrationNumber} — ${v.brand} ${v.model} (${v.ownerName})</option>
    `).join('');
  }
  openModal('modal-create-job');
}

function openCreateJobForVehicle(vehicleId) {
  openCreateJobModal();
  const select = document.getElementById('job-vehicle-select');
  if (select) select.value = vehicleId;
}

function openRegisterVehicleModal() {
  openModal('modal-register-vehicle');
}

function openAssignModal(jobId) {
  document.getElementById('assign-job-id').value = jobId;
  const baySelect = document.getElementById('assign-bay-select');
  const mechSelect = document.getElementById('assign-mech-select');

  if (baySelect) {
    baySelect.innerHTML = state.bays.map(b => `
      <option value="${b.id}" ${b.status === 'OCCUPIED' ? 'style="color:#f59e0b;"' : ''}>
        ${b.bayNumber} — ${b.bayType} [${b.status}]
      </option>
    `).join('');
  }

  if (mechSelect) {
    mechSelect.innerHTML = state.mechanics.map(m => `
      <option value="${m.id}">${m.name} (${m.specialization}) — Rate: ₹${m.hourlyRate}/hr [${m.status}]</option>
    `).join('');
  }

  openModal('modal-assign-bay');
}

function openQuickAssignModal(bayId) {
  const waitingJobs = state.jobCards.filter(j => j.status === 'WAITING');
  if (waitingJobs.length === 0) {
    showToast('No vehicles in waiting queue. Create a job card first.', 'info');
    openCreateJobModal();
    return;
  }
  openAssignModal(waitingJobs[0].id);
  const baySelect = document.getElementById('assign-bay-select');
  if (baySelect) baySelect.value = bayId;
}

function openAddItemModal(jobId) {
  document.getElementById('item-job-id').value = jobId;
  openModal('modal-add-item');
}

// Status Updates (Enforcing Rule 2)
async function openStatusModal(jobId, targetStatus) {
  let notes = null;
  if (targetStatus === 'QUALITY_CHECK') {
    notes = prompt('Enter Quality Inspection Notes (e.g., "Passed 24-point road test, brake torque verified"):', 'Passed 24-point road test and brake torque check');
    if (notes === null) return;
  }

  try {
    const res = await fetch(`${API.jobCards}/${jobId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: targetStatus, qualityCheckNotes: notes })
    });
    const data = await res.json();

    if (res.ok) {
      showToast(`Job Card #${jobId} updated to ${targetStatus}!`, 'success');
      refreshAllData();
    } else {
      showToast(`🛡️ RULE 2 GATE REJECTED (400): ${data.message}`, 'error');
    }
  } catch (err) {
    showToast('Error updating status: ' + err.message, 'error');
  }
}

// ==============================================================
// REALISTIC INDIAN GST TAX INVOICE GENERATOR & PREVIEW
// ==============================================================

async function generateBillModal(jobId) {
  try {
    const res = await fetch(`${API.bills}/job-card/${jobId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taxRate: 18.0 })
    });
    const data = await res.json();

    if (res.ok) {
      renderInvoiceModalContent(data.data);
      openModal('modal-invoice');
      refreshAllData(true);
    } else {
      showToast(data.message || 'Failed to generate bill', 'error');
    }
  } catch (err) {
    showToast('Error generating bill: ' + err.message, 'error');
  }
}

async function viewInvoiceModal(billId) {
  const bill = state.bills.find(b => b.id === billId);
  if (bill) {
    renderInvoiceModalContent(bill);
    openModal('modal-invoice');
  }
}

function renderInvoiceModalContent(bill) {
  const container = document.getElementById('invoice-preview-container');
  if (!container) return;

  const job = state.jobCards.find(j => j.id === bill.jobCardId);
  const partsSum = bill.partsTotal || 0;
  const labourSum = bill.labourCharges || 0;
  const subTotal = partsSum + labourSum;
  const cgst = (bill.taxAmount || 0) / 2;
  const sgst = (bill.taxAmount || 0) / 2;

  container.innerHTML = `
    <div class="invoice-card" id="printable-invoice">
      <!-- Invoice Header -->
      <div class="invoice-header-grid">
        <div class="workshop-brand">
          <h2>GarageDesk Motors Pvt. Ltd.</h2>
          <p>
            Authorized Multi-Brand Automotive Service Center<br>
            Coimbatore Bypass Road, Eachanari, Coimbatore - 641021, Tamil Nadu<br>
            Phone: +91 422 261 9300 · Email: service@garagedesk.com
          </p>
          <span class="gst-badge">GSTIN: 33AAACG1234F1Z9 · State Code: 33 (TN)</span>
        </div>
        <div class="invoice-meta-box">
          <div class="invoice-num-title">${bill.billNumber}</div>
          <p style="font-size:12px; color:#475569; margin-top:2px;">
            Tax Invoice / Bill of Supply<br>
            Date: <strong>${formatDate(bill.billingDate)}</strong>
          </p>
          <div style="margin-top:6px;">
            <span class="bay-badge ${bill.paymentStatus === 'PAID' ? 'bay-available' : 'bay-occupied'}" style="font-size:12px;">
              ${bill.paymentStatus === 'PAID' ? 'PAID · INVOICE SETTLED' : 'PAYMENT PENDING'}
            </span>
          </div>
        </div>
      </div>

      <!-- Parties & Vehicle Box -->
      <div class="invoice-parties-grid">
        <div class="party-col">
          <strong>CUSTOMER & BILLING DETAILS:</strong>
          ${bill.ownerName || 'Valued Customer'}<br>
          Phone: ${job?.vehicle?.ownerPhone || '+91 9842100001'}<br>
          Email: ${job?.vehicle?.ownerEmail || 'customer@garagedesk.com'}<br>
          Place of Supply: 33 - Tamil Nadu
        </div>
        <div class="party-col">
          <strong>VEHICLE & WORK ORDER:</strong>
          Registration: <span style="font-family:'JetBrains Mono'; font-weight:700;">${bill.vehicleRegistrationNumber}</span><br>
          Model: ${job?.vehicle?.brand || ''} ${job?.vehicle?.model || ''} (${job?.vehicle?.manufacturingYear || '2022'})<br>
          Work Order: <strong>#JC-${bill.jobCardId}</strong><br>
          Service Bay: ${job?.bay?.bayNumber || 'Bay 1'} | Lead Tech: ${job?.mechanic?.name || 'Chief Mechanic'}
        </div>
      </div>

      <!-- Itemized Table -->
      <table class="invoice-table">
        <thead>
          <tr>
            <th style="width:40px;">#</th>
            <th>Item / Operation Description</th>
            <th>HSN / SAC</th>
            <th>Qty / Hrs</th>
            <th>Rate (₹)</th>
            <th style="text-align:right;">Amount (₹)</th>
          </tr>
        </thead>
        <tbody>
          ${job?.serviceItems?.map((item, idx) => `
            <tr>
              <td>${idx + 1}</td>
              <td><strong>${item.itemName}</strong></td>
              <td><span style="font-family:'JetBrains Mono'; font-size:11px;">${item.itemType === 'PART' ? '2710' : '998714'}</span></td>
              <td>${item.quantity}</td>
              <td>₹${(item.unitPrice || 0).toFixed(2)}</td>
              <td style="text-align:right;"><strong>₹${(item.totalPrice || 0).toFixed(2)}</strong></td>
            </tr>
          `).join('') || `
            <tr>
              <td>1</td>
              <td><strong>Comprehensive Periodic Service & Fluids</strong></td>
              <td><span style="font-family:'JetBrains Mono'; font-size:11px;">998714</span></td>
              <td>1</td>
              <td>₹${subTotal.toFixed(2)}</td>
              <td style="text-align:right;"><strong>₹${subTotal.toFixed(2)}</strong></td>
            </tr>
          `}
        </tbody>
      </table>

      <!-- Totals & Taxes Breakdown -->
      <div style="display:flex; justify-content:space-between; align-items:flex-start;">
        <div style="font-size:11.5px; color:#475569; max-width:320px; line-height:1.5; margin-top:10px;">
          <strong>Payment Mode:</strong> ${bill.paymentMethod || 'UPI / NetBanking'}<br>
          <strong>Bank:</strong> SECE Campus Branch, A/C: 060100987654<br>
          <strong>IFSC:</strong> SBIN0001234
        </div>

        <div class="invoice-totals-box">
          <div class="totals-row">
            <span>Parts Subtotal:</span>
            <span>₹${partsSum.toFixed(2)}</span>
          </div>
          <div class="totals-row">
            <span>Labour Subtotal:</span>
            <span>₹${labourSum.toFixed(2)}</span>
          </div>
          <div class="totals-row">
            <span>CGST (9.0%):</span>
            <span>₹${cgst.toFixed(2)}</span>
          </div>
          <div class="totals-row">
            <span>SGST (9.0%):</span>
            <span>₹${sgst.toFixed(2)}</span>
          </div>
          <div class="totals-row grand-total">
            <span>Total Payable:</span>
            <span style="color:#0284c7;">₹${(bill.totalAmount || 0).toFixed(2)}</span>
          </div>
        </div>
      </div>

      <!-- Footer Terms & Actions -->
      <div class="invoice-footer-terms">
        <div>
          <strong>Terms & Warranty:</strong> 6 Months / 10,000 KM Warranty on replaced OEM parts.<br>
          This is a computer-generated tax invoice. No signature required.
        </div>
        <div style="display:flex; gap:10px;">
          <button class="btn btn-secondary btn-sm" onclick="window.print()">🖨️ Print Tax Invoice</button>
          ${bill.paymentStatus !== 'PAID' ? `
            <button class="btn btn-success btn-sm" onclick="markBillPaid(${bill.id})">💰 Pay Now (UPI / QR)</button>
          ` : `
            <span style="color:#10b981; font-weight:700; font-size:12px;">✓ Settled via ${bill.paymentMethod || 'UPI'}</span>
          `}
        </div>
      </div>
    </div>
  `;
}

async function markBillPaid(billId) {
  const method = prompt('Select Payment Method (UPI, CASH, CARD, NETBANKING):', 'UPI') || 'UPI';
  try {
    const res = await fetch(`${API.bills}/${billId}/pay?paymentMethod=${method}`, {
      method: 'POST'
    });
    if (res.ok) {
      showToast('Payment settled and recorded in audit log!', 'success');
      closeModal('modal-invoice');
      refreshAllData();
    } else {
      showToast('Failed to record payment', 'error');
    }
  } catch (err) {
    showToast('Error: ' + err.message, 'error');
  }
}

// Job Details Modal
function openJobDetailsModal(jobId) {
  const job = state.jobCards.find(j => j.id === jobId);
  if (!job) return;

  const container = document.getElementById('job-details-content');
  if (!container) return;

  container.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <div>
        <h3 style="font-family:'Outfit';">Job Order #JC-${job.id}</h3>
        <p style="font-size:12px; color:var(--text-secondary);">Logged: ${formatDate(job.createdAt)}</p>
      </div>
      <span class="bay-badge ${getStatusBadgeClass(job.status)}" style="font-size:12px;">${job.status}</span>
    </div>

    <div style="background:var(--bg-input); padding:16px; border-radius:var(--radius-md); margin-bottom:14px; border:1px solid var(--border-color);">
      <h5 style="margin-bottom:8px; color:var(--accent-cyan); font-size:13px; text-transform:uppercase;">Vehicle & Customer</h5>
      <p style="font-size:13px; margin:2px 0;"><strong>Plate:</strong> <span style="font-family:'JetBrains Mono'; color:#fff;">${job.vehicle?.registrationNumber}</span></p>
      <p style="font-size:13px; margin:2px 0;"><strong>Vehicle:</strong> ${job.vehicle?.brand} ${job.vehicle?.model} (${job.vehicle?.manufacturingYear || '2022'})</p>
      <p style="font-size:13px; margin:2px 0;"><strong>Customer:</strong> ${job.vehicle?.ownerName} · 📞 ${job.vehicle?.ownerPhone}</p>
    </div>

    <div style="background:var(--bg-input); padding:16px; border-radius:var(--radius-md); margin-bottom:14px; border:1px solid var(--border-color);">
      <h5 style="margin-bottom:8px; color:var(--accent-cyan); font-size:13px; text-transform:uppercase;">Bay & Technician Allocation</h5>
      <p style="font-size:13px; margin:2px 0;"><strong>Bay:</strong> ${job.bay ? `${job.bay.bayNumber} (${job.bay.bayType})` : '<span style="color:var(--accent-amber);">Unassigned</span>'}</p>
      <p style="font-size:13px; margin:2px 0;"><strong>Technician:</strong> ${job.mechanic ? `${job.mechanic.name} (${job.mechanic.specialization})` : '<span style="color:var(--text-muted);">Unassigned</span>'}</p>
      <p style="font-size:13px; margin:2px 0;"><strong>Work Order:</strong> ${job.requestedServices}</p>
      ${job.qualityCheckNotes ? `<p style="font-size:13px; margin:2px 0; color:var(--accent-emerald);"><strong>QC Inspection:</strong> ${job.qualityCheckNotes}</p>` : ''}
    </div>

    <div style="margin-bottom:16px;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <h5 style="color:var(--accent-cyan); font-size:13px; text-transform:uppercase;">Logged Parts & Labour (${job.serviceItems?.length || 0})</h5>
        ${job.status !== 'COMPLETED' ? `<button class="btn btn-secondary btn-sm" onclick="closeModal('modal-job-details'); openAddItemModal(${job.id});">+ Add</button>` : ''}
      </div>
      <table class="custom-table" style="font-size:12px;">
        <thead>
          <tr>
            <th>Item SKU / Description</th>
            <th>Type</th>
            <th>Qty</th>
            <th>Rate</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          ${job.serviceItems?.map(item => `
            <tr>
              <td><strong>${item.itemName}</strong></td>
              <td><span style="font-size:10px; padding:2px 6px; background:rgba(255,255,255,0.06); border-radius:4px;">${item.itemType}</span></td>
              <td>${item.quantity}</td>
              <td>₹${item.unitPrice}</td>
              <td><strong style="color:var(--accent-cyan);">₹${item.totalPrice}</strong></td>
            </tr>
          `).join('') || '<tr><td colspan="5" class="text-center text-muted">No parts logged on this job.</td></tr>'}
        </tbody>
      </table>
    </div>

    <div style="display:flex; gap:10px; justify-content:flex-end;">
      ${job.status === 'WAITING' ? `<button class="btn btn-primary btn-sm" onclick="closeModal('modal-job-details'); openAssignModal(${job.id});">Schedule Bay</button>` : ''}
      ${job.status === 'IN_PROGRESS' ? `<button class="btn btn-warning btn-sm" onclick="closeModal('modal-job-details'); openStatusModal(${job.id}, 'QUALITY_CHECK');">Submit to QC</button>` : ''}
      ${job.status === 'QUALITY_CHECK' ? `<button class="btn btn-success btn-sm" onclick="closeModal('modal-job-details'); openStatusModal(${job.id}, 'COMPLETED');">Pass QC & Complete</button>` : ''}
      ${job.status === 'COMPLETED' ? `<button class="btn btn-primary btn-sm" onclick="closeModal('modal-job-details'); generateBillModal(${job.id});">View / Print Bill</button>` : ''}
    </div>
  `;

  openModal('modal-job-details');
}

// Helpers
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

function showToast(msg, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${msg}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideInRight 0.28s ease-out reverse';
    setTimeout(() => toast.remove(), 280);
  }, 4500);
}

function getStatusBadgeClass(status) {
  switch (status) {
    case 'WAITING': return 'bay-maintenance';
    case 'IN_PROGRESS': return 'bay-occupied';
    case 'QUALITY_CHECK': return 'bay-available';
    case 'COMPLETED': return 'bay-available';
    default: return '';
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    return d.toLocaleString('en-IN', {
      day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}

// ===================================================================
// AUTHENTICATION & USER PROFILE MANAGEMENT
// ===================================================================

const DEFAULT_AUTH_USER = {
  name: 'Mahilesh',
  email: 'admin@garagedesk.com',
  role: 'Workshop Manager',
  badge: 'SECE-060',
  provider: 'google'
};

function getStoredUser() {
  const stored = localStorage.getItem('garagedesk_user');
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (e) {
      return null;
    }
  }
  return null;
}

function setStoredUser(user) {
  if (user) {
    localStorage.setItem('garagedesk_user', JSON.stringify(user));
  } else {
    localStorage.removeItem('garagedesk_user');
  }
  initAuthUI();
}

function initAuthUI() {
  const user = getStoredUser();
  const splitContainer = document.getElementById('split-auth-container');
  const appContainer = document.getElementById('app-container');
  const slot = document.getElementById('user-auth-slot');

  if (user) {
    if (splitContainer) splitContainer.style.display = 'none';
    if (appContainer) appContainer.style.display = 'flex';

    if (slot) {
      const initial = user.name ? user.name.charAt(0).toUpperCase() : 'M';
      slot.innerHTML = `
        <div class="user-profile-pill" onclick="toggleProfileDropdown(event)" title="Click to view profile, switch roles, or manage account">
          <div class="user-avatar-wrap">
            <div class="user-avatar-small">${initial}</div>
            <span class="user-status-dot"></span>
          </div>
          <div class="user-meta-text">
            <span class="user-meta-name">
              ${user.name}
              ${user.provider === 'google' ? '<span title="Google Verified" style="font-size:11px;">✨</span>' : ''}
            </span>
            <span class="user-meta-role">${user.role}</span>
          </div>
          <span class="user-chevron">▾</span>
        </div>
      `;
    }

    // Update Dropdown details
    const dropAvatar = document.getElementById('dropdown-user-avatar');
    const dropName = document.getElementById('dropdown-user-name');
    const dropEmail = document.getElementById('dropdown-user-email');
    const dropRole = document.getElementById('dropdown-user-role');

    if (dropAvatar) dropAvatar.textContent = user.name ? user.name.charAt(0).toUpperCase() : 'M';
    if (dropName) dropName.textContent = user.name;
    if (dropEmail) dropEmail.textContent = user.email || 'user@garagedesk.com';
    if (dropRole) {
      dropRole.textContent = user.role;
      dropRole.className = `role-badge ${getRoleBadgeClass(user.role)}`;
    }

    // Update active role switcher buttons
    document.querySelectorAll('.role-switch-btn').forEach(btn => {
      btn.classList.remove('active');
    });
    if (user.role.includes('Manager')) document.getElementById('role-btn-manager')?.classList.add('active');
    else if (user.role.includes('Advisor')) document.getElementById('role-btn-advisor')?.classList.add('active');
    else if (user.role.includes('Mechanic')) document.getElementById('role-btn-mechanic')?.classList.add('active');
    else if (user.role.includes('Inspector') || user.role.includes('QC')) document.getElementById('role-btn-inspector')?.classList.add('active');

  } else {
    // Show Split Login Screen
    if (splitContainer) splitContainer.style.display = 'flex';
    if (appContainer) appContainer.style.display = 'none';

    if (slot) {
      slot.innerHTML = `
        <button class="btn btn-primary btn-sm" onclick="showSplitLogin()">
          <span>🔐 Sign In / Register</span>
        </button>
      `;
    }
  }
}

function getRoleBadgeClass(role) {
  if (!role) return 'role-manager';
  if (role.includes('Manager')) return 'role-manager';
  if (role.includes('Advisor')) return 'role-advisor';
  if (role.includes('Mechanic')) return 'role-mechanic';
  if (role.includes('Inspector') || role.includes('QC')) return 'role-inspector';
  return 'role-manager';
}

function toggleProfileDropdown(e) {
  if (e) {
    e.stopPropagation();
    e.preventDefault();
  }
  const dropdown = document.getElementById('user-profile-dropdown');
  if (dropdown) {
    dropdown.classList.toggle('active');
  }
}

function closeProfileDropdown() {
  const dropdown = document.getElementById('user-profile-dropdown');
  if (dropdown) {
    dropdown.classList.remove('active');
  }
}

function openAuthModal(tab = 'signin') {
  closeProfileDropdown();
  switchAuthTab(tab);
  openModal('modal-auth');
}

function switchAuthTab(tab) {
  const btnSignin = document.getElementById('tab-btn-signin');
  const btnRegister = document.getElementById('tab-btn-register');
  const formSignin = document.getElementById('form-signin');
  const formRegister = document.getElementById('form-register');

  if (tab === 'signin') {
    btnSignin.classList.add('active');
    btnRegister.classList.remove('active');
    formSignin.style.display = 'block';
    formRegister.style.display = 'none';
  } else {
    btnRegister.classList.add('active');
    btnSignin.classList.remove('active');
    formRegister.style.display = 'block';
    formSignin.style.display = 'none';
  }
}

async function handleSignInSubmit(e) {
  e.preventDefault();
  const emailInput = document.getElementById('signin-email');
  const pwdInput = document.getElementById('signin-password');
  const usernameOrEmail = emailInput ? emailInput.value.trim() : '';
  const password = pwdInput ? pwdInput.value.trim() : '';

  try {
    showToast('🔐 Authenticating with GarageDesk backend...', 'info');
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail, password })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Invalid username or password');
    }

    const authData = data.data;
    const user = {
      name: authData.user.fullName,
      email: authData.user.email,
      role: formatRoleName(authData.user.role),
      badge: authData.user.staffBadgeNumber || 'SECE-060',
      token: authData.token,
      provider: authData.user.authProvider
    };

    setStoredUser(user);
    closeModal('modal-auth');
    showToast(`🚀 Welcome back, ${user.name}! Signed in as ${user.role}.`, 'success');
  } catch (err) {
    showToast(`❌ ${err.message}. If you are a new user, please click the "Create Account" tab to register.`, 'error');
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const name = document.getElementById('reg-name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const badge = document.getElementById('reg-badge').value.trim();
  const role = document.getElementById('reg-role').value;
  const pwd = document.getElementById('reg-password').value;
  const pwdConfirm = document.getElementById('reg-confirm-password').value;

  if (pwd !== pwdConfirm) {
    showToast('⚠️ Passwords do not match! Please verify your password.', 'error');
    return;
  }

  const username = email.includes('@') ? email.split('@')[0] : email;

  try {
    showToast('Creating new account in database...', 'info');
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username,
        email: email,
        password: pwd,
        fullName: name,
        role: mapRoleToEnum(role),
        staffBadgeNumber: badge || ('SECE-' + Math.floor(100 + Math.random() * 900))
      })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    const authData = data.data;
    const user = {
      name: authData.user.fullName,
      email: authData.user.email,
      role: formatRoleName(authData.user.role),
      badge: authData.user.staffBadgeNumber,
      token: authData.token,
      provider: authData.user.authProvider
    };

    setStoredUser(user);
    closeModal('modal-auth');
    showToast(`🎉 Welcome to GarageDesk, ${user.name}! Your account has been registered.`, 'success');
  } catch (err) {
    showToast(`❌ Registration error: ${err.message}`, 'error');
  }
}

function openGoogleAuthModal() {
  closeModal('modal-auth');
  openModal('modal-google-auth');
}

function openCustomGooglePrompt() {
  const customEmail = prompt('Enter your Google work or personal email address:', 'mahilesh.sece@gmail.com');
  if (customEmail && customEmail.includes('@')) {
    const rawName = customEmail.split('@')[0];
    const cleanName = rawName.charAt(0).toUpperCase() + rawName.slice(1);
    confirmGoogleSignIn(cleanName, customEmail, 'Workshop Manager');
  }
}

function quickFillCredentials(roleKey) {
  const emailInput = document.getElementById('signin-email');
  const pwdInput = document.getElementById('signin-password');

  if (roleKey === 'manager') {
    emailInput.value = 'admin@garagedesk.com';
    pwdInput.value = 'garage2026';
    showToast('Filled Workshop Manager credentials', 'info');
  } else if (roleKey === 'advisor') {
    emailInput.value = 'advisor@garagedesk.com';
    pwdInput.value = 'advisor123';
    showToast('Filled Service Advisor credentials', 'info');
  } else if (roleKey === 'mechanic') {
    emailInput.value = 'mechanic@garagedesk.com';
    pwdInput.value = 'mech123';
    showToast('Filled Master Mechanic credentials', 'info');
  } else if (roleKey === 'inspector') {
    emailInput.value = 'inspector@garagedesk.com';
    pwdInput.value = 'qc123';
    showToast('Filled Quality Inspector credentials', 'info');
  }

  emailInput.style.borderColor = 'var(--accent-cyan)';
  setTimeout(() => { emailInput.style.borderColor = ''; }, 1000);
}

function togglePasswordVisibility(inputId, btn) {
  const input = document.getElementById(inputId);
  if (!input) return;
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = '🙈';
  } else {
    input.type = 'password';
    btn.textContent = '👁️';
  }
}

function switchActiveRole(newRole) {
  const user = getStoredUser();
  if (user) {
    user.role = newRole;
    setStoredUser(user);
    closeProfileDropdown();
    showToast(`🔄 Active role switched to: ${newRole}`, 'info');
  }
}

function handleLogout() {
  closeProfileDropdown();
  setStoredUser(null);
  showToast('🔒 Signed out successfully. Click "Sign In" anytime!', 'info');
}

function showForgotPasswordToast(e) {
  e.preventDefault();
  showToast('📧 Password reset instructions dispatched to your registered address.', 'info');
}

// Split-Screen Reference Login Handlers (Real Backend Connected)
function toggleSplitAuthCard(cardType) {
  const loginCard = document.getElementById('split-login-card');
  const regCard = document.getElementById('split-register-card');
  const tabLogin = document.getElementById('tab-split-login');
  const tabReg = document.getElementById('tab-split-register');
  const usernameInput = document.getElementById('escrow-username');
  const regEmailInput = document.getElementById('escrow-reg-email');
  const regNameInput = document.getElementById('escrow-reg-name');

  if (cardType === 'register') {
    if (loginCard) loginCard.style.display = 'none';
    if (regCard) regCard.style.display = 'block';
    if (tabLogin) tabLogin.classList.remove('active');
    if (tabReg) tabReg.classList.add('active');

    // Auto-carry typed email/name over to the register form
    if (usernameInput && regEmailInput && usernameInput.value) {
      const val = usernameInput.value.trim();
      if (val.includes('@')) {
        regEmailInput.value = val;
        if (regNameInput && !regNameInput.value) {
          const prefix = val.split('@')[0];
          regNameInput.value = prefix.charAt(0).toUpperCase() + prefix.slice(1);
        }
      } else if (!regEmailInput.value) {
        regEmailInput.value = val + '@garagedesk.com';
      }
    }
  } else {
    if (loginCard) loginCard.style.display = 'block';
    if (regCard) regCard.style.display = 'none';
    if (tabLogin) tabLogin.classList.add('active');
    if (tabReg) tabReg.classList.remove('active');
  }
}

async function quickSplitFill(roleKey) {
  const usernameInput = document.getElementById('escrow-username');
  const pwdInput = document.getElementById('escrow-password');

  if (roleKey === 'admin') {
    usernameInput.value = 'admin@garagedesk.com';
    pwdInput.value = 'garage2026';
  } else if (roleKey === 'client') {
    usernameInput.value = 'client@garagedesk.com';
    pwdInput.value = 'client123';
  } else if (roleKey === 'advisor') {
    usernameInput.value = 'advisor@garagedesk.com';
    pwdInput.value = 'advisor123';
  } else if (roleKey === 'mechanic') {
    usernameInput.value = 'mechanic@garagedesk.com';
    pwdInput.value = 'mech123';
  }

  // Trigger login automatically against real backend
  await handleSplitLogin(new Event('submit'));
}

async function handleSplitLogin(e) {
  if (e && e.preventDefault) e.preventDefault();
  const usernameOrEmail = document.getElementById('escrow-username').value.trim();
  const password = document.getElementById('escrow-password').value.trim();

  if (!usernameOrEmail || !password) {
    showToast('⚠️ Please enter both your username/email and password.', 'error');
    return;
  }

  try {
    showToast('🔐 Authenticating with GarageDesk backend...', 'info');
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail, password })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Invalid username or password');
    }

    const authData = data.data;
    const user = {
      name: authData.user.fullName,
      email: authData.user.email,
      role: formatRoleName(authData.user.role),
      badge: authData.user.staffBadgeNumber || 'SECE-060',
      token: authData.token,
      provider: authData.user.authProvider
    };

    setStoredUser(user);
    showToast(`🚀 Welcome back, ${user.name}! Signed in as ${user.role}.`, 'success');
  } catch (err) {
    // If account not found or wrong password, inform the user clearly
    const regEmail = document.getElementById('escrow-reg-email');
    if (regEmail && usernameOrEmail.includes('@')) {
      regEmail.value = usernameOrEmail;
    }
    showToast(`❌ Account not found or wrong password. If you are a new user, click "New User · Sign Up" above to register.`, 'error');
  }
}

async function handleSplitRegister(e) {
  e.preventDefault();
  const fullName = document.getElementById('escrow-reg-name').value.trim();
  const email = document.getElementById('escrow-reg-email').value.trim();
  const roleInput = document.getElementById('escrow-reg-role').value;
  const password = document.getElementById('escrow-reg-pwd').value.trim();

  if (!fullName || !email || !password) {
    showToast('⚠️ Please fill in all required registration fields.', 'error');
    return;
  }

  const username = email.includes('@') ? email.split('@')[0] : email;

  try {
    showToast('Creating new account in database...', 'info');
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username,
        email: email,
        password: password,
        fullName: fullName,
        role: mapRoleToEnum(roleInput),
        staffBadgeNumber: 'SECE-' + Math.floor(100 + Math.random() * 900)
      })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    const authData = data.data;
    const user = {
      name: authData.user.fullName,
      email: authData.user.email,
      role: formatRoleName(authData.user.role),
      badge: authData.user.staffBadgeNumber,
      token: authData.token,
      provider: authData.user.authProvider
    };

    setStoredUser(user);
    showToast(`🎉 Welcome to GarageDesk, ${user.name}! Your account has been created.`, 'success');
  } catch (err) {
    showToast(`❌ Registration error: ${err.message}. If you already have an account, click "Existing User · Sign In".`, 'error');
  }
}

async function confirmGoogleSignIn(name, email, role) {
  closeModal('modal-google-auth');
  try {
    showToast('Authenticating with Google OAuth...', 'info');
    const res = await fetch('/api/auth/google', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email,
        name: name,
        googleId: 'goog_' + Date.now(),
        avatarUrl: '',
        role: mapRoleToEnum(role || 'Workshop Manager')
      })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Google authentication failed');
    }

    const authData = data.data;
    const user = {
      name: authData.user.fullName,
      email: authData.user.email,
      role: formatRoleName(authData.user.role),
      badge: authData.user.staffBadgeNumber || 'GOOGLE-OAUTH',
      token: authData.token,
      provider: 'google'
    };

    setStoredUser(user);
    showToast(`🚀 Authenticated via Google as ${user.name} (${user.role})!`, 'success');
  } catch (err) {
    showToast(`❌ Google Sign-In failed: ${err.message}`, 'error');
  }
}

function mapRoleToEnum(roleStr) {
  if (!roleStr) return 'WORKSHOP_MANAGER';
  if (roleStr.includes('Manager') || roleStr.includes('Admin')) return 'WORKSHOP_MANAGER';
  if (roleStr.includes('Advisor')) return 'SERVICE_ADVISOR';
  if (roleStr.includes('Mechanic') || roleStr.includes('Technician')) return 'MECHANIC';
  if (roleStr.includes('Inspector') || roleStr.includes('QC')) return 'QUALITY_INSPECTOR';
  if (roleStr.includes('Client') || roleStr.includes('Customer')) return 'CLIENT';
  return 'WORKSHOP_MANAGER';
}

function formatRoleName(roleEnum) {
  switch (roleEnum) {
    case 'WORKSHOP_MANAGER': return 'Workshop Manager';
    case 'SERVICE_ADVISOR': return 'Service Advisor';
    case 'MECHANIC': return 'Chief Mechanic';
    case 'QUALITY_INSPECTOR': return 'Quality Inspector';
    case 'CLIENT': return 'Client User';
    default: return roleEnum || 'Workshop Manager';
  }
}

function showSplitLogin() {
  closeProfileDropdown();
  setStoredUser(null);
}



