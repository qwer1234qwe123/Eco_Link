// 부드러운 스크롤
document.querySelectorAll('a[href^="#"]').forEach(a => {
  a.addEventListener('click', e => {
    const href = a.getAttribute('href');
    if (href === '#') return;
    e.preventDefault();
    closeMobileMenu();
    document.querySelector(a.getAttribute('href'))?.scrollIntoView({ behavior: 'smooth' });
  });
});

// 햄버거 메뉴
const hamburger = document.getElementById('hamburger');
const mobileMenu = document.getElementById('mobileMenu');

function closeMobileMenu() {
  hamburger.classList.remove('open');
  mobileMenu.classList.remove('open');
}

hamburger.addEventListener('click', () => {
  hamburger.classList.toggle('open');
  mobileMenu.classList.toggle('open');
});

document.querySelectorAll('.mobile-link').forEach(a => {
  a.addEventListener('click', closeMobileMenu);
});

// 스크롤 등장 애니메이션
const observer = new IntersectionObserver(entries => {
  entries.forEach(e => {
    if (e.isIntersecting) {
      e.target.style.opacity = '1';
      e.target.style.transform = 'translateY(0)';
    }
  });
}, { threshold: 0.1 });

document.querySelectorAll('.intro-card, .team-card, .flow-step').forEach(el => {
  el.style.opacity = '0';
  el.style.transform = 'translateY(24px)';
  el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
  observer.observe(el);
});

// 대시보드 이동 (로그인 체크)
async function goToDashboard() {
  try {
    const res = await fetch('/api/auth/me');
    if (res.ok) {
      window.location.href = '/dashboard';
    } else {
      window.location.href = '/login';
    }
  } catch {
    window.location.href = '/login';
  }
}

// 실시간 통계 로드
async function loadStats() {
  try {
    // 1. 쓰레기통 개수
    const canRes = await fetch('/api/trashcan');
    const cans = await canRes.json();
    const total = cans.length;
    document.getElementById('stat-cans').textContent = total;

    // 2. 센서 데이터 있는 통 비율 (가동률)
    let activeCount = 0;
    await Promise.all(cans.map(async (can) => {
      try {
        const res = await fetch(`/api/sensor/${can.id}`);
        const logs = await res.json();
        if (logs.length > 0) activeCount++;
      } catch {}
    }));
    const rate = total > 0 ? Math.round((activeCount / total) * 100) : 0;
    document.getElementById('stat-active').textContent = rate + '%';

    // 3. 평균 적재율
    try {
      const fills = [];
      await Promise.all(cans.map(async (can) => {
        try {
          const res = await fetch(`/api/sensor/${can.id}`);
          const logs = await res.json();
          if (logs.length > 0) {
            const latest = logs[logs.length - 1];
            fills.push(latest.fillLevel);
          }
        } catch {}
      }));
      const avg = fills.length > 0
        ? Math.round(fills.reduce((a, b) => a + b, 0) / fills.length)
        : 0;
      document.getElementById('stat-avg').textContent = avg + '%';
    } catch {
      document.getElementById('stat-avg').textContent = '—';
    }

    // 4. 마지막 수신 시각
    document.getElementById('stat-time').textContent = '실시간';

  } catch (err) {
    console.warn('통계 로드 실패:', err);
    document.getElementById('stat-cans').textContent = '—';
    document.getElementById('stat-active').textContent = '—';
  }
}

// 페이지 로드 시 통계 불러오기
document.addEventListener('DOMContentLoaded', loadStats);