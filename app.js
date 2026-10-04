// Nearly V1 - Application Engine with Google Maps Integration & Mpumalanga Hubs
// Base Province: Mpumalanga, South Africa

document.addEventListener("DOMContentLoaded", () => {
  // ==========================================
  // STATE MANAGEMENT & LOCAL STORAGE
  // ==========================================
  const STORAGE_KEYS = {
    SAVED_PLACES: "nearly_saved_places",
    SEARCH_HISTORY: "nearly_search_history",
    USER_NAME: "nearly_user_name",
    USER_AREA: "nearly_user_area",
    THEME: "nearly_theme_mode"
  };

  const state = {
    activeTab: "homeTab",
    // Mpumalanga is the default base province
    selectedArea: localStorage.getItem(STORAGE_KEYS.USER_AREA) || "Mbombela (Nelspruit), Mpumalanga",
    userName: localStorage.getItem(STORAGE_KEYS.USER_NAME) || "Karabo Shumba",
    themeMode: localStorage.getItem(STORAGE_KEYS.THEME) || "system",
    savedPlaceIds: new Set(JSON.parse(localStorage.getItem(STORAGE_KEYS.SAVED_PLACES) || "[]")),
    searchHistory: JSON.parse(localStorage.getItem(STORAGE_KEYS.SEARCH_HISTORY) || '["phone charger", "print documents", "fix my phone", "school supplies"]'),
    activeCategoryFilter: null,
    searchQuery: "",
    maxDistanceKm: null,
    openNowOnly: false,
    sortOption: "relevance",
    isMapView: false,
    selectedPlace: null,
    directionsPlace: null
  };

  // ==========================================
  // DOM ELEMENT REFERENCES
  // ==========================================
  const currentAreaText = document.getElementById("currentAreaText");
  const areaSelectorBtn = document.getElementById("areaSelectorBtn");
  const areaModal = document.getElementById("areaModal");
  const closeAreaModal = document.getElementById("closeAreaModal");
  const areaOptionsList = document.getElementById("areaOptionsList");

  const themeToggleBtn = document.getElementById("themeToggleBtn");
  const themeSegmented = document.getElementById("themeSegmented");

  const navItems = document.querySelectorAll(".nav-item");
  const tabPanes = document.querySelectorAll(".tab-pane");

  const heroSearchTrigger = document.getElementById("heroSearchTrigger");
  const searchInput = document.getElementById("searchInput");
  const clearSearchBtn = document.getElementById("clearSearchBtn");
  const filterToggleBtn = document.getElementById("filterToggleBtn");
  const filterDrawer = document.getElementById("filterDrawer");
  const resetFiltersBtn = document.getElementById("resetFiltersBtn");
  const applyFiltersBtn = document.getElementById("applyFiltersBtn");
  const quickClearFiltersBtn = document.getElementById("quickClearFiltersBtn");
  const emptyStateResetBtn = document.getElementById("emptyStateResetBtn");

  const homeCategoryGrid = document.getElementById("homeCategoryGrid");
  const searchCategoryChips = document.getElementById("searchCategoryChips");
  const viewAllCategoriesBtn = document.getElementById("viewAllCategoriesBtn");
  const distanceFilterRow = document.getElementById("distanceFilterRow");
  const openNowSwitch = document.getElementById("openNowSwitch");
  const sortSelect = document.getElementById("sortSelect");

  const searchResultsList = document.getElementById("searchResultsList");
  const searchMapContainer = document.getElementById("searchMapContainer");
  const toggleMapViewBtn = document.getElementById("toggleMapViewBtn");
  const resultsCountText = document.getElementById("resultsCountText");
  const searchEmptyState = document.getElementById("searchEmptyState");

  const recommendationsList = document.getElementById("recommendationsList");
  const recommendationSubtitle = document.getElementById("recommendationSubtitle");
  const homeSavedBanner = document.getElementById("homeSavedBanner");
  const savedBannerTitle = document.getElementById("savedBannerTitle");
  const homeRecentSearches = document.getElementById("homeRecentSearches");
  const recentSearchesRow = document.getElementById("recentSearchesRow");

  const savedPlacesList = document.getElementById("savedPlacesList");
  const savedCategoryFilterRow = document.getElementById("savedCategoryFilterRow");
  const savedEmptyState = document.getElementById("savedEmptyState");
  const savedCountBadge = document.getElementById("savedCountBadge");
  const navSavedBadge = document.getElementById("navSavedBadge");
  const savedExploreBtn = document.getElementById("savedExploreBtn");

  const profileNameDisplay = document.getElementById("profileNameDisplay");
  const profileAreaText = document.getElementById("profileAreaText");
  const profileInitial = document.getElementById("profileInitial");
  const statSavedCount = document.getElementById("statSavedCount");
  const statSearchCount = document.getElementById("statSearchCount");
  const profileSearchHistoryList = document.getElementById("profileSearchHistoryList");
  const clearAllHistoryBtn = document.getElementById("clearAllHistoryBtn");
  const editNameBtn = document.getElementById("editNameBtn");
  const profileAreaChangeBtn = document.getElementById("profileAreaChangeBtn");

  const placeDetailModal = document.getElementById("placeDetailModal");
  const closeDetailModal = document.getElementById("closeDetailModal");
  const detailContent = document.getElementById("detailContent");
  const detailCategoryBadge = document.getElementById("detailCategoryBadge");
  const detailShareBtn = document.getElementById("detailShareBtn");
  const detailSaveBtn = document.getElementById("detailSaveBtn");

  const directionsModal = document.getElementById("directionsModal");
  const closeDirectionsModal = document.getElementById("closeDirectionsModal");
  const directionsContent = document.getElementById("directionsContent");

  const editNameModal = document.getElementById("editNameModal");
  const closeNameModal = document.getElementById("closeNameModal");
  const nameInput = document.getElementById("nameInput");
  const saveNameBtn = document.getElementById("saveNameBtn");

  const aboutModal = document.getElementById("aboutModal");
  const closeAboutModal = document.getElementById("closeAboutModal");
  const closeAboutBtn = document.getElementById("closeAboutBtn");
  const aboutBtn = document.getElementById("aboutBtn");

  // ==========================================
  // DISTANCE & NLP SEARCH CALCULATION
  // ==========================================
  function calculateDistance(place, currentArea) {
    const userCoords = CITY_COORDINATES[currentArea] || CITY_COORDINATES["Mbombela (Nelspruit), Mpumalanga"];
    if (userCoords && place.latitude && place.longitude) {
      const R = 6371; // Earth radius in km
      const dLat = (place.latitude - userCoords.lat) * (Math.PI / 180);
      const dLon = (place.longitude - userCoords.lng) * (Math.PI / 180);
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(userCoords.lat * (Math.PI / 180)) * Math.cos(place.latitude * (Math.PI / 180)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return Math.round(R * c * 10) / 10;
    }
    return place.area.toLowerCase() === currentArea.toLowerCase() ? place.baseDistance : place.baseDistance + 24.5;
  }

  function searchPlaces() {
    const rawQuery = state.searchQuery.trim().toLowerCase();
    const stopWords = new Set(["i", "need", "where", "can", "to", "a", "an", "the", "for", "my", "me", "nearby", "closest", "around", "in", "is", "there", "someone", "buy", "find"]);
    const tokens = rawQuery
      .split(/[^a-zA-Z0-9]+/)
      .filter(t => t.length > 0 && !stopWords.has(t));

    const matched = [];

    PLACES_DATA.forEach(place => {
      const distance = calculateDistance(place, state.selectedArea);

      // Category filter check
      if (state.activeCategoryFilter && place.category.toLowerCase() !== state.activeCategoryFilter.toLowerCase()) {
        return;
      }

      // Max distance check
      if (state.maxDistanceKm !== null && distance > state.maxDistanceKm) {
        return;
      }

      // Open now check
      if (state.openNowOnly && !place.isOpen) {
        return;
      }

      // If no search query, match with default relevance
      if (!rawQuery) {
        matched.push({ place, distance, score: 1.0 / (distance + 0.1) });
        return;
      }

      let score = 0;
      const nameLower = place.name.toLowerCase();
      const descLower = place.description.toLowerCase();
      const catLower = place.category.toLowerCase();

      // Direct matches
      if (nameLower.includes(rawQuery)) score += 50;
      if (descLower.includes(rawQuery)) score += 20;
      if (catLower.includes(rawQuery)) score += 30;

      // Smart Intent Mapping
      if (rawQuery.includes("charger") || rawQuery.includes("cable") || rawQuery.includes("type-c")) {
        if (catLower === "electronics") score += 40;
      }
      if (rawQuery.includes("print") || rawQuery.includes("document") || rawQuery.includes("copy") || rawQuery.includes("scan")) {
        if (catLower === "printing") score += 40;
      }
      if (rawQuery.includes("fix") || rawQuery.includes("repair") || rawQuery.includes("screen")) {
        if (catLower === "repairs") score += 40;
      }
      if (rawQuery.includes("haircut") || rawQuery.includes("barber") || rawQuery.includes("fade") || rawQuery.includes("beard")) {
        if (catLower === "beauty") score += 40;
      }
      if (rawQuery.includes("school") || rawQuery.includes("supplies") || rawQuery.includes("textbook") || rawQuery.includes("stationery")) {
        if (catLower === "education" || catLower === "shopping") score += 40;
      }
      if (rawQuery.includes("groceries") || rawQuery.includes("food") || rawQuery.includes("bread") || rawQuery.includes("coffee")) {
        if (catLower === "food") score += 40;
      }
      if (rawQuery.includes("locksmith") || rawQuery.includes("plumber") || rawQuery.includes("geyser") || rawQuery.includes("burst pipe")) {
        if (catLower === "services") score += 40;
      }

      // Tag matching
      place.tags.forEach(tag => {
        const tagLower = tag.toLowerCase();
        if (tagLower === rawQuery) score += 30;
        else if (tagLower.includes(rawQuery) || rawQuery.includes(tagLower)) score += 15;
      });

      // Service matching
      place.services.forEach(srv => {
        const srvLower = srv.toLowerCase();
        if (srvLower.includes(rawQuery) || rawQuery.includes(srvLower)) score += 15;
      });

      // Token matching
      tokens.forEach(token => {
        if (token.length >= 3) {
          if (nameLower.includes(token)) score += 12;
          if (descLower.includes(token)) score += 6;
          if (catLower.includes(token)) score += 10;
          if (place.tags.some(t => t.includes(token))) score += 8;
        }
      });

      if (score > 0) {
        score += 10.0 / (distance + 0.2);
        matched.push({ place, distance, score });
      }
    });

    // Sorting
    if (state.sortOption === "nearest") {
      matched.sort((a, b) => a.distance - b.distance);
    } else if (state.sortOption === "rating") {
      matched.sort((a, b) => b.place.rating - a.place.rating);
    } else {
      matched.sort((a, b) => b.score - a.score);
    }

    return matched.map(m => m.place);
  }

  // ==========================================
  // CARD GENERATOR
  // ==========================================
  function createPlaceCardHTML(place) {
    const isSaved = state.savedPlaceIds.has(place.id);
    const distance = calculateDistance(place, state.selectedArea);
    const catObj = CATEGORIES.find(c => c.name.toLowerCase() === place.category.toLowerCase()) || { color: "#64748B", icon: "th-large" };

    return `
      <div class="place-card" data-id="${place.id}">
        <div class="card-top">
          <span class="badge-category" style="background-color: ${catObj.color}15; color: ${catObj.color}">
            <i class="fa-solid fa-${catObj.icon}"></i> ${place.category}
          </span>
          <button class="save-btn ${isSaved ? "active" : ""}" data-save-id="${place.id}" aria-label="Save">
            <i class="${isSaved ? "fa-solid" : "fa-regular"} fa-bookmark"></i>
          </button>
        </div>
        <div class="card-title-row">
          <h4>${place.name}</h4>
          <span class="rating-badge"><i class="fa-solid fa-star"></i> ${place.rating}</span>
        </div>
        <p class="card-desc">${place.description}</p>
        <div class="card-meta-row">
          <span class="dist-pill"><i class="fa-solid fa-location-arrow"></i> ${distance.toFixed(1)} km</span>
          <span class="status-dot-wrap ${place.isOpen ? "open" : "closed"}">
            <span class="status-dot"></span>
            ${place.isOpen ? "Open Now" : "Closed"}
          </span>
          <span style="color: var(--text-muted); margin-left: auto;">${place.area.split(",")[0]}</span>
        </div>
        <div class="card-address">
          <i class="fa-solid fa-location-dot"></i> ${place.address}
        </div>
        <div class="card-actions">
          <button class="btn-card-action primary" onclick="event.stopPropagation(); window.location.href='tel:${place.phone.replace(/\s+/g, '')}'">
            <i class="fa-solid fa-phone"></i> Call
          </button>
          <button class="btn-card-action gmaps-action" data-directions-id="${place.id}">
            <i class="fa-solid fa-diamond-turn-right"></i> Directions
          </button>
        </div>
      </div>
    `;
  }

  // ==========================================
  // RENDER VIEWS
  // ==========================================
  function renderAll() {
    renderHeader();
    renderHomeTab();
    renderSearchTab();
    renderSavedTab();
    renderProfileTab();
  }

  function renderHeader() {
    currentAreaText.textContent = state.selectedArea.split(",")[0];
    profileAreaText.textContent = state.selectedArea;
    profileNameDisplay.textContent = state.userName;
    profileInitial.textContent = (state.userName[0] || "U").toUpperCase();

    const isDark = document.body.classList.contains("dark-mode");
    themeToggleBtn.innerHTML = isDark ? '<i class="fa-solid fa-sun"></i>' : '<i class="fa-solid fa-moon"></i>';
  }

  function renderHomeTab() {
    // Categories Grid
    homeCategoryGrid.innerHTML = CATEGORIES.map(cat => `
      <div class="category-tile" data-cat="${cat.name}">
        <div class="cat-icon-wrap" style="background-color: ${cat.color}15; color: ${cat.color}">
          <i class="fa-solid fa-${cat.icon}"></i>
        </div>
        <span>${cat.name}</span>
      </div>
    `).join("");

    // Saved banner
    const savedCount = state.savedPlaceIds.size;
    if (savedCount > 0) {
      homeSavedBanner.classList.remove("hidden");
      savedBannerTitle.textContent = `${savedCount} Place${savedCount > 1 ? "s" : ""} Saved`;
    } else {
      homeSavedBanner.classList.add("hidden");
    }

    // Recent searches
    if (state.searchHistory.length > 0) {
      homeRecentSearches.classList.remove("hidden");
      recentSearchesRow.innerHTML = state.searchHistory.slice(0, 6).map(q => `
        <button class="prompt-chip recent-search-chip" data-query="${q}">${q}</button>
      `).join("");
    } else {
      homeRecentSearches.classList.add("hidden");
    }

    // Recommendations (Open now & sorted by distance)
    recommendationSubtitle.textContent = `Open right now around ${state.selectedArea.split(",")[0]}`;
    const recommendations = PLACES_DATA
      .filter(p => p.isOpen)
      .sort((a, b) => calculateDistance(a, state.selectedArea) - calculateDistance(b, state.selectedArea))
      .slice(0, 5);

    recommendationsList.innerHTML = recommendations.map(createPlaceCardHTML).join("");
  }

  function renderSearchTab() {
    // Category chips in search
    searchCategoryChips.innerHTML = `
      <button class="category-chip ${state.activeCategoryFilter === null ? "active" : ""}" data-cat="all">All</button>
    ` + CATEGORIES.map(cat => `
      <button class="category-chip ${state.activeCategoryFilter === cat.name ? "active" : ""}" data-cat="${cat.name}">
        <i class="fa-solid fa-${cat.icon}"></i> ${cat.name}
      </button>
    `).join("");

    const results = searchPlaces();
    
    // Toggle Map vs List
    if (state.isMapView) {
      searchResultsList.classList.add("hidden");
      searchMapContainer.classList.remove("hidden");
      renderMapView(results);
    } else {
      searchResultsList.classList.remove("hidden");
      searchMapContainer.classList.add("hidden");
    }

    if (results.length === 0) {
      searchResultsList.innerHTML = "";
      searchEmptyState.classList.remove("hidden");
      resultsCountText.textContent = "0 places found";
    } else {
      searchEmptyState.classList.add("hidden");
      searchResultsList.innerHTML = results.map(createPlaceCardHTML).join("");
      if (state.searchQuery) {
        resultsCountText.textContent = `Found ${results.length} place${results.length > 1 ? "s" : ""} for "${state.searchQuery}"`;
      } else if (state.activeCategoryFilter) {
        resultsCountText.textContent = `${state.activeCategoryFilter} places in ${state.selectedArea.split(",")[0]} (${results.length})`;
      } else {
        resultsCountText.textContent = `Places in ${state.selectedArea.split(",")[0]} (${results.length})`;
      }
    }

    const hasFilters = state.searchQuery || state.activeCategoryFilter || state.maxDistanceKm !== null || state.openNowOnly;
    quickClearFiltersBtn.classList.toggle("hidden", !hasFilters);
  }

  function renderMapView(places) {
    const userCoords = CITY_COORDINATES[state.selectedArea] || CITY_COORDINATES["Mbombela (Nelspruit), Mpumalanga"];
    const areaName = state.selectedArea.split(",")[0];

    searchMapContainer.innerHTML = `
      <div class="map-view-box">
        <div class="map-top-bar">
          <span class="map-center-lbl"><i class="fa-solid fa-location-dot"></i> ${areaName} Center</span>
          <button class="btn-gmaps-link" onclick="window.open('https://www.google.com/maps/search/?api=1&query=${encodeURIComponent((state.searchQuery || 'places') + ' in ' + state.selectedArea)}', '_blank')">
            <i class="fa-solid fa-map-location-dot"></i> Google Maps App
          </button>
        </div>

        <div class="map-pins-canvas" id="mapPinsCanvas">
          <div class="map-center-pulse"></div>
          ${places.slice(0, 15).map((p, idx) => {
            const dLat = (p.latitude - userCoords.lat) * 1200;
            const dLng = (p.longitude - userCoords.lng) * 1200;
            const left = Math.min(Math.max(50 + dLng, 8), 92);
            const top = Math.min(Math.max(50 - dLat, 10), 90);
            const catObj = CATEGORIES.find(c => c.name.toLowerCase() === p.category.toLowerCase()) || { color: "#64748B" };

            return `
              <div class="map-pin" style="left: ${left}%; top: ${top}%; background-color: ${catObj.color};" data-map-pin-id="${p.id}" title="${p.name}">
                <i class="fa-solid fa-location-dot"></i>
              </div>
            `;
          }).join("")}
        </div>

        <div class="map-floating-card" id="mapFloatingCard">
          <p style="font-size: 0.85rem; color: var(--text-muted); text-align: center;">Tap any map pin above to inspect place details and navigate.</p>
        </div>
      </div>
    `;

    document.querySelectorAll(".map-pin").forEach(pin => {
      pin.addEventListener("click", () => {
        const placeId = pin.getAttribute("data-map-pin-id");
        const place = PLACES_DATA.find(p => p.id === placeId);
        if (place) {
          const distance = calculateDistance(place, state.selectedArea);
          const cardContainer = document.getElementById("mapFloatingCard");
          cardContainer.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="badge-category" style="font-size: 0.72rem;">${place.category}</span>
              <span style="font-size: 0.82rem; font-weight: 700; color: var(--primary);">${distance.toFixed(1)} km away</span>
            </div>
            <h4 style="font-size: 0.98rem; font-weight: 700; margin-bottom: 4px;">${place.name}</h4>
            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 10px;">${place.address}</p>
            <div style="display: flex; gap: 8px;">
              <button class="btn-card-action primary" onclick="window.open('https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}', '_blank')">
                <i class="fa-solid fa-diamond-turn-right"></i> Google Maps Directions
              </button>
              <button class="btn-card-action" onclick="window.location.href='tel:${place.phone.replace(/\s+/g, '')}'">
                <i class="fa-solid fa-phone"></i> Call
              </button>
            </div>
          `;
        }
      });
    });
  }

  function renderSavedTab() {
    const savedCount = state.savedPlaceIds.size;
    savedCountBadge.textContent = savedCount;
    if (savedCount > 0) {
      navSavedBadge.textContent = savedCount;
      navSavedBadge.classList.remove("hidden");
    } else {
      navSavedBadge.classList.add("hidden");
    }

    const savedPlaces = PLACES_DATA.filter(p => state.savedPlaceIds.has(p.id));

    if (savedPlaces.length === 0) {
      savedPlacesList.innerHTML = "";
      savedEmptyState.classList.remove("hidden");
      savedCategoryFilterRow.innerHTML = "";
    } else {
      savedEmptyState.classList.add("hidden");
      const savedCats = [...new Set(savedPlaces.map(p => p.category))];
      if (savedCats.length > 1) {
        savedCategoryFilterRow.innerHTML = `
          <button class="category-chip active" data-saved-cat="all">All (${savedPlaces.length})</button>
        ` + savedCats.map(cat => `
          <button class="category-chip" data-saved-cat="${cat}">${cat}</button>
        `).join("");
      } else {
        savedCategoryFilterRow.innerHTML = "";
      }

      savedPlacesList.innerHTML = savedPlaces.map(createPlaceCardHTML).join("");
    }
  }

  function renderProfileTab() {
    statSavedCount.textContent = state.savedPlaceIds.size;
    statSearchCount.textContent = state.searchHistory.length;

    if (state.searchHistory.length === 0) {
      profileSearchHistoryList.innerHTML = '<p style="color: var(--text-muted); font-size: 0.85rem; padding: 10px;">No searches recorded yet.</p>';
    } else {
      profileSearchHistoryList.innerHTML = state.searchHistory.map((q, idx) => `
        <div class="history-item">
          <span>${q}</span>
          <button class="icon-btn-sm" data-delete-history="${idx}"><i class="fa-solid fa-trash-can"></i></button>
        </div>
      `).join("");
    }
  }

  // ==========================================
  // MODAL HANDLERS
  // ==========================================
  function openAreaModal() {
    areaOptionsList.innerHTML = AVAILABLE_AREAS.map(area => `
      <div class="area-option-item ${area === state.selectedArea ? "selected" : ""}" data-area="${area}">
        <div>
          <div style="font-weight: 700;">${area.split(",")[0]}</div>
          <div style="font-size: 0.78rem; color: var(--text-muted);">${area.split(",")[1] || "Mpumalanga"}</div>
        </div>
        ${area === state.selectedArea ? '<i class="fa-solid fa-check"></i>' : ""}
      </div>
    `).join("");
    areaModal.classList.remove("hidden");
  }

  function openDirectionsModal(place) {
    state.directionsPlace = place;
    const distance = calculateDistance(place, state.selectedArea);
    const driveMinutes = Math.max(2, Math.ceil((distance / 40) * 60));
    const walkMinutes = Math.max(5, Math.ceil((distance / 4.8) * 60));

    directionsContent.innerHTML = `
      <div class="directions-header">
        <h3 style="font-size: 1.15rem; font-weight: 800;">${place.name}</h3>
        <p style="font-size: 0.82rem; color: var(--text-muted);"><i class="fa-solid fa-location-dot"></i> ${place.address}</p>
      </div>

      <div class="travel-modes-row">
        <div class="travel-mode-card active" data-mode="driving">
          <i class="fa-solid fa-car"></i>
          <span style="font-weight: 700;">~${driveMinutes} min</span>
          <span style="font-size: 0.72rem; color: var(--text-muted);">Drive</span>
        </div>
        <div class="travel-mode-card" data-mode="walking">
          <i class="fa-solid fa-person-walking"></i>
          <span style="font-weight: 700;">~${walkMinutes} min</span>
          <span style="font-size: 0.72rem; color: var(--text-muted);">Walk</span>
        </div>
        <div class="travel-mode-card" data-mode="transit">
          <i class="fa-solid fa-bus"></i>
          <span style="font-weight: 700;">~${driveMinutes + 12} min</span>
          <span style="font-size: 0.72rem; color: var(--text-muted);">Transit</span>
        </div>
      </div>

      <div style="margin: 16px 0;">
        <button class="btn-gmaps-full" onclick="window.open('https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}', '_blank')">
          <i class="fa-solid fa-diamond-turn-right"></i> Start Navigation in Google Maps
        </button>
      </div>

      <div style="display: flex; gap: 8px;">
        <button class="btn-secondary" style="flex: 1;" onclick="window.open('https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(place.name + ', ' + place.address)}', '_blank')">
          <i class="fa-solid fa-map"></i> View on Google Maps
        </button>
        <button class="btn-secondary" style="flex: 1;" onclick="window.location.href='tel:${place.phone.replace(/\s+/g, '')}'">
          <i class="fa-solid fa-phone"></i> Call Place
        </button>
      </div>
    `;

    directionsModal.classList.remove("hidden");
  }

  function openPlaceDetail(place) {
    state.selectedPlace = place;
    const isSaved = state.savedPlaceIds.has(place.id);
    const distance = calculateDistance(place, state.selectedArea);
    const catObj = CATEGORIES.find(c => c.name.toLowerCase() === place.category.toLowerCase()) || { color: "#64748B", icon: "th-large" };

    detailCategoryBadge.innerHTML = `<i class="fa-solid fa-${catObj.icon}"></i> ${place.category}`;
    detailCategoryBadge.style.backgroundColor = `${catObj.color}15`;
    detailCategoryBadge.style.color = catObj.color;

    detailSaveBtn.innerHTML = `<i class="${isSaved ? "fa-solid" : "fa-regular"} fa-bookmark"></i>`;
    detailSaveBtn.classList.toggle("active", isSaved);

    detailContent.innerHTML = `
      <h3 class="detail-title">${place.name}</h3>
      <div style="display: flex; align-items: center; gap: 8px; font-size: 0.85rem; margin-top: -6px;">
        <span class="rating-badge"><i class="fa-solid fa-star"></i> ${place.rating}</span>
        <span style="color: var(--text-muted)">(${place.reviews} reviews)</span>
        <span style="color: var(--text-muted)">•</span>
        <span style="color: var(--primary); font-weight: 600;">${distance.toFixed(1)} km from ${state.selectedArea.split(",")[0]}</span>
      </div>

      <p style="font-size: 0.9rem; color: var(--text-muted); line-height: 1.45;">${place.description}</p>

      <div class="detail-meta-box">
        <div class="detail-meta-row">
          <i class="fa-regular fa-clock" style="color: var(--primary)"></i>
          <div>
            <span class="status-dot-wrap ${place.isOpen ? "open" : "closed"}">
              <span class="status-dot"></span> ${place.isOpen ? "Open Now" : "Closed"}
            </span>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">${place.openingHours}</div>
          </div>
        </div>
        <div class="detail-meta-row">
          <i class="fa-solid fa-location-dot" style="color: var(--primary)"></i>
          <span>${place.address}</span>
        </div>
        <div class="detail-meta-row">
          <i class="fa-solid fa-phone" style="color: var(--primary)"></i>
          <span>${place.phone}</span>
        </div>
      </div>

      <div>
        <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 8px;">Services Offered</h4>
        <ul class="detail-services-list">
          ${place.services.map(s => `<li><i class="fa-solid fa-circle-check"></i> ${s}</li>`).join("")}
        </ul>
      </div>

      <div>
        <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 8px;">Matched Needs &amp; Tags</h4>
        <div class="tag-cloud">
          ${place.tags.map(t => `<span class="tag-pill">#${t}</span>`).join("")}
        </div>
      </div>

      <div class="detail-bottom-actions">
        <button class="btn-primary" style="flex: 1" onclick="window.open('https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}', '_blank')">
          <i class="fa-solid fa-diamond-turn-right"></i> Google Maps Directions
        </button>
        <button class="btn-secondary" style="flex: 1" onclick="window.location.href='tel:${place.phone.replace(/\s+/g, '')}'">
          <i class="fa-solid fa-phone"></i> Call
        </button>
      </div>
    `;

    placeDetailModal.classList.remove("hidden");
  }

  // ==========================================
  // USER ACTIONS & EVENT LISTENERS
  // ==========================================

  // Tab Navigation
  navItems.forEach(item => {
    item.addEventListener("click", () => {
      const tabId = item.getAttribute("data-tab");
      navItems.forEach(n => n.classList.remove("active"));
      tabPanes.forEach(p => p.classList.remove("active"));
      item.classList.add("active");
      document.getElementById(tabId).classList.add("active");
      state.activeTab = tabId;
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  });

  function switchTab(tabId) {
    const targetNav = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    if (targetNav) targetNav.click();
  }

  // Search Bar Interactions
  heroSearchTrigger.addEventListener("click", () => {
    switchTab("searchTab");
    searchInput.focus();
  });

  searchInput.addEventListener("input", (e) => {
    state.searchQuery = e.target.value;
    clearSearchBtn.classList.toggle("hidden", !state.searchQuery);
    renderSearchTab();
  });

  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && state.searchQuery.trim()) {
      recordSearch(state.searchQuery.trim());
      searchInput.blur();
    }
  });

  clearSearchBtn.addEventListener("click", () => {
    searchInput.value = "";
    state.searchQuery = "";
    clearSearchBtn.classList.add("hidden");
    renderSearchTab();
  });

  // Map vs List Toggle
  toggleMapViewBtn.addEventListener("click", () => {
    state.isMapView = !state.isMapView;
    toggleMapViewBtn.classList.toggle("active", state.isMapView);
    toggleMapViewBtn.innerHTML = state.isMapView ? '<i class="fa-solid fa-list"></i> List' : '<i class="fa-solid fa-map"></i> Map';
    renderSearchTab();
  });

  // Filter drawer toggle
  filterToggleBtn.addEventListener("click", () => {
    filterDrawer.classList.toggle("hidden");
  });

  // Distance filters
  distanceFilterRow.addEventListener("click", (e) => {
    if (e.target.classList.contains("seg-btn")) {
      document.querySelectorAll("#distanceFilterRow .seg-btn").forEach(b => b.classList.remove("active"));
      e.target.classList.add("active");
      const val = e.target.getAttribute("data-dist");
      state.maxDistanceKm = val === "all" ? null : parseFloat(val);
      renderSearchTab();
    }
  });

  openNowSwitch.addEventListener("change", (e) => {
    state.openNowOnly = e.target.checked;
    renderSearchTab();
  });

  sortSelect.addEventListener("change", (e) => {
    state.sortOption = e.target.value;
    renderSearchTab();
  });

  resetFiltersBtn.addEventListener("click", resetAllFilters);
  quickClearFiltersBtn.addEventListener("click", resetAllFilters);
  emptyStateResetBtn.addEventListener("click", resetAllFilters);
  applyFiltersBtn.addEventListener("click", () => {
    filterDrawer.classList.add("hidden");
    renderSearchTab();
  });

  function resetAllFilters() {
    state.searchQuery = "";
    state.activeCategoryFilter = null;
    state.maxDistanceKm = null;
    state.openNowOnly = false;
    state.sortOption = "relevance";
    searchInput.value = "";
    clearSearchBtn.classList.add("hidden");
    openNowSwitch.checked = false;
    sortSelect.value = "relevance";
    document.querySelectorAll("#distanceFilterRow .seg-btn").forEach((b, i) => b.classList.toggle("active", i === 0));
    filterDrawer.classList.add("hidden");
    renderSearchTab();
  }

  // Quick Prompt Chips
  document.querySelectorAll(".prompt-chip[data-query]").forEach(chip => {
    chip.addEventListener("click", () => {
      const q = chip.getAttribute("data-query");
      executeSearchPrompt(q);
    });
  });

  document.addEventListener("click", (e) => {
    const chip = e.target.closest(".recent-search-chip");
    if (chip) executeSearchPrompt(chip.getAttribute("data-query"));
  });

  function executeSearchPrompt(query) {
    state.searchQuery = query;
    searchInput.value = query;
    clearSearchBtn.classList.remove("hidden");
    recordSearch(query);
    switchTab("searchTab");
    renderSearchTab();
  }

  function recordSearch(query) {
    if (!query) return;
    state.searchHistory = state.searchHistory.filter(q => q.toLowerCase() !== query.toLowerCase());
    state.searchHistory.unshift(query);
    if (state.searchHistory.length > 10) state.searchHistory.pop();
    localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify(state.searchHistory));
    renderProfileTab();
  }

  // Category selections
  homeCategoryGrid.addEventListener("click", (e) => {
    const tile = e.target.closest(".category-tile");
    if (tile) {
      state.activeCategoryFilter = tile.getAttribute("data-cat");
      switchTab("searchTab");
      renderSearchTab();
    }
  });

  searchCategoryChips.addEventListener("click", (e) => {
    const chip = e.target.closest(".category-chip");
    if (chip) {
      const cat = chip.getAttribute("data-cat");
      state.activeCategoryFilter = cat === "all" ? null : cat;
      renderSearchTab();
    }
  });

  viewAllCategoriesBtn.addEventListener("click", () => {
    switchTab("searchTab");
    filterDrawer.classList.remove("hidden");
  });

  // Bookmark / Save toggling & Directions trigger
  document.addEventListener("click", (e) => {
    const directionsBtn = e.target.closest(".gmaps-action");
    if (directionsBtn) {
      e.stopPropagation();
      const placeId = directionsBtn.getAttribute("data-directions-id");
      const place = PLACES_DATA.find(p => p.id === placeId);
      if (place) openDirectionsModal(place);
      return;
    }

    const saveBtn = e.target.closest(".save-btn");
    if (saveBtn) {
      e.stopPropagation();
      const placeId = saveBtn.getAttribute("data-save-id");
      toggleSavePlace(placeId);
      return;
    }

    const card = e.target.closest(".place-card");
    if (card) {
      const placeId = card.getAttribute("data-id");
      const place = PLACES_DATA.find(p => p.id === placeId);
      if (place) openPlaceDetail(place);
    }
  });

  function toggleSavePlace(placeId) {
    if (state.savedPlaceIds.has(placeId)) {
      state.savedPlaceIds.delete(placeId);
    } else {
      state.savedPlaceIds.add(placeId);
    }
    localStorage.setItem(STORAGE_KEYS.SAVED_PLACES, JSON.stringify([...state.savedPlaceIds]));
    renderSavedTab();
    renderHomeTab();
    renderSearchTab();
    renderProfileTab();
    if (state.selectedPlace && state.selectedPlace.id === placeId) {
      const isSaved = state.savedPlaceIds.has(placeId);
      detailSaveBtn.innerHTML = `<i class="${isSaved ? "fa-solid" : "fa-regular"} fa-bookmark"></i>`;
      detailSaveBtn.classList.toggle("active", isSaved);
    }
  }

  homeSavedBanner.addEventListener("click", () => switchTab("savedTab"));
  savedExploreBtn.addEventListener("click", () => switchTab("searchTab"));

  // Area Selection
  areaSelectorBtn.addEventListener("click", openAreaModal);
  profileAreaChangeBtn.addEventListener("click", openAreaModal);
  closeAreaModal.addEventListener("click", () => areaModal.classList.add("hidden"));

  areaOptionsList.addEventListener("click", (e) => {
    const item = e.target.closest(".area-option-item");
    if (item) {
      const selected = item.getAttribute("data-area");
      state.selectedArea = selected;
      localStorage.setItem(STORAGE_KEYS.USER_AREA, selected);
      areaModal.classList.add("hidden");
      renderAll();
    }
  });

  // Detail Modal Actions
  closeDetailModal.addEventListener("click", () => placeDetailModal.classList.add("hidden"));
  closeDirectionsModal.addEventListener("click", () => directionsModal.classList.add("hidden"));

  detailSaveBtn.addEventListener("click", () => {
    if (state.selectedPlace) toggleSavePlace(state.selectedPlace.id);
  });

  detailShareBtn.addEventListener("click", () => {
    if (!state.selectedPlace) return;
    const shareText = `Check out ${state.selectedPlace.name} in Mpumalanga on Nearly! ${state.selectedPlace.description} Directions: https://www.google.com/maps/dir/?api=1&destination=${state.selectedPlace.latitude},${state.selectedPlace.longitude}`;
    if (navigator.share) {
      navigator.share({ title: state.selectedPlace.name, text: shareText, url: window.location.href }).catch(() => {});
    } else {
      navigator.clipboard.writeText(shareText);
      alert("Place information and Google Maps directions link copied to clipboard!");
    }
  });

  // Edit Name Modal
  editNameBtn.addEventListener("click", () => {
    nameInput.value = state.userName;
    editNameModal.classList.remove("hidden");
  });
  closeNameModal.addEventListener("click", () => editNameModal.classList.add("hidden"));
  saveNameBtn.addEventListener("click", () => {
    const val = nameInput.value.trim();
    if (val) {
      state.userName = val;
      localStorage.setItem(STORAGE_KEYS.USER_NAME, val);
      editNameModal.classList.add("hidden");
      renderProfileTab();
      renderHeader();
    }
  });

  // Profile Search History Management
  profileSearchHistoryList.addEventListener("click", (e) => {
    const delBtn = e.target.closest("[data-delete-history]");
    if (delBtn) {
      const idx = parseInt(delBtn.getAttribute("data-delete-history"), 10);
      state.searchHistory.splice(idx, 1);
      localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify(state.searchHistory));
      renderProfileTab();
      renderHomeTab();
    }
  });

  clearAllHistoryBtn.addEventListener("click", () => {
    if (confirm("Clear all search history?")) {
      state.searchHistory = [];
      localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify([]));
      renderProfileTab();
      renderHomeTab();
    }
  });

  // Theme Management
  function applyTheme(mode) {
    state.themeMode = mode;
    localStorage.setItem(STORAGE_KEYS.THEME, mode);
    if (mode === "dark") {
      document.body.classList.add("dark-mode");
    } else if (mode === "light") {
      document.body.classList.remove("dark-mode");
    } else {
      const prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
      document.body.classList.toggle("dark-mode", prefersDark);
    }
    document.querySelectorAll("#themeSegmented .seg-sm").forEach(b => {
      b.classList.toggle("active", b.getAttribute("data-theme") === mode);
    });
    renderHeader();
  }

  themeToggleBtn.addEventListener("click", () => {
    const isDark = document.body.classList.contains("dark-mode");
    applyTheme(isDark ? "light" : "dark");
  });

  themeSegmented.addEventListener("click", (e) => {
    if (e.target.classList.contains("seg-sm")) {
      applyTheme(e.target.getAttribute("data-theme"));
    }
  });

  aboutBtn.addEventListener("click", () => aboutModal.classList.remove("hidden"));
  closeAboutModal.addEventListener("click", () => aboutModal.classList.add("hidden"));
  closeAboutBtn.addEventListener("click", () => aboutModal.classList.add("hidden"));

  // Initial load
  applyTheme(state.themeMode);
  renderAll();
});
