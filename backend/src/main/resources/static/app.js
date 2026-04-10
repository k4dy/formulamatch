const API = '/api/v1';
const PAGE_SIZE = 20;
const SUBS_SIZE = 15;

const getApiKey = () => localStorage.getItem('fm_api_key');
const setApiKey = (k) => localStorage.setItem('fm_api_key', k);
const clearApiKey = () => localStorage.removeItem('fm_api_key');

let searchPage = 0;
let searchTotal = 0;
let subsPage = 0;
let subsTotalPages = 0;
let currentProductIngredientCount = 0;
let currentProductId = null;
let currentTop5Filter = 5;
let currentProductTop5 = {}; // {position: ingredientId} for reference product

const stateHome = document.getElementById('state-home');
const stateResults = document.getElementById('state-results');
const stateProduct = document.getElementById('state-product');
const stateAuth = document.getElementById('state-auth');
const stateAccount = document.getElementById('state-account');
const homeInput = document.getElementById('home-input');
const resultsInput = document.getElementById('results-input');


window.addEventListener('hashchange', route);
window.addEventListener('load', route);

function route() {
  const hash = location.hash || '#/';

  if (hash === '#/' || hash === '') { show(stateHome); return; }

  if (hash.startsWith('#/search')) {
    const params = new URLSearchParams(hash.slice('#/search?'.length));
    const q = params.get('q') || '';
    resultsInput.value = q;
    show(stateResults);
    runSearch(q, 0, false);
    return;
  }

  if (hash.startsWith('#/product/')) {
    const id = parseInt(hash.split('/')[2], 10);
    if (id) { show(stateProduct); loadProduct(id); }
    return;
  }

  if (hash === '#/auth') { show(stateAuth); return; }

  if (hash === '#/account') {
    if (!getApiKey()) { location.hash = '#/auth'; return; }
    show(stateAccount);
    loadAccount();
    return;
  }

  show(stateHome);
}

function show(el) {
  [stateHome, stateResults, stateProduct, stateAuth, stateAccount].forEach(s => s.classList.add('hidden'));
  el.classList.remove('hidden');
  window.scrollTo(0, 0);
  updateAuthLinks();
}

document.getElementById('home-btn').addEventListener('click', () => triggerSearch(homeInput.value.trim()));
homeInput.addEventListener('keydown', e => { if (e.key === 'Enter') triggerSearch(homeInput.value.trim()); });

document.getElementById('results-btn').addEventListener('click', () => triggerSearch(resultsInput.value.trim()));
resultsInput.addEventListener('keydown', e => { if (e.key === 'Enter') triggerSearch(resultsInput.value.trim()); });

document.getElementById('product-back-btn').addEventListener('click', () => history.back());

document.getElementById('results-more-btn').addEventListener('click', () => {
  runSearch(resultsInput.value.trim(), searchPage + 1, true);
});

function triggerSearch(q) {
  if (!q) return;
  location.hash = `#/search?q=${encodeURIComponent(q)}`;
}

function goToProduct(id) {
  location.hash = `#/product/${id}`;
}

async function runSearch(q, page, append) {
  const statusEl = document.getElementById('results-status');
  const listEl   = document.getElementById('results-list');
  const moreRow  = document.getElementById('results-more');

  setStatus(statusEl, '<span class="spinner"></span>Searching…');
  if (!append) listEl.innerHTML = '';

  try {
    const url = `/products/search?q=${encodeURIComponent(q)}&page=${page}&size=${PAGE_SIZE}`;
    const data = await apiFetch(url);
    searchPage  = page;
    searchTotal = data.totalElements;

    if (data.content.length === 0 && page === 0) {
      setStatus(statusEl, 'No results for <strong>' + escHtml(q) + '</strong>');
      moreRow.classList.add('hidden');
      return;
    }

    if (page === 0) setStatus(statusEl, 'About ' + searchTotal.toLocaleString() + ' results');
    else setStatus(statusEl, '');

    data.content.forEach(p => listEl.appendChild(buildResultItem(p)));
    moreRow.classList.toggle('hidden', page + 1 >= data.totalPages);

  } catch (e) {
    setStatus(statusEl, escHtml(e.message), 'error');
  }
}

