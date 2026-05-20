
    const PRODUCT_IMAGES = [
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBCxKZWFCjZvZLTKlO8f0OPOI0HinW9IwEL_ODSnGqBOz7DKasf5GAA_0Fu_2td7OBiSwnge5IkMmLKjHNvNGT58sZAvLN3JKFsLm6AR0x5zciTwyABjNwbFUOEbOLjdpUTzpUByRnS8bQKV03pPIafTadUbTTKAAq9KmTaBkiJYid8uQisxJrdj9y6-aXF8EHYTOIUgj8D4BI4hxfS0Ve9oRthOMoQ9PUTY9LJ0vRHSriFRD-n4eAD3r5pnlJoSQx37WP_DpkRvrE",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCpiwEdKuP4EARdR6CvXd3dAvDAp_PG38ubLAU60YtVNVuKUaO2BipzsnIZsYbZHbNaFKUCxb9XVrEJQyGDEdKt7uHObKMaE1t2zKeL59YYpBoUGoz0ANJym4kT-R7g81-IDI28L5F_oSq2OgkFsbMd-CvctZj0rG8X-_BwwHJkzqT8WAuRtGCdvZ3TqMBk34Ew_zGCqeyn20urLj29Ivs4kYuNkvj-BQ_j5vee5rStpBkDgeyLVI0-5dd4Jl_9r58Imaig_jI6Ujs",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDiaCJOAxxvuBWSxMetepB_jSHAwZVje-nReckV_V8TIdEPJx5XqcWeipExMZKKZr_l68OzP7PbJHVur_UFtdrX2MH1cvxEfh7HPev5nK_VMPchjrtKu9lORIUJilvkmMlSJ-uLwtzsqfGFnNzh3_RyMOMvOty7lN8ivJYVekSJ20vJdf24IIGMv0uX913eFKUtpU2UvsxE001eOqQSdduwuz1maj-W7A-QjQMQ3uJaDY9BOoUVfFOzQ3Zgn3SvGsrM2aFHA99ZHLE",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCKTwlB_oDaogoPyl98tmEjX0_dzuEf_ME6M3suO5wTBaVGJDiqE8Hg33rZUzUA66KunmGX4nblRORSLTmJU28C0X9D8vHAlABNlFgiPQvZlssEuO9SfJ4DBqTpU-Y-gXekMSn9V4C4tAaYfp_N7-UKrgdUZEvJ9YlTHl4dfSRhg3d-PanvsZ3H1BM0q-CmtUQk6guRmord1pUd4rB7dVWwyetjQQRSp45M7QO2t1wBHDVKBjLYLLvzB9Ci802X4Dl5-rIZ7Iu5NNk",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCovq7GiHG-yeFAhJJlk4hfLhzIvBDG1ur9TpQ9tf35VkCYam_RbtQsNnh63-H2SxmtzYV9tS-mnC48VEffF8OskGySNpORJoM-AVUjwJxR2DHYGdoikDOa6V-m2mo_xH9ef86vDXCOtgixbHCdCnnTSyUYAlkq_8HarC5063uhlkkh1uz4La0kqlCM_5SAQ5Rq-FE95rJnbjcRSlIqtdnvEuHiQGEokFlUGeNV4ZrRxQCAlinK7FIBT5m6dyvRKAsEKiIgfHfe_Ak",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDgAr52ePArJZgVvP5BEAdBjzVOV2j5iGK9YuCwBofxtv624Oh5d11MnsYhRO8LZttV_9qIv7TkqxTiSKbQaQZaMEAynt131yyC685YJn6qplxgXu0Y9w_Izehav_xZjLN3OjHDo52nJwAEJQ1f4riWwN9il4U-UFlOVR1o1-dFf_DEMdHxPkIVJTLP4r9UzoM1TshVqcqFbKAv5tOKqaJk-UemDrbgAS-TFGlyOV1EivwwKfxf4UELBugTxwZ78u9DkqQY2NqTVQ0"
    ];

    let currentSearch = '';
    let currentCategory = '';
    let isRxFilter = null; // null = all, true = rx, false = otc
    let currentPage = 0;
    let loadedProducts = [];
    let totalItems = 0;
    const PAGE_SIZE = 6;

    document.addEventListener("DOMContentLoaded", () => {
        const params = new URLSearchParams(window.location.search);
        
        // Use query param OR local storage for the search
        currentSearch = params.get('search') || localStorage.getItem('searchQuery') || '';
        
        // Clear local storage after reading
        localStorage.removeItem('searchQuery');
        
        // Remove the query string from the URL if it exists, without reloading the page
        if (window.history.replaceState && window.location.search) {
            window.history.replaceState({}, document.title, window.location.pathname);
        }

        document.getElementById('navSearchInput').value = currentSearch;
        
        loadCategories();
        fetchProducts(true);
        checkAuthStatus();

        // Search Input listener
        let debounceTimer;
        document.getElementById('navSearchInput').addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentSearch = e.target.value;
                fetchProducts(true);
            }, 300);
        });
        
        initCart();
    });

    // Handle Category Loading
    async function loadCategories() {
        try {
            const res = await fetch('/api/categories');
            const data = await res.json();
            const cats = data.content || data;
            const container = document.getElementById('categoryFilterList');
            cats.forEach(c => {
                const label = document.createElement('label');
                label.className = 'flex items-center gap-3 cursor-pointer group';
                label.innerHTML = `
                    <input type="radio" name="category" value="${c.name}" class="rounded-full border-outline-variant text-primary focus:ring-primary w-4 h-4"/>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-primary">${c.name}</span>
                `;
                label.querySelector('input').addEventListener('change', (e) => {
                    if (e.target.checked) {
                        currentCategory = e.target.value;
                        fetchProducts(true);
                    }
                });
                container.appendChild(label);
            });

            // Listen to the 'All Categories' radio
            container.querySelector('input[value=""]').addEventListener('change', (e) => {
                if(e.target.checked) {
                    currentCategory = '';
                    fetchProducts(true);
                }
            });
        } catch(e) { console.error("Error loading categories", e); }
    }

    // Handle Type Filtering
    function setRxFilter(val) {
        isRxFilter = val;
        // Update button styles
        document.getElementById('typeAllBtn').className = val === null ? 'text-left px-4 py-2 rounded-xl bg-primary text-white text-sm font-semibold transition-all' : 'text-left px-4 py-2 rounded-xl hover:bg-surface-container-high transition-all text-sm font-medium text-on-surface';
        document.getElementById('typeRxBtn').className = val === true ? 'text-left px-4 py-2 rounded-xl bg-primary text-white text-sm font-semibold transition-all' : 'text-left px-4 py-2 rounded-xl hover:bg-surface-container-high transition-all text-sm font-medium text-on-surface';
        document.getElementById('typeOtcBtn').className = val === false ? 'text-left px-4 py-2 rounded-xl bg-primary text-white text-sm font-semibold transition-all' : 'text-left px-4 py-2 rounded-xl hover:bg-surface-container-high transition-all text-sm font-medium text-on-surface';
        fetchProducts(true);
    }

    // Fetch API Data
    async function fetchProducts(reset = false) {
        if (reset) {
            currentPage = 0;
            loadedProducts = [];
            document.getElementById('productGrid').innerHTML = '';
        }

        try {
            let url = `/api/medicines/storefront?size=1000`; // Fetch max and client-side filter for now
            if (currentSearch) url += `&search=${encodeURIComponent(currentSearch)}`;
            
            const res = await fetch(url);
            const data = await res.json();
            let allMatches = data.content || [];

            // Apply client-side filters
            if (currentCategory) {
                allMatches = allMatches.filter(p => p.categoryName === currentCategory);
            }
            if (isRxFilter !== null) {
                allMatches = allMatches.filter(p => p.prescriptionRequired === isRxFilter);
            }

            totalItems = allMatches.length;

            // Update UI Header
            if (currentSearch) {
                document.getElementById('searchTitle').textContent = `Showing results for "${currentSearch}"`;
                document.getElementById('searchSubtitle').innerHTML = `We found <span class="font-bold text-primary">${totalItems}</span> clinical matches for your search. Please ensure you have a valid prescription for regulated medications.`;
            } else {
                document.getElementById('searchTitle').textContent = 'All Products';
                document.getElementById('searchSubtitle').innerHTML = `Browse our complete collection of <span class="font-bold text-primary">${totalItems}</span> clinical-grade medications.`;
            }

            // Client side pagination
            const start = currentPage * PAGE_SIZE;
            const pageData = allMatches.slice(start, start + PAGE_SIZE);
            
            if(reset && allMatches.length === 0) {
                document.getElementById('productGrid').innerHTML = `
                    <div class="col-span-full flex flex-col items-center justify-center py-24 bg-white rounded-3xl border border-surface-container">
                        <span class="material-symbols-outlined text-6xl text-outline-variant mb-4">search_off</span>
                        <h3 class="text-xl font-bold text-primary mb-2">No products found</h3>
                        <p class="text-on-surface-variant">Try adjusting your filters or search terms.</p>
                    </div>`;
                document.getElementById('loadMoreSection').classList.add('hidden');
                return;
            }

            pageData.forEach((p, idx) => {
                const globalIndex = start + idx;
                renderProductCard(p, globalIndex);
                loadedProducts.push(p);
            });

            updateLoadMoreUI();

        } catch (e) {
            console.error("Error fetching products", e);
        }
    }

    function renderProductCard(p, index) {
        const grid = document.getElementById('productGrid');
        let img = PRODUCT_IMAGES[index % PRODUCT_IMAGES.length];
        if (p.imageUrl) {
            img = `/uploads/${p.imageUrl}`;
        } else if (p.formType) {
            const formType = p.formType.toUpperCase();
            if (formType === 'CREAM') img = '/images/cream.png';
            else if (formType === 'SYRUP') img = '/images/Syrup_Liquid.png';
            else if (formType === 'TABLET') img = '/images/Tablet_Capsule_Pill.png';
            else img = '/images/other.png';
        } else {
            img = '/images/other.png';
        }
        
        const title = p.name;
        const price = p.finalPrice ? `Rs. ${Number(p.finalPrice).toFixed(2)}` : 'Rs. 0.00';
        
        let badgeHtml = '';
        if (p.prescriptionRequired) {
            badgeHtml = `<span class="bg-secondary-container text-on-secondary-container text-[10px] font-bold uppercase tracking-widest px-3 py-1 rounded-full shadow-sm">Prescription Required</span>`;
        } else {
            badgeHtml = `<span class="bg-surface-container-highest text-on-surface-variant text-[10px] font-bold uppercase tracking-widest px-3 py-1 rounded-full shadow-sm">Over-the-Counter</span>`;
        }

        const outOfStock = p.totalStock === 0;

        const card = document.createElement('div');
        card.className = 'flex flex-col group cursor-pointer';
        card.onclick = (e) => {
            // Prevent opening detail view if clicking add to cart
            if(e.target.closest('button')) return;
            showDetail(p, img);
        };
        
        card.innerHTML = `
            <div class="relative aspect-[4/5] bg-white rounded-[2rem] overflow-hidden mb-6 transition-all duration-500 group-hover:-translate-y-2 group-hover:shadow-xl border border-surface-container">
                <img class="w-full h-full object-cover p-4 mix-blend-multiply" src="${img}" alt="${title}"/>
                <div class="absolute top-6 left-6">${badgeHtml}</div>
                ${outOfStock ? '<div class="absolute inset-0 bg-white/60 backdrop-blur-sm flex items-center justify-center"><span class="bg-error text-white font-bold px-4 py-2 rounded-full text-sm">Out of Stock</span></div>' : ''}
            </div>
            <div class="px-2">
                <div class="flex justify-between items-start mb-2 gap-4">
                    <h3 class="font-headline font-bold text-xl text-primary leading-tight group-hover:text-secondary transition-colors">${title}</h3>
                    <span class="font-headline font-bold text-lg text-on-surface whitespace-nowrap">${price}</span>
                </div>
                <p class="text-outline text-sm font-medium mb-6 line-clamp-1">${p.categoryName || 'General'} • ${p.dosage || 'Standard'} • ${p.brand || 'Generic'}</p>
                <button onclick="addToCart(decodeURIComponent('${encodeURIComponent(JSON.stringify(p))}'), '${img}')" ${outOfStock ? 'disabled' : ''} class="w-full ${outOfStock ? 'bg-surface-dim text-outline cursor-not-allowed' : 'bg-surface-container-high text-on-primary-fixed-variant hover:bg-primary hover:text-white'} py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2 shadow-sm relative z-10">
                    <span class="material-symbols-outlined text-sm">add_shopping_cart</span>
                    Add to Cart
                </button>
            </div>
        `;
        grid.appendChild(card);
    }

    function updateLoadMoreUI() {
        const section = document.getElementById('loadMoreSection');
        if (totalItems <= loadedProducts.length) {
            section.classList.add('hidden');
        } else {
            section.classList.remove('hidden');
            document.getElementById('viewingStats').textContent = `Viewing ${loadedProducts.length} of ${totalItems} items`;
            const pct = (loadedProducts.length / totalItems) * 100;
            document.getElementById('loadProgress').style.width = `${pct}%`;
        }
    }

    function loadMore() {
        currentPage++;
        fetchProducts(false);
    }

    function showDetail(product, imgSrc) {
        document.getElementById('catalogView').classList.add('hidden');
        document.getElementById('detailView').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });

        const title = product.name;
        document.getElementById('breadCat').textContent = product.categoryName || 'Category';
        document.getElementById('breadProduct').textContent = title;
        document.getElementById('detailTitle').textContent = title;
        document.getElementById('detailCatLabel').textContent = product.categoryName || '';
        document.getElementById('detailBrand').textContent = `by ${product.brand || 'Generic'}`;
        document.getElementById('detailPrice').textContent = product.finalPrice ? `Rs. ${Number(product.finalPrice).toFixed(2)}` : 'Rs. 0.00';
        document.getElementById('detailImg').src = imgSrc;
        
        document.getElementById('detailCatInfo').textContent = product.categoryName || '-';
        document.getElementById('detailBrandInfo').textContent = product.brand || '-';
        document.getElementById('detailDosageInfo').textContent = product.dosage || '-';
        document.getElementById('detailExpiryInfo').textContent = product.earliestExpiry || '-';

        // Badge
        const badge = document.getElementById('detailBadge');
        if (product.prescriptionRequired) { 
            badge.textContent = 'Prescription Required'; 
            badge.className = 'absolute top-8 left-8 bg-secondary-container text-on-secondary-container px-4 py-1.5 rounded-full text-xs font-bold tracking-wide uppercase shadow-sm';
        } else { 
            badge.textContent = 'Over The Counter'; 
            badge.className = 'absolute top-8 left-8 bg-surface-container-highest text-on-surface-variant px-4 py-1.5 rounded-full text-xs font-bold tracking-wide uppercase shadow-sm';
        }

        // Stock
        const stockBadge = document.getElementById('detailStockBadge');
        const cartBtn = document.getElementById('addToCartBtn');
        if (product.totalStock === 0) { 
            stockBadge.innerHTML = '<span class="text-error text-sm font-bold bg-error-container px-3 py-1 rounded-lg">Out of Stock</span>'; 
            cartBtn.disabled = true; 
            cartBtn.className = 'w-full py-4 bg-surface-dim text-outline rounded-xl font-bold text-lg cursor-not-allowed flex items-center justify-center gap-2'; 
            cartBtn.onclick = null;
        } else { 
            stockBadge.innerHTML = `<span class="text-secondary text-sm font-bold flex items-center bg-secondary-container/30 px-3 py-1 rounded-lg"><span class="material-symbols-outlined text-sm mr-1">inventory_2</span>${product.totalStock} Available</span>`; 
            cartBtn.disabled = false; 
            cartBtn.className = 'w-full py-4 bg-gradient-to-br from-primary to-primary-container text-on-primary rounded-xl font-bold text-lg transition-transform hover:scale-[1.02] active:scale-95 flex items-center justify-center gap-2 shadow-md cursor-pointer'; 
            cartBtn.onclick = () => addToCart(encodeURIComponent(JSON.stringify(product)), imgSrc);
        }

        // Description
        document.getElementById('detailDescription').textContent = `${title} is manufactured by ${product.brand || 'our trusted generic partners'}. Formulated as ${product.dosage || 'standard dosage'}. Part of our ${product.categoryName || ''} catalog.`;
    }

    function showCatalog() {
        document.getElementById('detailView').classList.add('hidden');
        document.getElementById('catalogView').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    async function checkAuthStatus() {
        try {
            const res = await fetch('/api/auth/me');
            const data = await res.json();
            if (data.loggedIn) {
                const btn = document.getElementById('userAccountBtn');
                if (btn) btn.onclick = () => window.location.href = '/profile.html';
            }
        } catch(e) { console.error(e); }
    }

    // --- Cart Logic ---
    let cart = [];

    function initCart() {
        const stored = localStorage.getItem('medimart_cart');
        if (stored) {
            cart = JSON.parse(stored);
        }
        updateCartUI();
    }

    function saveCart() {
        localStorage.setItem('medimart_cart', JSON.stringify(cart));
        updateCartUI();
    }

    function toggleCartDrawer() {
        const overlay = document.getElementById('cartDrawerOverlay');
        if (overlay.classList.contains('hidden')) {
            overlay.classList.remove('hidden');
            overlay.classList.add('flex');
        } else {
            overlay.classList.add('hidden');
            overlay.classList.remove('flex');
        }
    }

    function addToCart(productStr, img) {
        const product = JSON.parse(decodeURIComponent(productStr));
        const existing = cart.find(item => item.id === product.id);
        if (existing) {
            existing.cartQty += 1;
        } else {
            cart.push({
                ...product,
                cartQty: 1,
                cartImg: img
            });
        }
        saveCart();
        toggleCartDrawer();
    }

    function updateCartQuantity(id, delta) {
        const item = cart.find(i => i.id === id);
        if (item) {
            item.cartQty += delta;
            if (item.cartQty <= 0) {
                cart = cart.filter(i => i.id !== id);
            }
            saveCart();
        }
    }

    function removeFromCart(id) {
        cart = cart.filter(i => i.id !== id);
        saveCart();
    }

    function updateCartUI() {
        const container = document.getElementById('cartItemsContainer');
        const badgeDesktop = document.getElementById('navCartBadgeDesktop');
        const badgeMobile = document.getElementById('navCartBadgeMobile');
        const countText = document.getElementById('cartItemCountText');
        
        let totalItems = 0;
        let subtotal = 0;
        let requiresRx = false;

        container.innerHTML = '';

        if (cart.length === 0) {
            container.innerHTML = `
                <div class="flex flex-col items-center justify-center h-full text-on-surface-variant opacity-60">
                    <span class="material-symbols-outlined text-6xl mb-4">shopping_cart</span>
                    <p>Your cart is empty</p>
                </div>
            `;
        }

        cart.forEach(item => {
            totalItems += item.cartQty;
            const itemPrice = item.finalPrice || 0;
            subtotal += itemPrice * item.cartQty;
            if (item.prescriptionRequired) requiresRx = true;

            const div = document.createElement('div');
            div.className = 'flex gap-4 p-4 bg-surface-container-lowest rounded-3xl mb-6 group transition-all duration-300 hover:shadow-lg hover:shadow-primary/5';
            div.innerHTML = `
                <div class="w-20 h-20 rounded-2xl bg-surface-container-low overflow-hidden flex-shrink-0 flex items-center justify-center">
                    <img class="w-16 h-auto mix-blend-multiply" src="${item.cartImg}" alt="${item.name}"/>
                </div>
                <div class="flex-1 flex flex-col justify-between">
                    <div class="flex justify-between">
                        <div>
                            <h3 class="font-bold text-on-surface leading-tight">${item.name}</h3>
                            <p class="text-xs text-on-surface-variant">${item.categoryName || 'General'} • ${item.brand || 'Generic'}</p>
                        </div>
                        <button onclick="removeFromCart(${item.id})" class="text-on-surface-variant hover:text-error transition-colors">
                            <span class="material-symbols-outlined text-[20px]">delete_outline</span>
                        </button>
                    </div>
                    <div class="flex justify-between items-end mt-2">
                        <div class="flex items-center bg-surface-container rounded-full px-2 py-1">
                            <button onclick="updateCartQuantity(${item.id}, -1)" class="w-6 h-6 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors"><span class="material-symbols-outlined text-[16px]">remove</span></button>
                            <span class="px-3 font-bold text-sm">${item.cartQty}</span>
                            <button onclick="updateCartQuantity(${item.id}, 1)" class="w-6 h-6 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors"><span class="material-symbols-outlined text-[16px]">add</span></button>
                        </div>
                        <span class="font-bold text-primary">Rs. ${(itemPrice * item.cartQty).toFixed(2)}</span>
                    </div>
                </div>
            `;
            container.appendChild(div);
        });

        // Badges
        if (totalItems > 0) {
            badgeDesktop.textContent = totalItems;
            if (badgeMobile) badgeMobile.textContent = totalItems;
            badgeDesktop.classList.remove('hidden');
            if (badgeMobile) badgeMobile.classList.remove('hidden');
        } else {
            badgeDesktop.classList.add('hidden');
            if (badgeMobile) badgeMobile.classList.add('hidden');
        }

        countText.textContent = `${totalItems} item${totalItems !== 1 ? 's' : ''}`;
        document.getElementById('cartSubtotal').textContent = `Rs. ${subtotal.toFixed(2)}`;
        document.getElementById('cartTotal').textContent = `Rs. ${subtotal.toFixed(2)}`;

        // RX Notice
        const rxNotice = document.getElementById('cartRxNotice');
        if (requiresRx) {
            rxNotice.classList.remove('hidden');
        } else {
            rxNotice.classList.add('hidden');
        }
    }
