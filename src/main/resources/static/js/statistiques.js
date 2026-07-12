(function() {
    var data = window.STATS_DATA;
    var prodStats        = data.prod;
    var venteStats       = data.vente;
    var depenseStats     = data.depense;
    var medicamentStats  = data.medicament;
    var rentabiliteStats = data.rentabilite;

    var MOIS = ['Janvier','Février','Mars','Avril','Mai','Juin',
                'Juillet','Août','Septembre','Octobre','Novembre','Décembre'];

    function key(m, y)       { return y + '-' + String(m).padStart(2, '0'); }
    function fmtAxis(m, y)   { return new Date(y, m-1, 1).toLocaleString('fr-FR', {month:'short', year:'2-digit'}); }
    function fmtAr(v)        { return Number(v||0).toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' Ar'; }
    function fmtL(v)         { return Number(v||0).toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' L'; }

    function fmtYAxis(v) {
        var n = Number(v||0);
        if (Math.abs(n) >= 1e6) return (n/1e6).toLocaleString('fr-FR', {maximumFractionDigits:1}) + ' M';
        if (Math.abs(n) >= 1e3) return (n/1e3).toLocaleString('fr-FR', {maximumFractionDigits:1}) + ' k';
        if (Math.abs(n) >= 10)  return n.toLocaleString('fr-FR', {maximumFractionDigits:0});
        if (Math.abs(n) >= 1)   return n.toLocaleString('fr-FR', {minimumFractionDigits:1, maximumFractionDigits:1});
        return n.toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2});
    }

    function toggle(canvasId, emptyId, show) {
        var c = document.getElementById(canvasId);
        var e = document.getElementById(emptyId);
        if (c) c.style.display = show ? 'block' : 'none';
        if (e) e.style.display = show ? 'none'  : 'block';
    }

    function buildMap(arr, field) {
        var m = {};
        arr.forEach(function(s) { m[key(s.month, s.year)] = Number(s[field] || 0); });
        return m;
    }

    var prodMap   = buildMap(prodStats,   'total');
    var revMap    = buildMap(venteStats,  'total');
    var depMap    = buildMap(depenseStats,'total');
    var medMap    = buildMap(medicamentStats, 'total');
    var benMap    = buildMap(rentabiliteStats, 'benefice');

    var allPeriods = {};
    [prodStats, venteStats, depenseStats, medicamentStats, rentabiliteStats].forEach(function(arr) {
        arr.forEach(function(s) {
            var m = Number(s.month), y = Number(s.year);
            if (m && y) allPeriods[key(m, y)] = {month: m, year: y};
        });
    });

    var entries = Object.values(allPeriods).sort(function(a, b) {
        return a.year !== b.year ? a.year - b.year : a.month - b.month;
    });

    var yearsSet = {};
    entries.forEach(function(e) { yearsSet[e.year] = true; });
    var years = Object.keys(yearsSet).map(Number).sort();

    function fillMois(year) {
        var sel = document.getElementById('selectMois');
        var prev = sel.value ? Number(sel.value) : null;
        sel.innerHTML = '';

        var mois = entries
            .filter(function(e) { return e.year === year; })
            .map(function(e) { return e.month; })
            .sort(function(a, b) { return a - b; });

        if (!mois.length) for (var i = 1; i <= 12; i++) mois.push(i);

        mois.forEach(function(m) {
            var o = document.createElement('option');
            o.value = m;
            o.textContent = MOIS[m - 1];
            sel.appendChild(o);
        });

        if (prev && mois.includes(prev)) sel.value = prev;
    }

    function syncSelects(m, y) {
        var sa = document.getElementById('selectAnnee');
        var sm = document.getElementById('selectMois');
        if (sa.value != y) { sa.value = y; fillMois(Number(y)); }
        sm.value = m;
    }

    function showMonth(m, y) {
        var k      = key(m, y);
        var prod   = prodMap[k]  || 0;
        var rev    = revMap[k]   || 0;
        var dep    = depMap[k]   || 0;
        var med    = medMap[k]   || 0;
        var ben    = benMap[k] !== undefined ? benMap[k] : (rev - dep - med);

        document.getElementById('selectedProd').textContent   = fmtL(prod);
        document.getElementById('selectedRev').textContent    = fmtAr(rev);
        document.getElementById('selectedDep').textContent    = fmtAr(dep);
        document.getElementById('selectedDepMed').textContent = fmtAr(med);

        var benEl = document.getElementById('selectedBen');
        benEl.textContent = fmtAr(ben);
        benEl.style.color = ben >= 0 ? '#166534' : '#B91C1C';

        syncSelects(m, y);
    }

    function chartClick(statsArr) {
        return function(evt, els) {
            if (els && els.length) {
                var s = statsArr[els[0].index];
                if (s) showMonth(Number(s.month), Number(s.year));
            }
        };
    }

    var baseOpts = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'nearest', axis: 'x', intersect: false },
        plugins: {
            tooltip: { enabled: true, callbacks: { title: function(i) { return i.length ? 'Mois: '+i[0].label : ''; } } },
            legend:  { labels: { usePointStyle: true, boxWidth: 10 } }
        },
        scales: {
            x: { ticks: { autoSkip:true, maxTicksLimit:8, maxRotation:45, minRotation:0, padding:6, font:{size:11} }, grid:{display:false} },
            y: { beginAtZero:true, grace:'8%', ticks: { callback: fmtYAxis, font:{size:11} } }
        }
    };

    function bar(id, labels, datasets, src) {
        var el = document.getElementById(id);
        if (!el) return;
        new Chart(el.getContext('2d'), {
            type: 'bar',
            data: { labels: labels, datasets: datasets },
            options: Object.assign({}, baseOpts, { onClick: chartClick(src) })
        });
    }

    function line(id, labels, datasets, src) {
        var el = document.getElementById(id);
        if (!el) return;
        new Chart(el.getContext('2d'), {
            type: 'line',
            data: { labels: labels, datasets: datasets },
            options: Object.assign({}, baseOpts, { onClick: chartClick(src) })
        });
    }

    function labels(arr)  { return arr.map(function(s) { return fmtAxis(s.month, s.year); }); }
    function totals(arr)  { return arr.map(function(s) { return s.total; }); }

    document.addEventListener('DOMContentLoaded', function() {
        toggle('prodChart',           'prodChartEmpty',           prodStats.length > 0);
        toggle('venteChart',          'venteChartEmpty',          venteStats.length > 0);
        toggle('medicamentChart',     'medicamentChartEmpty',     medicamentStats.length > 0);
        toggle('rentabiliteChart',    'rentabiliteChartEmpty',    rentabiliteStats.length > 0);
        toggle('financeCompareChart', 'financeCompareChartEmpty', rentabiliteStats.length > 0);

        var selA = document.getElementById('selectAnnee');
        var selM = document.getElementById('selectMois');

        years.forEach(function(y) {
            var o = document.createElement('option');
            o.value = y; o.textContent = y;
            selA.appendChild(o);
        });

        var first = rentabiliteStats[0] || prodStats[0] || venteStats[0];
        if (first) {
            selA.value = Number(first.year);
            fillMois(Number(first.year));
            selM.value = Number(first.month);
            showMonth(Number(first.month), Number(first.year));
        } else if (years.length) {
            selA.value = years[years.length - 1];
            fillMois(years[years.length - 1]);
        }

        selA.addEventListener('change', function() {
            var y = Number(this.value);
            fillMois(y);
            var m = Number(selM.value);
            if (m && y) showMonth(m, y);
        });

        selM.addEventListener('change', function() {
            var m = Number(this.value);
            var y = Number(selA.value);
            if (m && y) showMonth(m, y);
        });

        bar('prodChart', labels(prodStats),
            [{ label:'Lait Produit (L)', data:totals(prodStats), backgroundColor:'#3B82F6', borderRadius:6, maxBarThickness:36 }],
            prodStats);

        bar('venteChart', labels(venteStats),
            [{ label:'Revenus (Ar)', data:totals(venteStats), backgroundColor:'#10B981', borderRadius:6, maxBarThickness:36 }],
            venteStats);

        bar('medicamentChart', labels(medicamentStats),
            [{ label:'Dép. médicaments (Ar)', data:totals(medicamentStats), backgroundColor:'#9333EA', borderRadius:6, maxBarThickness:36 }],
            medicamentStats);

        line('rentabiliteChart', labels(rentabiliteStats),
            [{ label:'Bénéfice (Ar)', data:rentabiliteStats.map(function(s){return s.benefice;}),
               borderColor:'#2563EB', backgroundColor:'rgba(37,99,235,0.15)',
               fill:true, tension:0.25, pointRadius:4, pointHoverRadius:7, pointHitRadius:22 }],
            rentabiliteStats);

        bar('financeCompareChart', labels(rentabiliteStats), [
            { label:'Revenus (Ar)',          data:rentabiliteStats.map(function(s){return s.revenus;}),              backgroundColor:'#10B981', borderRadius:6, maxBarThickness:32 },
            { label:'Dépenses totales (Ar)', data:rentabiliteStats.map(function(s){return Number(s.depensesTotales||0);}), backgroundColor:'#EF4444', borderRadius:6, maxBarThickness:32 }
        ], rentabiliteStats);
    });
})();