function buildResultItem(p) {
  const el = document.createElement('div');
  el.className = 'result-item';
  el.innerHTML = `
    <div>
      <span class="result-name">${escHtml(p.name)}</span>
      <span class="result-brand">${escHtml(p.brandName || '')}</span>
    </div>
    <div class="result-meta">
      <span>${p.ingredientCount != null ? p.ingredientCount + ' ingredients' : ''}</span>
    </div>
  `;
  el.addEventListener('click', () => goToProduct(p.id));
  return el;
}

async function loadProduct(id) {
  currentProductId = id;
  subsPage = 0;
  currentTop5Filter = 5;

  const statusEl  = document.getElementById('product-status');
  const contentEl = document.getElementById('product-content');

  setStatus(statusEl, '<span class="spinner"></span>Loading product…');
  contentEl.innerHTML = '';

  try {
    const product = await apiFetch(`/products/${id}`);
    const counts = await apiFetch(`/products/${id}/substitutes/counts`);

    currentProductIngredientCount = (product.ingredients || []).length;
    currentProductTop5 = {};
    for (let i = 0; i < (product.ingredients || []).length; i++) {
      const ing = product.ingredients[i];
      if (ing.position <= 5) {
        currentProductTop5[ing.position] = ing.cosingId;
      }
    }

    currentTop5Filter = 0;
    for (let i = 5; i >= 1; i--) {
      if ((counts[i] || 0) > 0) { currentTop5Filter = i; break; }
    }

    setStatus(statusEl, '');
    contentEl.innerHTML = renderProductHeader(product) +
                          renderIngredients(product.ingredients || []) +
                          renderSubsTabs(counts, currentTop5Filter);

    updateHeaderBadges(counts, contentEl);

    contentEl.querySelectorAll('.subs-tab').forEach(tab => {
      tab.addEventListener('click', async () => {
        const filter = parseInt(tab.dataset.filter, 10);
        if (filter === currentTop5Filter) return;
        currentTop5Filter = filter;
        subsPage = 0;
        contentEl.querySelectorAll('.subs-tab').forEach(t =>
          t.classList.toggle('active', t === tab)
        );
        await loadSubsForTab(id, filter, contentEl);
      });
    });

    await loadSubsForTab(id, currentTop5Filter, contentEl);

  } catch (e) {
    setStatus(statusEl, escHtml(e.message), 'error');
  }
}

async function loadSubsForTab(productId, top5Filter, contentEl) {
  const subsContent = contentEl.querySelector('#subs-content');
  subsContent.innerHTML = '<p style="padding:12px 0"><span class="spinner"></span></p>';
  subsPage = 0;

  try {
    const data = await apiFetch(`/products/${productId}/substitutes?top5Filter=${top5Filter}&page=0&size=${SUBS_SIZE}`);
    subsTotalPages = data.totalPages;

    let topIngredients = {};
    if (data.content.length > 0) {
      let idList = '';
      for (let i = 0; i < data.content.length; i++) {
        if (i > 0) idList += ',';
        idList += data.content[i].productId;
      }
      topIngredients = await apiFetch(`/products/top-ingredients?ids=${idList}`);
    }

    subsContent.innerHTML = renderSubsTable(data, topIngredients);

    const moreBtn = subsContent.querySelector('#subs-more-btn');
    if (moreBtn) moreBtn.addEventListener('click', () => loadMoreSubs(productId, top5Filter, contentEl, topIngredients));

    wireSubRows(subsContent);
  } catch (e) {
    subsContent.innerHTML = `<p class="status-line error" style="padding:12px 0">${escHtml(e.message)}</p>`;
  }
}

