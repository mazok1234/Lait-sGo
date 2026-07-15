(function () {
  function qs(sel) {
    return document.querySelector(sel);
  }

  function setText(sel, text) {
    var el = qs(sel);
    if (el) el.textContent = text;
  }

  async function loadDashboard() {
    var dashboardRoot = qs('.cheptel-header');
    var totalTileValue = qs('[th\:text]');
    if (!qs('.grid-metrics')) return;

    try {
      var res = await fetch('/api/cheptel/dashboard', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) throw new Error('API error ' + res.status);
      var data = await res.json();

      var totalVal = data.totalVaches;
      if (typeof totalVal !== 'undefined') {
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 4) {
          tiles[0].textContent = String(totalVal);
        }
      }

      var genealogyVal = data.vachesAvecMere;
      if (typeof genealogyVal !== 'undefined') {
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 4) {
          tiles[3].textContent = String(genealogyVal);
        }
      }

      var alertesVal = data.alertesNonAcquittees;
      if (typeof alertesVal !== 'undefined') {
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 3) {
          tiles[2].textContent = String(alertesVal);
        }
      }

      if (data.repartitionParStatut && typeof data.repartitionParStatut === 'object') {
        var badgesContainer = document.querySelector('.grid-metrics .metric-tile:nth-child(2) .stat-badge');
        var statTile = document.querySelectorAll('.grid-metrics .metric-tile')[1];
        if (statTile) {
          var existing = statTile.querySelector('.stat-badge');
          if (existing) existing.remove();

          var repartition = data.repartitionParStatut;
          var entries = Object.entries(repartition);
          entries.forEach(function (entry) {
            var code = entry[0];
            var nb = entry[1];

            var cls = '';
            if (code === 'en_lactation') cls = 'green';
            else if (code === 'reformee') cls = 'alert';

            var badge = document.createElement('span');
            badge.className = 'stat-badge ' + cls;
            badge.textContent = code + ' ' + nb;
            statTile.querySelector('.value')?.appendChild(badge);
          });
        }
      }

      var loading = qs('.empty-state');
      if (loading) loading.style.display = 'none';
    } catch (e) {
      console.error('Dashboard load failed:', e);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadDashboard);
  } else {
    loadDashboard();
  }
})();

