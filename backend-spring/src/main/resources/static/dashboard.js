// ───── 시계 ─────
function updateClock() {
  const now = new Date();
  document.getElementById('clock').textContent =
    now.toLocaleTimeString('ko-KR', { hour12: false });
}
setInterval(updateClock, 1000);
updateClock();

// ───── 렌더링 함수 ─────
function getFillColor(v) {
  if (v >= 80) return '#ef4444';
  if (v >= 60) return '#f59e0b';
  return '#2ea86a';
}

function getStatus(fill) {
  if (fill >= 80) return 'full';
  if (fill >= 60) return 'warn';
  return 'ok';
}

function renderTable(cans) {
  const html = `
    <table class="can-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>위치</th>
          <th>적재율</th>
          <th>배터리</th>
          <th>상태</th>
        </tr>
      </thead>
      <tbody>
        ${cans.map(c => `
          <tr>
            <td><span class="can-id">#${c.id}</span></td>
            <td>
              <div class="can-name">${c.locName}</div>
              <div class="can-loc">위도 ${c.locLat} / 경도 ${c.locLng}</div>
            </td>
            <td>
              <div class="fill-bar-wrap">
                <div class="fill-bar-bg">
                  <div class="fill-bar" style="width:${c.fill}%;background:${getFillColor(c.fill)}"></div>
                </div>
                <span class="fill-val" style="color:${getFillColor(c.fill)}">${c.fill}%</span>
              </div>
            </td>
            <td><span style="font-family:'JetBrains Mono',monospace;font-size:12px;color:var(--text-muted)">${c.battery}%</span></td>
            <td>
              ${c.fill >= 100
                ? '<span class="badge badge-full">오버플로우</span>'
                : c.status === 'full'
                  ? '<span class="badge badge-full">수거 필요</span>'
                  : c.status === 'warn'
                    ? '<span class="badge badge-warn">주의</span>'
                    : '<span class="badge badge-ok">정상</span>'
              }
            </td>
          </tr>
        `).join('')}
      </tbody>
    </table>`;
  document.getElementById('can-table-wrap').innerHTML = html;
}

function renderPreds(preds) {
  document.getElementById('pred-list').innerHTML = preds.map(p => `
    <div class="pred-item">
      <div class="pred-icon ${p.type === 'danger' ? 'pred-icon-danger' : p.type === 'warn' ? 'pred-icon-warn' : 'pred-icon-ok'}">${p.icon}</div>
      <div class="pred-info">
        <div class="pred-name">${p.name}</div>
        <div class="pred-detail">${p.detail}</div>
      </div>
      <div class="pred-time">${p.time}</div>
    </div>
  `).join('');
}

function renderLogs(logs) {
  document.getElementById('log-list').innerHTML = logs.map(l => `
    <div class="log-item">
      <div class="log-dot" style="background:${getFillColor(l.fillLevel)}"></div>
      <div class="log-text">
        <strong style="font-size:11px;font-family:'JetBrains Mono',monospace">#${l.canId}</strong>
        &nbsp;적재율 ${l.fillLevel}% / 배터리 ${l.batteryLevel}% 기록
      </div>
      <div class="log-time">${new Date(l.logTime).toLocaleTimeString('ko-KR')}</div>
    </div>
  `).join('');
}

function renderDistChart(cans) {
  const ok   = cans.filter(c => c.fill < 60).length;
  const warn = cans.filter(c => c.fill >= 60 && c.fill < 80).length;
  const full = cans.filter(c => c.fill >= 80).length;
  const total = cans.length || 1;
  document.getElementById('dist-chart').innerHTML = `
    <div style="display:flex;flex-direction:column;gap:16px">
      ${[
        { label:'여유 (0–59%)',      val:ok,   color:'#2ea86a', pct: Math.round(ok/total*100) },
        { label:'주의 (60–79%)',     val:warn, color:'#f59e0b', pct: Math.round(warn/total*100) },
        { label:'수거 필요 (80%+)', val:full, color:'#ef4444', pct: Math.round(full/total*100) },
      ].map(r => `
        <div>
          <div style="display:flex;justify-content:space-between;margin-bottom:6px">
            <span style="font-size:12px;color:var(--text-muted)">${r.label}</span>
            <span style="font-family:'JetBrains Mono',monospace;font-size:12px;color:${r.color}">${r.val}개 (${r.pct}%)</span>
          </div>
          <div style="height:10px;background:rgba(255,255,255,0.06);border-radius:100px;overflow:hidden">
            <div style="height:100%;width:${r.pct}%;background:${r.color};border-radius:100px;transition:width 0.5s ease"></div>
          </div>
        </div>
      `).join('')}
      <div style="margin-top:8px;padding-top:16px;border-top:1px solid var(--border);display:flex;justify-content:space-between">
        <span style="font-size:12px;color:var(--text-muted)">전체 평균 적재율</span>
        <span style="font-family:'JetBrains Mono',monospace;font-size:14px;font-weight:600;color:var(--text)">
          ${Math.round(cans.reduce((s,c) => s + c.fill, 0) / total)}%
        </span>
      </div>
    </div>
  `;
}

function updateStats(cans, predCount) {
  const full = cans.filter(c => c.fill >= 80).length;
  document.getElementById('stat-total').textContent = cans.length;
  document.getElementById('stat-full').textContent = full;
  document.getElementById('stat-pred').textContent = predCount;
  document.getElementById('stat-done').textContent = '—';
}