async function loadMoreSubs(productId, top5Filter, contentEl, existingTopIngredients) {
  subsPage++;
  const subsContent = contentEl.querySelector('#subs-content');
  const moreBtn = subsContent.querySelector('#subs-more-btn');
  if (moreBtn) moreBtn.textContent = 'Loading…';

  try {
    const data = await apiFetch(`/products/${productId}/substitutes?top5Filter=${top5Filter}&page=${subsPage}&size=${SUBS_SIZE}`);
    subsTotalPages = data.totalPages;

    let newTopIngredients = {};
    if (data.content.length > 0) {
      let idList = '';
      for (let i = 0; i < data.content.length; i++) {
        if (i > 0) idList += ',';
        idList += data.content[i].productId;
      }
      newTopIngredients = await apiFetch(`/products/top-ingredients?ids=${idList}`);
    }
    const topIngredients = Object.assign({}, existingTopIngredients, newTopIngredients);

    const tbody = subsContent.querySelector('#subs-tbody');
    data.content.forEach(s => tbody.appendChild(buildSubRow(s, topIngredients[s.productId] || [])));

    if (subsPage + 1 >= subsTotalPages) moreBtn?.remove();
    else if (moreBtn) moreBtn.textContent = 'Load more substitutes';

    wireSubRows(subsContent);
  } catch (e) {
    if (moreBtn) moreBtn.textContent = 'Error — try again';
  }
}

function wireSubRows(container) {
  container.querySelectorAll('.sub-row[data-id]').forEach(row => {
    if (row.dataset.wired) return;
    row.dataset.wired = '1';
    row.addEventListener('click', () => goToProduct(parseInt(row.dataset.id, 10)));
  });
}

function renderProductHeader(p) {
  const imgHtml = p.imageUrl
    ? `<img src="${escHtml(p.imageUrl)}" alt="${escHtml(p.name)}"
            onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">`
    : '';
  const placeholder = `<div class="img-placeholder" ${p.imageUrl ? 'style="display:none"' : ''}>${escHtml(p.name.charAt(0).toUpperCase())}</div>`;
  const descHtml = p.description ? `<p class="description">${escHtml(p.description)}</p>` : '';
  const linkHtml = p.productPageUrl
    ? `<a href="${escHtml(p.productPageUrl)}" target="_blank" rel="noopener" class="product-link">View original product page ↗</a>`
    : '';

  return `
    <div class="product-header">
      <div class="product-img-wrap">${imgHtml}${placeholder}</div>
      <div class="product-info">
        <h1>${escHtml(p.name)}</h1>
        <div class="brand-name">${escHtml(p.brand?.name || '')}</div>
        <div class="meta-badges" id="header-badges">
          <span class="badge badge-subs" style="display:none" id="badge-subs"></span>
          <span class="badge badge-golden" style="display:none" id="badge-golden">★ Golden match available</span>
        </div>
        ${descHtml}
        ${linkHtml}
      </div>
    </div>
  `;
}

function updateHeaderBadges(counts, container) {
  let total = 0;
  for (const key in counts) {
    total += counts[key];
  }
  const badgeSubs = container.querySelector('#badge-subs');
  if (badgeSubs) {
    badgeSubs.textContent = '↔ ' + total.toLocaleString() + ' substitutes';
    badgeSubs.style.display = total > 0 ? '' : 'none';
  }
  const badgeGolden = container.querySelector('#badge-golden');
  if (badgeGolden) badgeGolden.style.display = 'none';
}

function renderIngredients(ingredients) {
  if (!ingredients.length) return '';
  let chips = '';
  for (let i = 0; i < ingredients.length; i++) {
    const ing = ingredients[i];
    chips += `<span class="chip" title="${escHtml(ing.functions || '')}"><span class="chip-pos">${ing.position}.</span>${escHtml(ing.inciName)}</span>`;
  }
  return `
    <div class="section-title">Ingredients (${ingredients.length})</div>
    <div class="ingredient-chips">${chips}</div>
  `;
}

