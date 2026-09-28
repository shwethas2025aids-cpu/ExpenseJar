/**
 * ExpenseJar — Frontend Application Controller
 * Connects directly to Spring Boot REST APIs on http://localhost:8080
 */

const API_BASE = '/api';

// Application State
let activeUserId = null;
let usersList = [];
let categoriesList = [];
let budgetsList = [];

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    setupDateTimeDefaults();
    setupEventListeners();
    await loadInitialData();
}

function setupDateTimeDefaults() {
    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];
    const monthStr = now.toISOString().slice(0, 7);

    // Set form input defaults
    const expenseDateInput = document.getElementById('expenseDateInput');
    const budgetMonthInput = document.getElementById('budgetMonthInput');
    if (expenseDateInput) expenseDateInput.value = todayStr;
    if (budgetMonthInput) budgetMonthInput.value = monthStr;

    // Set header badge
    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    const formattedMonth = `${monthNames[now.getMonth()]} ${now.getFullYear()}`;
    const currentMonthText = document.getElementById('currentMonthText');
    const metricMonthLabel = document.getElementById('metricMonthLabel');
    if (currentMonthText) currentMonthText.textContent = formattedMonth;
    if (metricMonthLabel) metricMonthLabel.textContent = formattedMonth;
}

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.form-tab-content').forEach(c => c.classList.remove('active'));

            btn.classList.add('active');
            const targetTab = document.getElementById(btn.dataset.tab);
            if (targetTab) targetTab.classList.add('active');
        });
    });

    // Alert Banner Close
    const btnCloseAlert = document.getElementById('btnCloseAlert');
    if (btnCloseAlert) {
        btnCloseAlert.addEventListener('click', hideAlertBanner);
    }

    // User Selection Change
    const activeUserSelect = document.getElementById('activeUserSelect');
    if (activeUserSelect) {
        activeUserSelect.addEventListener('change', (e) => {
            activeUserId = Number(e.target.value);
            refreshUserData();
        });
    }

    // Modal Events
    const btnOpenNewUserModal = document.getElementById('btnOpenNewUserModal');
    const btnCloseUserModal = document.getElementById('btnCloseUserModal');
    const modalNewUser = document.getElementById('modalNewUser');

    if (btnOpenNewUserModal) {
        btnOpenNewUserModal.addEventListener('click', () => modalNewUser.classList.add('active'));
    }
    if (btnCloseUserModal) {
        btnCloseUserModal.addEventListener('click', () => modalNewUser.classList.remove('active'));
    }

    // Form Submissions
    document.getElementById('formAddExpense').addEventListener('submit', handleAddExpense);
    document.getElementById('formSetBudget').addEventListener('submit', handleSetBudget);
    document.getElementById('formAddCategory').addEventListener('submit', handleAddCategory);
    document.getElementById('formNewUser').addEventListener('submit', handleCreateUser);
}

// -------------------------------------------------------------
// Data Fetching & Sync
// -------------------------------------------------------------

async function loadInitialData() {
    try {
        await Promise.all([loadUsers(), loadCategories()]);
        if (usersList.length > 0) {
            activeUserId = usersList[0].id;
            const activeUserSelect = document.getElementById('activeUserSelect');
            if (activeUserSelect) activeUserSelect.value = activeUserId;
            await refreshUserData();
        } else {
            // Auto create sample user and categories if database is fresh
            await seedInitialData();
        }
    } catch (err) {
        console.error('Error loading initial data:', err);
    }
}

