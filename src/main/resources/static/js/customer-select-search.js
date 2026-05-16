(function () {
    const picker = document.getElementById('customerPicker');
    const hidden = document.getElementById('customerId');
    const search = document.getElementById('customerSearch');
    const results = document.getElementById('customerSearchResults');
    const selectedLabel = document.getElementById('customerSelected');
    const countHint = document.getElementById('customerSearchCount');

    if (!picker || !hidden || !search || !results || !window.LICENSE_PORTAL_CUSTOMERS) {
        return;
    }

    const customers = window.LICENSE_PORTAL_CUSTOMERS;
    let activeIndex = -1;

    function normalize(value) {
        return (value || '').toString().toLowerCase().trim();
    }

    function formatLabel(customer) {
        if (customer.tax) {
            return customer.name + ' — ' + customer.tax;
        }
        return customer.name;
    }

    function findById(id) {
        return customers.find(function (c) {
            return String(c.id) === String(id);
        });
    }

    function filterCustomers(query) {
        const q = normalize(query);
        if (!q) {
            return customers.slice(0, 50);
        }
        return customers.filter(function (c) {
            return normalize(c.name).includes(q) || normalize(c.tax).includes(q);
        }).slice(0, 50);
    }

    function setSelected(customer) {
        if (!customer) {
            hidden.value = '';
            selectedLabel.textContent = '';
            selectedLabel.classList.add('d-none');
            return;
        }
        hidden.value = customer.id;
        search.value = customer.name;
        selectedLabel.textContent = 'Seçili: ' + formatLabel(customer);
        selectedLabel.classList.remove('d-none');
        results.classList.add('d-none');
        activeIndex = -1;
    }

    function renderResults(items) {
        results.innerHTML = '';
        if (items.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'list-group-item text-secondary small';
            empty.textContent = 'Eşleşen kurum yok.';
            results.appendChild(empty);
            results.classList.remove('d-none');
            if (countHint) {
                countHint.textContent = '0 kurum';
            }
            return;
        }

        items.forEach(function (customer, index) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'list-group-item list-group-item-action py-2';
            btn.textContent = formatLabel(customer);
            btn.addEventListener('click', function () {
                setSelected(customer);
            });
            btn.dataset.index = String(index);
            results.appendChild(btn);
        });

        results.classList.remove('d-none');
        if (countHint) {
            countHint.textContent = items.length + (items.length >= 50 ? '+' : '') + ' kurum';
        }
    }

    function syncActiveItem() {
        const buttons = results.querySelectorAll('button[data-index]');
        buttons.forEach(function (btn, i) {
            btn.classList.toggle('active', i === activeIndex);
        });
        if (activeIndex >= 0 && buttons[activeIndex]) {
            buttons[activeIndex].scrollIntoView({ block: 'nearest' });
        }
    }

    search.addEventListener('input', function () {
        hidden.value = '';
        selectedLabel.classList.add('d-none');
        const items = filterCustomers(search.value);
        activeIndex = items.length > 0 ? 0 : -1;
        renderResults(items);
        syncActiveItem();
    });

    search.addEventListener('focus', function () {
        const items = filterCustomers(search.value);
        activeIndex = items.length > 0 ? 0 : -1;
        renderResults(items);
        syncActiveItem();
    });

    search.addEventListener('keydown', function (event) {
        const buttons = results.querySelectorAll('button[data-index]');
        if (buttons.length === 0) {
            return;
        }
        if (event.key === 'ArrowDown') {
            event.preventDefault();
            activeIndex = Math.min(activeIndex + 1, buttons.length - 1);
            syncActiveItem();
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            activeIndex = Math.max(activeIndex - 1, 0);
            syncActiveItem();
        } else if (event.key === 'Enter') {
            if (activeIndex >= 0 && buttons[activeIndex]) {
                event.preventDefault();
                buttons[activeIndex].click();
            }
        } else if (event.key === 'Escape') {
            results.classList.add('d-none');
        }
    });

    document.addEventListener('click', function (event) {
        if (!picker.contains(event.target)) {
            results.classList.add('d-none');
        }
    });

    if (hidden.value) {
        const existing = findById(hidden.value);
        if (existing) {
            setSelected(existing);
        }
    }

    const form = picker.closest('form');
    if (form) {
        form.addEventListener('submit', function (event) {
            if (!hidden.value) {
                event.preventDefault();
                search.classList.add('is-invalid');
                search.focus();
                renderResults(filterCustomers(''));
            }
        });
    }
})();
