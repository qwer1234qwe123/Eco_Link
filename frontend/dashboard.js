    // ───── 시계 ─────
    function updateClock() {
      const now = new Date();
      document.getElementById('clock').textContent =
        now.toLocaleTimeString('ko-KR', { hour12: false });
    }
    setInterval(updateClock, 1000);
    updateClock();

    // ───── 모의 데이터 (실제 서버 연동 시 fetch로 교체) ─────
    // 실제 연동 시: fetch('/api/trashcans').then(r => r.json()).then(renderTable)
    const MOCK_CANS = [
      { id: 'TC-001', name: '정문 앞 A', loc: '본관 정문 우측', fill: 92, battery: 87, overflow: true,  status: 'full' },
      { id: 'TC-002', name: '도서관 입구', loc: '도서관 1층 입구', fill: 75, battery: 92, overflow: false, status: 'warn' },
      { id: 'TC-003', name: '학생회관 1', loc: '학생회관 앞', fill: 45, battery: 78, overflow: false, status: 'ok' },
      { id: 'TC-004', name: '공학관 B동', loc: 'B동 1층 복도', fill: 88, battery: 65, overflow: false, status: 'full' },
      { id: 'TC-005', name: '운동장 북쪽', loc: '운동장 북문 옆', fill: 33, battery: 95, overflow: false, status: 'ok' },
      { id: 'TC-006', name: '기숙사 1동', loc: '기숙사 1동 출입구', fill: 61, battery: 82, overflow: false, status: 'warn' },
      { id: 'TC-007', name: '카페테리아', loc: '학생식당 앞', fill: 97, battery: 70, overflow: true,  status: 'full' },
      { id: 'TC-008', name: '정문 앞 B', loc: '본관 정문 좌측', fill: 22, battery: 91, overflow: false, status: 'ok' },
    ];

    const MOCK_PREDS = [
      { name: 'TC-007 카페테리아', detail: '현재 97% — 즉시 수거 필요', time: '지금', type: 'danger', icon: '🔴' },
      { name: 'TC-001 정문 앞 A', detail: '오후 2시경 포화 예상 (신뢰도 91%)', time: '14:00', type: 'danger', icon: '🔴' },
      { name: 'TC-004 공학관 B동', detail: '오후 4시경 포화 예상 (신뢰도 85%)', time: '16:00', type: 'warn', icon: '🟡' },
      { name: 'TC-002 도서관 입구', detail: '오후 5시 30분 예상 (신뢰도 76%)', time: '17:30', type: 'warn', icon: '🟡' },
      { name: 'TC-006 기숙사 1동', detail: '오늘 내 포화 미도달 예상', time: '안전', type: 'ok', icon: '🟢' },
    ];

    const MOCK_LOGS = [
      { can: 'TC-007', msg: '적재율 97% 감지 — 오버플로우 경고', time: '방금 전', color: '#ef4444' },
      { can: 'TC-001', msg: '적재율 92% 기록', time: '2분 전', color: '#ef4444' },
      { can: 'TC-004', msg: '적재율 88% 기록', time: '5분 전', color: '#f59e0b' },
      { can: 'TC-003', msg: '배터리 78% — 정상 범위', time: '8분 전', color: '#3ecf8e' },
      { can: 'TC-002', msg: '적재율 75% 기록', time: '11분 전', color: '#f59e0b' },
      { can: 'TC-005', msg: '수거 완료 처리됨 (before: 89%)', time: '23분 전', color: '#3b82f6' },
      { can: 'TC-008', msg: '적재율 22% — 여유 있음', time: '31분 전', color: '#3ecf8e' },
      { can: 'TC-006', msg: '연결 재확인 완료', time: '45분 전', color: '#7d8590' },
    ];

    // ───── 렌더링 함수 ─────
    function getFillColor(v) {
      if (v >= 80) return '#ef4444';
      if (v >= 60) return '#f59e0b';
      return '#2ea86a';
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
                <td><span class="can-id">${c.id}</span></td>
                <td>
                  <div class="can-name">${c.name}</div>
                  <div class="can-loc">${c.loc}</div>
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
                  ${c.overflow
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
          <div class="log-dot" style="background:${l.color}"></div>
          <div class="log-text">
            <strong style="font-size:11px;font-family:'JetBrains Mono',monospace">${l.can}</strong>
            &nbsp;${l.msg}
          </div>
          <div class="log-time">${l.time}</div>
        </div>
      `).join('');
    }

    function renderDistChart(cans) {
      const ok   = cans.filter(c => c.fill < 60).length;
      const warn = cans.filter(c => c.fill >= 60 && c.fill < 80).length;
      const full = cans.filter(c => c.fill >= 80).length;
      const total = cans.length;
      document.getElementById('dist-chart').innerHTML = `
        <div style="display:flex;flex-direction:column;gap:16px">
          ${[
            { label:'여유 (0–59%)',  val:ok,   color:'#2ea86a', pct: Math.round(ok/total*100) },
            { label:'주의 (60–79%)', val:warn, color:'#f59e0b', pct: Math.round(warn/total*100) },
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
              ${Math.round(cans.reduce((s,c)=>s+c.fill,0)/total)}%
            </span>
          </div>
        </div>
      `;
    }

    function updateStats(cans) {
      const full = cans.filter(c => c.fill >= 80).length;
      document.getElementById('stat-total').textContent = cans.length;
      document.getElementById('stat-full').textContent = full;
      document.getElementById('stat-pred').textContent = MOCK_PREDS.filter(p => p.type !== 'ok').length;
      document.getElementById('stat-done').textContent = '3';
    }

    // ───── 데이터 로드 (실제 API 연동 시 fetch 사용) ─────
    function loadData() {
      document.getElementById('last-updated').textContent = '업데이트 중...';

      // 실제 연동 예시:
      // fetch('/api/trashcans')
      //   .then(r => r.json())
      //   .then(data => { renderTable(data); updateStats(data); })
      //   .catch(err => console.error(err));

      // 현재는 모의 데이터 사용 (지연 시뮬레이션)
      setTimeout(() => {
        const randomized = MOCK_CANS.map(c => ({
          ...c,
          fill: Math.min(100, Math.max(0, c.fill + Math.floor(Math.random() * 5 - 2)))
        }));
        renderTable(randomized);
        renderPreds(MOCK_PREDS);
        renderLogs(MOCK_LOGS);
        renderDistChart(randomized);
        updateStats(randomized);
        const now = new Date();
        document.getElementById('last-updated').textContent =
          now.toLocaleTimeString('ko-KR') + ' 업데이트';
      }, 600);
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
