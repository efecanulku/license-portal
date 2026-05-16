(function () {
    function setActiveNav() {
        var path = window.location.pathname;
        var best = null;
        var bestLen = -1;
        document.querySelectorAll('.portal-nav-link[data-match]').forEach(function (link) {
            var match = link.getAttribute('data-match');
            var exact = link.getAttribute('data-exact') === 'true';
            var active = exact ? path === match : path === match || path.startsWith(match + '/');
            if (active && match.length > bestLen) {
                best = link;
                bestLen = match.length;
            }
        });
        document.querySelectorAll('.portal-nav-link[data-match]').forEach(function (link) {
            link.classList.toggle('active', link === best);
        });
    }

    function initSidebar() {
        var toggle = document.getElementById('portalSidebarToggle');
        var sidebar = document.getElementById('portalSidebar');
        var backdrop = document.getElementById('portalSidebarBackdrop');
        if (!toggle || !sidebar) {
            return;
        }
        function close() {
            sidebar.classList.remove('show');
            if (backdrop) {
                backdrop.classList.remove('show');
            }
        }
        function open() {
            sidebar.classList.add('show');
            if (backdrop) {
                backdrop.classList.add('show');
            }
        }
        toggle.addEventListener('click', function () {
            if (sidebar.classList.contains('show')) {
                close();
            } else {
                open();
            }
        });
        if (backdrop) {
            backdrop.addEventListener('click', close);
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        setActiveNav();
        initSidebar();
    });
})();