async function seedInitialData() {
    try {
        // Create sample user Sakthi
        const resUser = await fetch(`${API_BASE}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: 'Sakthi', email: 'sakthi@example.com' })
        });
        if (resUser.ok) {
            const user = await resUser.json();
            usersList = [user];
            activeUserId = user.id;
            populateUserDropdown();
        }

        // Create sample categories: Food, Travel, Stationery
        const defaultCategories = ['Food', 'Travel', 'Stationery'];
        for (const catName of defaultCategories) {
            await fetch(`${API_BASE}/categories`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: catName })
            });
        }
        await loadCategories();

        // Create sample budget for Food in current month
        const now = new Date();
        const currentMonthStr = now.toISOString().slice(0, 7);
        if (categoriesList.length > 0 && activeUserId) {
            await fetch(`${API_BASE}/budgets`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    userId: activeUserId,
                    categoryId: categoriesList[0].id,
                    amount: 5000.0,
                    month: currentMonthStr
                })
            });
        }

        await refreshUserData();
    } catch (e) {
        console.error('Seeding initial data failed:', e);
    }
}

async function loadUsers() {
    const res = await fetch(`${API_BASE}/users`);
    if (res.ok) {
        usersList = await res.json();
        populateUserDropdown();
    }
}

function populateUserDropdown() {
    const select = document.getElementById('activeUserSelect');
    if (!select) return;
    select.innerHTML = '';
    usersList.forEach(u => {
        const opt = document.createElement('option');
        opt.value = u.id;
        opt.textContent = `${u.name} (${u.email})`;
        select.appendChild(opt);
    });
    if (activeUserId) select.value = activeUserId;
}

async function loadCategories() {
    const res = await fetch(`${API_BASE}/categories`);
    if (res.ok) {
        categoriesList = await res.json();
        populateCategoryDropdowns();
    }
}

function populateCategoryDropdowns() {
    const expSelect = document.getElementById('expenseCategorySelect');
    const budSelect = document.getElementById('budgetCategorySelect');

    const renderOpts = (el) => {
        if (!el) return;
        el.innerHTML = '<option value="" disabled selected>Select category</option>';
        categoriesList.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = c.name;
            el.appendChild(opt);
        });
    };

    renderOpts(expSelect);
    renderOpts(budSelect);
}

async function refreshUserData() {
    if (!activeUserId) return;
    await Promise.all([
        loadBudgets(),
        loadCurrentMonthSummary(),
        loadSpendingTrend(),
        loadUserExpenses()
    ]);
}

async function loadBudgets() {
    if (!activeUserId) return;
    const res = await fetch(`${API_BASE}/budgets/user/${activeUserId}`);
    if (res.ok) {
        budgetsList = await res.json();
    } else {
        budgetsList = [];
    }
}

// -------------------------------------------------------------
// Current Month Summary & Metric Calculations
// -------------------------------------------------------------

async function loadCurrentMonthSummary() {
    if (!activeUserId) return;
    try {
        const res = await fetch(`${API_BASE}/expenses/summary/current-month/${activeUserId}`);
        let summary = [];
        if (res.ok) {
            summary = await res.json();
        }

        renderCategorySpendingBreakdown(summary);
        updateTopMetrics(summary);
    } catch (err) {
        console.error('Failed to load current month summary:', err);
    }
}

function updateTopMetrics(summary) {
    const now = new Date();
    const currentMonthStr = now.toISOString().slice(0, 7);

    // Sum spend from current month summary
    const totalSpend = summary.reduce((acc, curr) => acc + (curr.totalSpent || 0), 0);

    // Filter budgets for active user and active month
    const activeBudgets = budgetsList.filter(b => b.month === currentMonthStr);
    const totalBudget = activeBudgets.reduce((acc, b) => acc + (b.amount || 0), 0);

    // Calculate percentage used
    let overallPercentage = 0;
    if (totalBudget > 0) {
        overallPercentage = Math.round((totalSpend / totalBudget) * 100);
    }

    // Count categories >= 90%
    let alertsCount = 0;
    activeBudgets.forEach(b => {
        const catSpent = summary.find(s => s.category.toLowerCase() === b.category.name.toLowerCase());
        const spent = catSpent ? catSpent.totalSpent : 0;
        if (b.amount > 0 && ((spent / b.amount) * 100) >= 90) {
            alertsCount++;
        }
    });

    document.getElementById('totalCurrentMonthSpend').textContent = `₹${totalSpend.toFixed(2)}`;
    document.getElementById('totalMonthlyBudget').textContent = `₹${totalBudget.toFixed(2)}`;
    document.getElementById('overallPercentUsed').textContent = `${overallPercentage}%`;
    document.getElementById('alertCount').textContent = alertsCount;

    const usageDesc = document.getElementById('overallUsageDesc');
    if (overallPercentage >= 90) {
        usageDesc.textContent = 'Critical threshold exceeded!';
        usageDesc.style.color = '#fb7185';
    } else {
        usageDesc.textContent = 'Within safe limit';
        usageDesc.style.color = 'var(--text-dim)';
    }
}

function renderCategorySpendingBreakdown(summary) {
    const container = document.getElementById('categorySpendingList');
    if (!container) return;

    const now = new Date();
    const currentMonthStr = now.toISOString().slice(0, 7);

    if (summary.length === 0 && budgetsList.length === 0) {
        container.innerHTML = '<div class="empty-state">No expense records found for the current month.</div>';
        return;
    }

    // Combine category budgets and expenses
    const displayList = [];
    const processedCategories = new Set();

    summary.forEach(item => {
        const budgetObj = budgetsList.find(b => 
            b.month === currentMonthStr && 
            b.category.name.toLowerCase() === item.category.toLowerCase()
        );
        const budgetAmount = budgetObj ? budgetObj.amount : 0;
        const percent = budgetAmount > 0 ? Math.round((item.totalSpent / budgetAmount) * 100) : 0;

        displayList.push({
            name: item.category,
            totalSpent: item.totalSpent,
            budget: budgetAmount,
            percentageUsed: percent,
            isAlert: percent >= 90
        });
        processedCategories.add(item.category.toLowerCase());
    });

    // Also include categories that have a budget set even if spent is 0
    budgetsList.filter(b => b.month === currentMonthStr).forEach(b => {
        if (!processedCategories.has(b.category.name.toLowerCase())) {
            displayList.push({
                name: b.category.name,
                totalSpent: 0,
                budget: b.amount,
                percentageUsed: 0,
                isAlert: false
            });
        }
    });

    if (displayList.length === 0) {
        container.innerHTML = '<div class="empty-state">No category spending or budget set for this month.</div>';
        return;
    }

    container.innerHTML = displayList.map(item => {
        let fillClass = 'safe';
        let badgeClass = 'safe';
        let badgeText = 'Safe';

        if (item.percentageUsed >= 90) {
            fillClass = 'alert';
            badgeClass = 'alert';
            badgeText = 'Alert ≥90%';
        } else if (item.percentageUsed >= 70) {
            fillClass = 'warn';
        }

        const clampedWidth = Math.min(item.percentageUsed, 100);

        return `
            <div class="cat-item-card ${item.isAlert ? 'alert-active' : ''}">
                <div class="cat-item-header">
                    <span class="cat-name">${escapeHtml(item.name)}</span>
                    <span class="cat-badge ${badgeClass}">${badgeText}</span>
                </div>
                <div class="cat-numbers">
                    <div class="cat-spent">₹${item.totalSpent.toFixed(2)}</div>
                    <div class="cat-budget">Budget: ₹${item.budget.toFixed(2)}</div>
                </div>
                <div class="cat-progress-track">
                    <div class="cat-progress-fill ${fillClass}" style="width: ${clampedWidth}%;"></div>
                </div>
                <div class="cat-percent-text">${item.percentageUsed}% of budget used</div>
            </div>
        `;
    }).join('');
}

// -------------------------------------------------------------
// Month-over-Month Spending Trend
// -------------------------------------------------------------

async function loadSpendingTrend() {
    if (!activeUserId) return;
    try {
        const res = await fetch(`${API_BASE}/expenses/trend/${activeUserId}`);
        const wrapper = document.getElementById('trendBarsWrapper');
        if (!wrapper) return;

        if (!res.ok) {
            wrapper.innerHTML = '<div class="empty-state">Unable to load trend data.</div>';
            return;
        }

        const trends = await res.json();
        if (trends.length === 0) {
            wrapper.innerHTML = '<div class="empty-state" style="width: 100%;">No spending records logged yet.</div>';
            return;
        }

        const maxSpent = Math.max(...trends.map(t => t.totalSpent), 100);
        const currentMonthStr = new Date().toISOString().slice(0, 7);

        wrapper.innerHTML = trends.map(t => {
            const heightPercent = Math.max((t.totalSpent / maxSpent) * 100, 10);
            const isCurrent = t.month === currentMonthStr;

            return `
                <div class="trend-bar-col">
                    <div class="trend-bar-val">₹${Math.round(t.totalSpent)}</div>
                    <div class="trend-bar ${isCurrent ? 'active-month' : ''}" style="height: ${heightPercent}%;"></div>
                    <div class="trend-bar-label">${t.month}</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load spending trend:', err);
    }
}

// -------------------------------------------------------------
// Expenses Table
// -------------------------------------------------------------

async function loadUserExpenses() {
    if (!activeUserId) return;
    try {
        const res = await fetch(`${API_BASE}/expenses/user/${activeUserId}`);
        const tbody = document.getElementById('expensesTableBody');
        if (!tbody) return;

        if (!res.ok) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No expenses found.</td></tr>';
            return;
        }

        const expenses = await res.json();
        if (expenses.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No expenses logged yet. Add your first expense above!</td></tr>';
            return;
        }

        // Sort descending by date and id
        expenses.sort((a, b) => new Date(b.date) - new Date(a.date) || b.id - a.id);

        tbody.innerHTML = expenses.map(e => `
            <tr>
                <td>#${e.id}</td>
                <td>${e.date}</td>
                <td><span class="table-badge">${escapeHtml(e.categoryName || 'General')}</span></td>
                <td class="table-amount">₹${e.amount.toFixed(2)}</td>
                <td>
                    <button type="button" class="btn-delete-row" title="Delete Expense" onclick="handleDeleteExpense(${e.id})">
                        <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Failed to load expenses list:', err);
    }
}

// -------------------------------------------------------------
// Form Handlers
// -------------------------------------------------------------

async function handleAddExpense(e) {
    e.preventDefault();
    if (!activeUserId) {
        showAlert('Please select or create a user first', 'danger');
        return;
    }

    const categoryId = document.getElementById('expenseCategorySelect').value;
    const amount = parseFloat(document.getElementById('expenseAmountInput').value);
    const date = document.getElementById('expenseDateInput').value;

    // RULE 1 Validation
    if (isNaN(amount) || amount <= 0) {
        showAlert('Expense amount must be greater than zero', 'danger');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/expenses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: activeUserId,
                categoryId: Number(categoryId),
                amount: amount,
                date: date
            })
        });

        const data = await res.json();

        if (res.status === 201) {
            // Check for RULE 2 Budget Alert
            if (data.alert) {
                showAlert(
                    `⚠️ Budget Alert: Spend in "${data.category}" reached ${data.percentageUsed}% of budget (₹${data.totalSpent.toFixed(2)} / ₹${data.budget.toFixed(2)})!`,
                    'danger'
                );
            } else {
                showAlert(
                    `Expense of ₹${amount.toFixed(2)} logged for "${data.category}". Spend is at ${data.percentageUsed}% of budget.`,
                    'success'
                );
            }

            // Reset amount field
            document.getElementById('expenseAmountInput').value = '';
            await refreshUserData();
        } else {
            showAlert(data.error || 'Failed to add expense', 'danger');
        }
    } catch (err) {
        showAlert('Network error while adding expense', 'danger');
    }
}

async function handleSetBudget(e) {
    e.preventDefault();
    if (!activeUserId) {
        showAlert('Please select or create a user first', 'danger');
        return;
    }

    const categoryId = document.getElementById('budgetCategorySelect').value;
    const amount = parseFloat(document.getElementById('budgetAmountInput').value);
    const month = document.getElementById('budgetMonthInput').value;

    if (isNaN(amount) || amount <= 0) {
        showAlert('Budget amount must be greater than zero', 'danger');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/budgets`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: activeUserId,
                categoryId: Number(categoryId),
                amount: amount,
                month: month
            })
        });

        const data = await res.json();
        if (res.ok) {
            showAlert(`Budget of ₹${amount.toFixed(2)} set for "${data.category.name}" (${month})`, 'success');
            document.getElementById('budgetAmountInput').value = '';
            await refreshUserData();
        } else {
            showAlert(data.error || 'Failed to set budget', 'danger');
        }
    } catch (err) {
        showAlert('Network error while setting budget', 'danger');
    }
}

