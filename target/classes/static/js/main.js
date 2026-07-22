/**
 * MelodyMart Client Scripting (SE2030 Project)
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check for success or error url triggers to pop toast notifications
    const params = new URLSearchParams(window.location.search);
    if (params.has('registered') && params.get('registered') === 'true') {
        showToast('Account registered successfully! Please log in.', 'success');
    }
    if (params.has('logout') && params.get('logout') === 'true') {
        showToast('Logged out successfully.', 'info');
    }
    if (params.has('saved') && params.get('saved') === 'true') {
        showToast('Product configuration saved successfully.', 'success');
    }
    if (params.has('deleted') && params.get('deleted') === 'true') {
        showToast('Product deleted from inventory.', 'info');
    }
    if (params.has('statusUpdated') && params.get('statusUpdated') === 'true') {
        showToast('Order status updated.', 'success');
    }
});

/**
 * Creates and displays a floating toast notification.
 */
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast';
    
    // Choose border color based on status type
    if (type === 'error') {
        toast.style.borderLeftColor = 'var(--color-danger)';
    } else if (type === 'warning') {
        toast.style.borderLeftColor = 'var(--color-warning)';
    } else if (type === 'info') {
        toast.style.borderLeftColor = 'var(--color-info)';
    }

    toast.innerHTML = `
        <div class="toast-content">${message}</div>
        <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
    `;

    container.appendChild(toast);

    // Auto fade out
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.5s ease';
        setTimeout(() => toast.remove(), 500);
    }, 4000);
}

/**
 * Add product to cart via AJAX.
 */
function addToCart(productId, quantity = 1) {
    const url = `/api/cart/add`;
    const data = new URLSearchParams();
    data.append('productId', productId);
    data.append('quantity', quantity);

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: data
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            // Update cart counter in header
            const cartCounter = document.getElementById('cart-count');
            if (cartCounter) {
                cartCounter.innerText = result.cartCount;
                cartCounter.style.display = 'inline';
            }
            showToast(result.message, 'success');
        } else {
            showToast(result.message, 'error');
        }
    })
    .catch(error => {
        console.error('Cart error:', error);
        showToast('Failed to add product to cart. Try logging in again.', 'error');
    });
}

/**
 * Updates cart quantity via AJAX.
 */
function updateCartQuantity(productId, inputElement) {
    const quantity = parseInt(inputElement.value);
    if (isNaN(quantity) || quantity <= 0) {
        inputElement.value = 1;
        return;
    }

    const data = new URLSearchParams();
    data.append('productId', productId);
    data.append('quantity', quantity);

    fetch(`/api/cart/update`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: data
    })
    .then(res => res.json())
    .then(result => {
        if (result.success) {
            // Update this item's subtotal label
            const subtotalLabel = document.getElementById(`subtotal-${productId}`);
            if (subtotalLabel) {
                subtotalLabel.innerText = '$' + result.itemSubtotal.toFixed(2);
            }

            // Update main cart totals
            updateCartSummary(result);
            showToast('Cart quantities synchronized.', 'info');
        } else {
            showToast(result.message, 'error');
            // Revert value
            location.reload();
        }
    })
    .catch(() => showToast('Failed to sync changes.', 'error'));
}

/**
 * Removes an item from cart via AJAX.
 */
function removeCartItem(productId, rowElement) {
    const data = new URLSearchParams();
    data.append('productId', productId);

    fetch(`/api/cart/remove`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: data
    })
    .then(res => res.json())
    .then(result => {
        if (result.success) {
            rowElement.style.opacity = '0';
            rowElement.style.transform = 'translateX(-20px)';
            rowElement.style.transition = 'all 0.4s ease';
            setTimeout(() => {
                rowElement.remove();
                
                // If cart is now empty, reload page to display empty cart state
                if (result.cartCount === 0) {
                    location.reload();
                } else {
                    updateCartSummary(result);
                }
            }, 4000);
            
            showToast('Item removed from cart.', 'info');
        }
    })
    .catch(() => showToast('Could not remove item.', 'error'));
}

function updateCartSummary(result) {
    const cartCounter = document.getElementById('cart-count');
    if (cartCounter) {
        cartCounter.innerText = result.cartCount;
        if (result.cartCount === 0) {
            cartCounter.style.display = 'none';
        }
    }

    const subtotalText = document.getElementById('cart-subtotal');
    if (subtotalText) subtotalText.innerText = '$' + result.subtotal.toFixed(2);

    const taxText = document.getElementById('cart-tax');
    if (taxText) taxText.innerText = '$' + result.tax.toFixed(2);

    const totalText = document.getElementById('cart-total');
    if (totalText) totalText.innerText = '$' + result.total.toFixed(2);
}

// --- Admin Specifications Panel Builders ---

function addSpecRow() {
    const container = document.getElementById('specifications-container');
    if (!container) return;

    const row = document.createElement('div');
    row.className = 'spec-builder-row';
    row.innerHTML = `
        <input type="text" name="specKeys" placeholder="e.g. Weight" class="form-control" required />
        <input type="text" name="specValues" placeholder="e.g. 3.5 kg" class="form-control" required />
        <button type="button" class="btn btn-secondary" onclick="this.parentElement.remove()" style="padding: 0.5rem 1rem;">&times;</button>
    `;
    container.appendChild(row);
}

// --- Profile Page Tab Control ---
function switchProfileTab(tabName) {
    // Deactivate all tabs
    document.querySelectorAll('.profile-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    // Activate specific elements
    const activeTab = document.querySelector(`button[onclick*="'${tabName}'"]`);
    if (activeTab) activeTab.classList.add('active');

    const activeContent = document.getElementById(`tab-${tabName}`);
    if (activeContent) activeContent.classList.add('active');
}

// --- Layout Sidebar Toggle ---
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;
    
    if (window.innerWidth <= 900) {
        // Mobile: Sidebar is hidden by default. Toggle opens it.
        sidebar.classList.toggle('mobile-active');
    } else {
        // Desktop: Sidebar is visible by default. Toggle hides it.
        sidebar.classList.toggle('desktop-hidden');
        document.body.classList.toggle('desktop-fullwidth');
    }
}