function renderSubsTabs(counts, activeFilter) {
  let total = 0;
  for (const key in counts) total += counts[key];

  let tabsHtml = '';
  const levels = [5, 4, 3, 2, 1, 0];
  for (let i = 0; i < levels.length; i++) {
    const n = levels[i];
    const count = n === 0 ? total : (counts[n] || 0);
    const isActive = n === activeFilter ? ' active' : '';
    const isEmpty  = count === 0 ? ' empty' : '';
    const label    = n === 0 ? 'All' : n + '/5';
    tabsHtml += `<button class="subs-tab${isActive}${isEmpty}" data-filter="${n}">${label} <span class="tab-count">${count.toLocaleString()}</span></button>`;
  }
  return `
    <div class="section-title">Substitutes</div>
    <p class="subs-filter-hint">Filter products based on core ingredients (first 5 in formula)</p>
    <div class="subs-tabs">${tabsHtml}</div>
    <div id="subs-content"></div>
  `;
}

function renderSubsTable(subsData, topIngredients) {
  if (subsData.totalElements === 0) {
    return '<p style="font-size:0.875rem;color:#5f6368;padding:8px 0">No substitutes in this category.</p>';
  }
  let rows = '';
  for (let i = 0; i < subsData.content.length; i++) {
    const s = subsData.content[i];
    rows += buildSubRow(s, topIngredients[s.productId] || []).outerHTML;
  }
  const moreBtn = subsPage + 1 < subsData.totalPages
    ? `<div class="more-row"><button id="subs-more-btn">Load more substitutes</button></div>`
    : '';
  return `
    <table class="subs-table">
      <thead><tr><th></th><th>Product</th><th>Match</th><th>Top ingredients</th></tr></thead>
      <tbody id="subs-tbody">${rows}</tbody>
    </table>
    ${moreBtn}
  `;
}

function buildSubRow(s, subTopIngredients) {
  const tr = document.createElement('tr');
  tr.className = 'sub-row';
  tr.dataset.id = s.productId;

  const subTotal  = s.ingredientCount || 0;
  const pctSub    = subTotal > 0 ? Math.round(s.matchCount / subTotal * 100) : 0;
  const fillClass = s.isGoldenMatch ? 'golden' : '';

  const imgHtml = s.imageUrl
    ? `<img src="${escHtml(s.imageUrl)}" alt="${escHtml(s.productName)}"
            onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">`
    : '';
  const placeholder = `<div class="img-placeholder" ${s.imageUrl ? 'style="display:none"' : ''}>${escHtml((s.productName || '?').charAt(0).toUpperCase())}</div>`;

  const subIngByPos = {};
  if (subTopIngredients) {
    for (let i = 0; i < subTopIngredients.length; i++) {
      subIngByPos[subTopIngredients[i].position] = subTopIngredients[i];
    }
  }
  let chips = '';
  for (let pos = 1; pos <= 5; pos++) {
    const ing = subIngByPos[pos];
    if (!ing) {
      chips += `<span class="sub-ing-chip sub-ing-missing">${pos}. —</span>`;
    } else {
      const isMatch = currentProductTop5[pos] === ing.cosingId;
      chips += `<span class="sub-ing-chip${isMatch ? ' sub-ing-match' : ''}">${pos}. ${escHtml(ing.inciName)}</span>`;
    }
  }

  tr.innerHTML = `
    <td><div class="sub-thumb">${imgHtml}${placeholder}</div></td>
    <td>
      <div class="sub-product-name">${escHtml(s.productName)}</div>
      <div class="sub-brand">${escHtml(s.brandName || '')}</div>
    </td>
    <td>
      <div class="match-bars">
        <div class="match-row">
          <div class="match-bar"><div class="match-bar-fill ${fillClass}" style="width:${pctSub}%"></div></div>
          <span class="match-text">${s.matchCount}&thinsp;/&thinsp;${subTotal} <span class="match-pct">(${pctSub}%)</span></span>
        </div>
      </div>
    </td>
    <td><div class="sub-ing-list">${chips}</div></td>
  `;
  return tr;
}

function setStatus(el, html, cls) {
  el.innerHTML = html;
  el.className = 'status-line' + (cls ? ' ' + cls : '');
}