// ───── AI 예측 데이터 로드 ─────
async function loadPredictions(cansWithSensor) {
  try {
    // Spring Boot API 호출 → 내부에서 FastAPI 호출
    const predRes = await fetch('/bingo/api/prediction/all', { credentials: 'include' });

    if (!predRes.ok) throw new Error('예측 API 오류');

    const predData = await predRes.json();

    // 예측 결과 → 화면 렌더링용 포맷으로 변환
    const preds = predData.map(p => ({
      name: `#${p.canId} 쓰레기통`,
      detail: p.needsCollection
        ? `오늘 오후 6시 전 포화 예상 (신뢰도 ${Math.round(p.confidence * 100)}%)`
        : `정상 상태 (신뢰도 ${Math.round(p.confidence * 100)}%)`,
      time: p.needsCollection ? '수거 필요' : '안전',
      type: p.needsCollection ? 'danger' : 'ok',
      icon: p.needsCollection ? '🔴' : '🟢'
    }));

    // 수거 필요한 것만 필터링 (없으면 전체 정상 표시)
    const needCollection = preds.filter(p => p.type === 'danger');

    renderPreds(
      needCollection.length > 0
        ? needCollection
        : [{ name: '수거 필요 없음', detail: '모든 쓰레기통 양호', time: '안전', type: 'ok', icon: '🟢' }]
    );

    return needCollection.length;

  } catch (err) {
    // AI 서버 연동 전까지 적재율 기준으로 임시 표시
    console.warn('AI 예측 API 미연동 — 적재율 기준으로 표시:', err.message);

    const preds = cansWithSensor
      .filter(c => c.fill >= 60)
      .sort((a, b) => b.fill - a.fill)
      .map(c => ({
        name: `#${c.id} ${c.locName}`,
        detail: c.fill >= 80
          ? `현재 ${c.fill}% — 즉시 수거 필요`
          : `현재 ${c.fill}% — 오늘 내 포화 가능성 있음`,
        time: c.fill >= 80 ? '지금' : '예측 중',
        type: c.fill >= 80 ? 'danger' : 'warn',
        icon: c.fill >= 80 ? '🔴' : '🟡'
      }));

    renderPreds(
      preds.length > 0
        ? preds
        : [{ name: '수거 필요 없음', detail: '모든 쓰레기통 양호', time: '안전', type: 'ok', icon: '🟢' }]
    );

    return preds.filter(p => p.type === 'danger' || p.type === 'warn').length;
  }
}

// ───── 데이터 로드 ─────
async function loadData() {
  document.getElementById('last-updated').textContent = '업데이트 중...';

  try {
    // 1. 쓰레기통 목록 가져오기
    const canRes = await fetch('/bingo/api/trashcan', { credentials: 'include' });
    const canList = await canRes.json();

    // 2. 각 쓰레기통의 최신 센서값 가져오기
    const cansWithSensor = await Promise.all(
      canList.map(async (can) => {
        try {
          const sensorRes = await fetch(`/bingo/api/sensor/${can.id}`, { credentials: 'include' });
          const sensorLogs = await sensorRes.json();

          const sorted = [...sensorLogs].sort((a, b) => new Date(b.logTime) - new Date(a.logTime));
          const latest = sorted.length > 0 ? sorted[0] : null;  

          const fill    = latest ? latest.fillLevel    : 0;
          const battery = latest ? latest.batteryLevel : 0;

          return { ...can, fill, battery, status: getStatus(fill) };
        } catch {
          return { ...can, fill: 0, battery: 0, status: 'ok' };
        }
      })
    );

    // 3. 최근 센서 로그 전체 가져오기
    const logRes = await fetch('/bingo/api/sensor', { credentials: 'include' });
    const allLogs = await logRes.json();
    const recentLogs = [...allLogs]
      .sort((a, b) => new Date(b.logTime) - new Date(a.logTime))
      .slice(0, 8);

    // 4. AI 예측 API 호출 ← 여기서 FastAPI 연동
    const predCount = await loadPredictions(cansWithSensor);

    // 5. 화면 렌더링
    renderTable(cansWithSensor);
    renderLogs(recentLogs);
    renderDistChart(cansWithSensor);
    updateStats(cansWithSensor, predCount);

    const now = new Date();
    document.getElementById('last-updated').textContent =
      now.toLocaleTimeString('ko-KR') + ' 업데이트';

  } catch (err) {
    console.error('데이터 로드 실패:', err);
    document.getElementById('last-updated').textContent = '로드 실패 — 서버 확인 필요';
  }
}

// 초기 로드 + 30초 자동 갱신
loadData();
setInterval(loadData, 30000);

// ───── 모바일 햄버거 메뉴 ─────
const dashHamburger = document.getElementById('dashHamburger');
const sideNav = document.querySelector('nav.side-nav');

function initMobileNav() {
  if (window.innerWidth <= 768) {
    dashHamburger.style.display = 'flex';
  } else {
    dashHamburger.style.display = 'none';
    sideNav.classList.remove('open');
    dashHamburger.classList.remove('open');
  }
}

dashHamburger.addEventListener('click', () => {
  dashHamburger.classList.toggle('open');
  sideNav.classList.toggle('open');
});

document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', () => {
    sideNav.classList.remove('open');
    dashHamburger.classList.remove('open');
  });
});

initMobileNav();
window.addEventListener('resize', initMobileNav);