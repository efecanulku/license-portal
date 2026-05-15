(function () {
    function initLicenseCopy() {
        var keyEl = document.getElementById('licenseKey');
        var btn = document.getElementById('copyLicenseBtn');
        var feedback = document.getElementById('copyFeedback');
        if (!keyEl || !btn) {
            return;
        }

        var defaultLabel = btn.textContent.trim() || 'Kopyala';

        function setFeedback(message, isError) {
            if (!feedback) {
                return;
            }
            feedback.textContent = message;
            feedback.classList.remove('d-none', 'text-success', 'text-danger');
            feedback.classList.add(isError ? 'text-danger' : 'text-success');
            setTimeout(function () {
                feedback.classList.add('d-none');
            }, 2500);
        }

        function showCopied() {
            var wasPrimary = btn.classList.contains('btn-primary');
            btn.textContent = 'Kopyalandı!';
            btn.classList.remove('btn-primary', 'btn-outline-primary');
            btn.classList.add('btn-success');
            setFeedback('Panoya kopyalandı', false);
            setTimeout(function () {
                btn.textContent = defaultLabel;
                btn.classList.remove('btn-success');
                btn.classList.add(wasPrimary ? 'btn-primary' : 'btn-outline-primary');
            }, 2000);
        }

        function fallbackCopy(text) {
            var ta = document.createElement('textarea');
            ta.value = text;
            ta.setAttribute('readonly', '');
            ta.style.position = 'fixed';
            ta.style.left = '-9999px';
            document.body.appendChild(ta);
            ta.select();
            var ok = false;
            try {
                ok = document.execCommand('copy');
            } catch (err) {
                ok = false;
            }
            document.body.removeChild(ta);
            return ok;
        }

        btn.addEventListener('click', function () {
            var text = (keyEl.textContent || '').trim();
            if (!text) {
                setFeedback('Kopyalanacak anahtar yok', true);
                return;
            }

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text).then(showCopied).catch(function () {
                    if (fallbackCopy(text)) {
                        showCopied();
                    } else {
                        setFeedback('Kopyalama başarısız — anahtarı seçip Ctrl+C kullanın', true);
                    }
                });
                return;
            }

            if (fallbackCopy(text)) {
                showCopied();
            } else {
                setFeedback('Kopyalama başarısız — anahtarı seçip Ctrl+C kullanın', true);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initLicenseCopy);
    } else {
        initLicenseCopy();
    }
})();