async function handleAddCategory(e) {
    e.preventDefault();
    const nameInput = document.getElementById('newCategoryName');
    const name = nameInput.value.trim();

    if (!name) return;

    try {
        const res = await fetch(`${API_BASE}/categories`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        const data = await res.json();
        if (res.ok) {
            showAlert(`Category "${data.name}" created successfully`, 'success');
            nameInput.value = '';
            await loadCategories();
        } else {
            showAlert(data.error || 'Failed to create category', 'danger');
        }
    } catch (err) {
        showAlert('Network error while creating category', 'danger');
    }
}

async function handleCreateUser(e) {
    e.preventDefault();
    const name = document.getElementById('newUserName').value.trim();
    const email = document.getElementById('newUserEmail').value.trim();

    try {
        const res = await fetch(`${API_BASE}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email })
        });

        const data = await res.json();
        if (res.ok) {
            showAlert(`User "${data.name}" created successfully`, 'success');
            document.getElementById('modalNewUser').classList.remove('active');
            document.getElementById('newUserName').value = '';
            document.getElementById('newUserEmail').value = '';
            await loadUsers();
            activeUserId = data.id;
            document.getElementById('activeUserSelect').value = data.id;
            await refreshUserData();
        } else {
            showAlert(data.error || 'Failed to create user', 'danger');
        }
    } catch (err) {
        showAlert('Network error while creating user', 'danger');
    }
}

async function handleDeleteExpense(id) {
    if (!confirm('Are you sure you want to delete this expense record?')) return;
    try {
        const res = await fetch(`${API_BASE}/expenses/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showAlert('Expense record deleted', 'success');
            await refreshUserData();
        } else {
            showAlert('Failed to delete expense', 'danger');
        }
    } catch (err) {
        showAlert('Network error while deleting expense', 'danger');
    }
}

// -------------------------------------------------------------
// UI Utilities
// -------------------------------------------------------------

function showAlert(message, type = 'success') {
    const banner = document.getElementById('alertBanner');
    const title = document.getElementById('alertTitle');
    const desc = document.getElementById('alertDesc');
    const icon = document.getElementById('alertIcon');

    if (!banner) return;

    banner.className = `alert-banner ${type}`;
    if (type === 'danger') {
        title.textContent = 'Alert Notification';
        icon.innerHTML = `
            <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
        `;
    } else {
        title.textContent = 'Success';
        icon.innerHTML = `
            <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path>
            </svg>
        `;
    }

    desc.textContent = message;
    banner.style.display = 'flex';

    // Auto dismiss after 6 seconds if success
    if (type === 'success') {
        setTimeout(hideAlertBanner, 6000);
    }
}

function hideAlertBanner() {
    const banner = document.getElementById('alertBanner');
    if (banner) banner.style.display = 'none';
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}
