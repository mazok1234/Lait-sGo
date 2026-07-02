(function () {
  function qs(sel) {
    return document.querySelector(sel);
  }

  function setText(sel, text) {
    var el = qs(sel);
    if (el) el.textContent = text;
  }

  async function loadDashboard() {
    // Only run if the dashboard template is present
    var dashboardRoot = qs('.cheptel-header');
    // Heuristic: dashboard.html contains metric tiles; if not present, do nothing.
    var totalTileValue = qs('[th\:text]');
    // If the tiles haven’t been rendered by Thymeleaf, we’ll still see placeholders like .value inside dashboard.
    if (!qs('.grid-metrics')) return;

    try {
      // Fetch JSON provided by /api/cheptel/dashboard
      var res = await fetch('/api/cheptel/dashboard', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) throw new Error('API error ' + res.status);
      var data = await res.json();

      // dashboard.html currently expects different keys via thymeleaf. We’ll patch using common keys.
      // Total
      var totalVal = data.totalVaches;
      if (typeof totalVal !== 'undefined') {
        // Replace first metric tile value (Total vaches)
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 4) {
          tiles[0].textContent = String(totalVal);
        }
      }

      // Généalogie
      var genealogyVal = data.vachesAvecMere;
      if (typeof genealogyVal !== 'undefined') {
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 4) {
          tiles[3].textContent = String(genealogyVal);
        }
      }

      // Alertes non acquittées
      var alertesVal = data.alertesNonAcquittees;
      if (typeof alertesVal !== 'undefined') {
        var tiles = document.querySelectorAll('.grid-metrics .metric-tile .value');
        if (tiles && tiles.length >= 3) {
          tiles[2].textContent = String(alertesVal);
        }
      }

      // Statuts badges
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

            // Map code to class like dashboard fragment intended
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

      // If present, hide loading state
      var loading = qs('.empty-state');
      if (loading) loading.style.display = 'none';
    } catch (e) {
      console.error('Dashboard load failed:', e);
    }
  }

  // Run after DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadDashboard);
  } else {
    loadDashboard();
  }
})();