async function apiFetch(path, options = {}) {
  const key = getApiKey();
  const headers = { ...(options.headers || {}) };
  if (key) headers['X-Api-Key'] = key;
  if (options.body) headers['Content-Type'] = 'application/json';

  const res = await fetch(API + path, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || res.statusText);
  }
  if (res.status === 204) return null;
  return res.json();
}

// prevent XSS
function escHtml(s) {
  if (!s) return '';
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function updateAuthLinks() {
  const key = getApiKey();
  const html = key
    ? `<a href="#/account" class="auth-login-link">My account</a>`
    : `<a href="#/auth" class="auth-login-link">Login / Register</a>`;
  ['home-auth-link', 'results-auth-link', 'product-auth-link'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = html;
  });
}

function logout() {
  clearApiKey();
  location.hash = '#/';
}

let currentTab = 'register';

function switchTab(tab) {
  currentTab = tab;
  document.getElementById('tab-register').classList.toggle('active', tab === 'register');
  document.getElementById('tab-login').classList.toggle('active', tab === 'login');
  document.getElementById('auth-submit').textContent = tab === 'register' ? 'Register' : 'Login';
  document.getElementById('auth-password').placeholder = tab === 'register' ? 'Password (min 8 characters)' : 'Password';
  document.getElementById('auth-error').classList.add('hidden');
}

async function loadAccount() {
  const el = document.getElementById('account-content');
  el.innerHTML = '<p style="color:#5f6368;font-size:0.9rem">Loading…</p>';
  try {
    const data = await apiFetch('/auth/me');
    el.innerHTML = `
      <div class="account-row">
        <span class="account-label">Email</span>
        <span class="account-value">${escHtml(data.email)}</span>
      </div>
      <div class="account-row">
        <span class="account-label">API Key</span>
        <div class="api-key-box">
          <code>${escHtml(data.apiKey)}</code>
          <button class="copy-btn" onclick="copyKey('${escHtml(data.apiKey)}')">Copy</button>
        </div>
      </div>
      <div class="api-key-hint">
        <a href="/swagger-ui.html" target="_blank">View API documentation →</a>
      </div>
      <hr class="account-divider">
      <button class="logout-btn" onclick="logout()">Logout</button>
      <button class="delete-btn" onclick="confirmDelete()">Delete account</button>
      <p id="delete-error" class="auth-error hidden"></p>
    `;
  } catch (e) {
    el.innerHTML = `<p class="auth-error">${escHtml(e.message)}</p>`;
  }
}

function copyKey(key) {
  navigator.clipboard.writeText(key).then(() => {
    const btn = document.querySelector('.copy-btn');
    if (btn) { btn.textContent = 'Copied!'; setTimeout(() => btn.textContent = 'Copy', 1500); }
  });
}

async function confirmDelete() {
  if (!confirm('Delete your account? This cannot be undone.')) return;
  try {
    await apiFetch('/auth/account', { method: 'DELETE' });
    clearApiKey();
    location.hash = '#/';
  } catch (e) {
    const errEl = document.getElementById('delete-error');
    if (errEl) { errEl.textContent = e.message; errEl.classList.remove('hidden'); }
  }
}

async function submitAuth(e) {
  e.preventDefault();
  const email     = document.getElementById('auth-email').value.trim();
  const password  = document.getElementById('auth-password').value;
  const errorEl   = document.getElementById('auth-error');
  const submitBtn = document.getElementById('auth-submit');

  errorEl.classList.add('hidden');
  submitBtn.disabled = true;
  submitBtn.textContent = '…';

  try {
    const path = currentTab === 'register' ? '/auth/register' : '/auth/login';
    const data = await apiFetch(path, { method: 'POST', body: JSON.stringify({ email, password }) });
    setApiKey(data.apiKey);
    location.hash = '#/';
  } catch (err) {
    errorEl.textContent = err.message;
    errorEl.classList.remove('hidden');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = currentTab === 'register' ? 'Register' : 'Login';
  }
}
