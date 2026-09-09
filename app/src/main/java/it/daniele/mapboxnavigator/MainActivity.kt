package it.daniele.mapboxnavigator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.extension.style.terrain.setStyleTerrain
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import it.daniele.mapboxnavigator.databinding.ActivityMainBinding
import it.daniele.mapboxnavigator.databinding.SheetMapCustomizationBinding
import java.util.Locale

/**
 * Navigatore turn-by-turn con ricerca Mapbox, edifici 3D e posizione GPS reale.
 * La destinazione può essere cercata oppure selezionata con una pressione prolungata.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainActivity : AppCompatActivity() {

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1_500L
        private const val PALERMO_LONGITUDE = 13.3615
        private const val PALERMO_LATITUDE = 38.1157
        private const val STANDARD_STYLE_IMPORT_ID = "basemap"
        private const val TERRAIN_SOURCE_ID = "daniele-terrain-dem"
        private const val TERRAIN_TILESET_URL = "mapbox://mapbox.mapbox-terrain-dem-v1"
        private const val TERRAIN_EXAGGERATION = 1.3
        private const val LIGHT_PRESET_DAWN = "dawn"
        private const val LIGHT_PRESET_DAY = "day"
        private const val LIGHT_PRESET_DUSK = "dusk"
        private const val LIGHT_PRESET_NIGHT = "night"
        private const val STYLE_THEME_DEFAULT = "default"
        private const val STYLE_THEME_FADED = "faded"
        private const val STYLE_THEME_MONOCHROME = "monochrome"
        private const val AUTO_FOLLOW_DELAY_MS = 6_000L
        private const val APPEARANCE_PREFS_NAME = "map_appearance"
        private const val KEY_LIGHT_PRESET = "light_preset"
        private const val KEY_STYLE_THEME = "style_theme"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var maneuverApi: MapboxManeuverApi
    private lateinit var tripProgressApi: MapboxTripProgressApi
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var routeArrowView: MapboxRouteArrowView
    private lateinit var speechApi: MapboxSpeechApi
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var destinationAnnotationManager: CircleAnnotationManager

    private val routeArrowApi = MapboxRouteArrowApi()
    private val navigationLocationProvider = NavigationLocationProvider()
    private var firstLocationReceived = false
    private var navigationAttached = false
    private var tripSessionStarted = false
    private var activeGuidance = false
    private var ignoreSearchTextChanges = false
    private var threeDimensionalMode = true
    private var currentLightPreset = LIGHT_PRESET_DAY
    private var currentStyleTheme = STYLE_THEME_DEFAULT

    private val autoFollowHandler = Handler(Looper.getMainLooper())
    private val returnToFollowingRunnable = Runnable {
        navigationCamera.requestNavigationCameraToFollowing()
    }

    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            150.0 * pixelDensity,
            44.0 * pixelDensity,
            170.0 * pixelDensity,
            44.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            44.0 * pixelDensity,
            360.0 * pixelDensity,
            120.0 * pixelDensity,
            44.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            190.0 * pixelDensity,
            44.0 * pixelDensity,
            190.0 * pixelDensity,
            44.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            44.0 * pixelDensity,
            360.0 * pixelDensity,
            130.0 * pixelDensity,
            44.0 * pixelDensity
        )
    }

    private var voiceMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val preciseLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    hasPreciseLocationPermission()

            if (preciseLocationGranted) {
                startTripSessionWhenReady()
            } else {
                showStatus(getString(R.string.gps_permission_denied), persist = true)
                Toast.makeText(this, R.string.gps_permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) = Unit

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation

            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints
            )
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            if (!firstLocationReceived) {
                firstLocationReceived = true
                showStatus(getString(R.string.map_hint_ready), persist = true)
                navigationCamera.requestNavigationCameraToFollowing(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0)
                        .build()
                )
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        routeLineApi.updateWithRouteProgress(routeProgress) { update ->
            binding.mapView.mapboxMap.style?.let { style ->
                routeLineView.renderRouteLineUpdate(style, update)
            }
        }

        binding.mapView.mapboxMap.style?.let { style ->
            val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, arrowUpdate)
        }

        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(this, error.errorMessage, Toast.LENGTH_SHORT).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        binding.tripProgressView.render(tripProgressApi.getTripProgress(routeProgress))
    }

    private val routesObserver = RoutesObserver { routeUpdate ->
        val routes = routeUpdate.navigationRoutes
        if (routes.isNotEmpty()) {
            routeLineApi.setNavigationRoutes(routes) { drawData ->
                binding.mapView.mapboxMap.style?.let { style ->
                    routeLineView.renderRouteDrawData(style, drawData)
                }
            }

            viewportDataSource.onRouteChanged(routes.first())
            viewportDataSource.evaluate()
            binding.routeLoading.visibility = View.GONE

            if (activeGuidance) {
                showStatus(getString(R.string.route_recalculated))
            }
        } else {
            clearRouteGraphics()
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { announcement ->
            speechApi.clean(announcement)
        }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                navigationAttached = true
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
                startTripSessionWhenReady()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                navigationAttached = false
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            }
        },
        onInitialize = this::initializeNavigationSdk
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedMapAppearance()
        initializeNavigationUi()
        initializeMap()
        initializeSearch()
        initializeControls()

        if (!isPublicTokenConfigured()) {
            showStatus(getString(R.string.token_missing), persist = true)
            Toast.makeText(this, R.string.token_missing, Toast.LENGTH_LONG).show()
        }

        requestRequiredPermissions()
    }

    private fun initializeNavigationSdk() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )
    }

    private fun loadSavedMapAppearance() {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val defaultPreset =
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) LIGHT_PRESET_NIGHT else LIGHT_PRESET_DAY
        val prefs = getSharedPreferences(APPEARANCE_PREFS_NAME, Context.MODE_PRIVATE)
        currentLightPreset = prefs.getString(KEY_LIGHT_PRESET, defaultPreset) ?: defaultPreset
        currentStyleTheme = prefs.getString(KEY_STYLE_THEME, STYLE_THEME_DEFAULT) ?: STYLE_THEME_DEFAULT
    }

    private fun initializeNavigationUi() {
        viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.mapboxMap)
        navigationCamera = NavigationCamera(
            binding.mapView.mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )

        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { state ->
            binding.recenter.visibility = when (state) {
                NavigationCameraState.FOLLOWING,
                NavigationCameraState.TRANSITION_TO_FOLLOWING -> {
                    autoFollowHandler.removeCallbacks(returnToFollowingRunnable)
                    View.INVISIBLE
                }

                else -> View.VISIBLE
            }
        }

        // Se l'utente sposta la mappa a mano, dopo una breve pausa la camera
        // torna da sola a seguire il puntatore, come nei navigatori Mapbox.
        binding.mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                autoFollowHandler.removeCallbacks(returnToFollowingRunnable)
            }

            override fun onMove(detector: MoveGestureDetector): Boolean = false

            override fun onMoveEnd(detector: MoveGestureDetector) {
                autoFollowHandler.removeCallbacks(returnToFollowingRunnable)
                autoFollowHandler.postDelayed(returnToFollowingRunnable, AUTO_FOLLOW_DELAY_MS)
            }
        })

        val landscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        viewportDataSource.overviewPadding =
            if (landscape) landscapeOverviewPadding else overviewPadding
        viewportDataSource.followingPadding =
            if (landscape) landscapeFollowingPadding else followingPadding

        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()
        maneuverApi = MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions))
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(TimeRemainingFormatter(this))
                .percentRouteTraveledFormatter(PercentDistanceTraveledFormatter())
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        speechApi = MapboxSpeechApi(this, Locale.ITALIAN.language)
        voiceInstructionsPlayer =
            MapboxVoiceInstructionsPlayer(this, Locale.ITALIAN.language)

        routeLineApi = MapboxRouteLineApi(
            MapboxRouteLineApiOptions.Builder()
                .vanishingRouteLineEnabled(true)
                .build()
        )
        routeLineView = MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(this).build()
        )
        routeArrowView = MapboxRouteArrowView(RouteArrowOptions.Builder(this).build())
    }

    private fun initializeMap() {
        binding.mapView.scalebar.enabled = false
        binding.mapView.compass.marginTop = 165f * pixelDensity
        binding.mapView.logo.marginBottom = 120f * pixelDensity
        binding.mapView.attribution.marginBottom = 120f * pixelDensity
        destinationAnnotationManager =
            binding.mapView.annotations.createCircleAnnotationManager(null)

        binding.mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(PALERMO_LONGITUDE, PALERMO_LATITUDE))
                .zoom(15.2)
                .pitch(55.0)
                .build()
        )

        binding.mapView.mapboxMap.loadStyle(Style.STANDARD) { style ->
            applyStandardStyleConfiguration(style)
            addRealisticTerrain(style)
            routeLineView.initializeLayers(style)
            binding.mapView.gestures.addOnMapLongClickListener { destination ->
                showDestinationMarker(destination)
                requestRoute(destination)
                true
            }
        }

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.Companion.from(
                    com.mapbox.navigation.ui.components.R.drawable.mapbox_navigation_puck_icon
                )
            )
            puckBearingEnabled = true
            enabled = true
        }
    }

    // Lo stile Standard include già edifici 3D realistici, ombre e attraversamenti
    // pedonali nativi: qui si applicano solo le preferenze di aspetto dell'utente.
    private fun applyStandardStyleConfiguration(style: Style) {
        applyLightPreset(style, currentLightPreset)
        applyStyleTheme(style, currentStyleTheme)
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "show3dObjects",
            Value.valueOf(true)
        )
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "showPlaceLabels",
            Value.valueOf(true)
        )
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "showRoadLabels",
            Value.valueOf(true)
        )
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "showPointOfInterestLabels",
            Value.valueOf(true)
        )
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "showLandmarkIcons",
            Value.valueOf(true)
        )
    }

    // Rilievo 3D reale (Mapbox Terrain-DEM): stesse tile della mappa normale,
    // niente costi né prodotti aggiuntivi rispetto al piano gratuito.
    private fun addRealisticTerrain(style: Style) {
        style.addSource(
            rasterDemSource(TERRAIN_SOURCE_ID) {
                url(TERRAIN_TILESET_URL)
                tileSize(514)
            }
        )
        style.setStyleTerrain(
            terrain(TERRAIN_SOURCE_ID) {
                exaggeration(TERRAIN_EXAGGERATION)
            }
        )
    }

    private fun applyLightPreset(style: Style, preset: String) {
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "lightPreset",
            Value.valueOf(preset)
        )
    }

    private fun applyStyleTheme(style: Style, theme: String) {
        style.setStyleImportConfigProperty(
            STANDARD_STYLE_IMPORT_ID,
            "theme",
            Value.valueOf(theme)
        )
    }

    private fun persistMapAppearance() {
        getSharedPreferences(APPEARANCE_PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_LIGHT_PRESET, currentLightPreset)
            .putString(KEY_STYLE_THEME, currentStyleTheme)
            .apply()
    }

    private fun showMapCustomizationSheet() {
        val sheetBinding = SheetMapCustomizationBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.lightPresetToggle.check(
            when (currentLightPreset) {
                LIGHT_PRESET_DAWN -> sheetBinding.presetDawn.id
                LIGHT_PRESET_DUSK -> sheetBinding.presetDusk.id
                LIGHT_PRESET_NIGHT -> sheetBinding.presetNight.id
                else -> sheetBinding.presetDay.id
            }
        )
        sheetBinding.themeToggle.check(
            when (currentStyleTheme) {
                STYLE_THEME_FADED -> sheetBinding.themeFaded.id
                STYLE_THEME_MONOCHROME -> sheetBinding.themeMonochrome.id
                else -> sheetBinding.themeStandard.id
            }
        )

        sheetBinding.lightPresetToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentLightPreset = when (checkedId) {
                sheetBinding.presetDawn.id -> LIGHT_PRESET_DAWN
                sheetBinding.presetDusk.id -> LIGHT_PRESET_DUSK
                sheetBinding.presetNight.id -> LIGHT_PRESET_NIGHT
                else -> LIGHT_PRESET_DAY
            }
            binding.mapView.mapboxMap.style?.let { applyLightPreset(it, currentLightPreset) }
            persistMapAppearance()
        }

        sheetBinding.themeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentStyleTheme = when (checkedId) {
                sheetBinding.themeFaded.id -> STYLE_THEME_FADED
                sheetBinding.themeMonochrome.id -> STYLE_THEME_MONOCHROME
                else -> STYLE_THEME_DEFAULT
            }
            binding.mapView.mapboxMap.style?.let { applyStyleTheme(it, currentStyleTheme) }
            persistMapAppearance()
        }

        dialog.show()
    }

    private fun initializeSearch() {
        binding.searchResultsView.initialize(
            SearchResultsView.Configuration(
                CommonSearchViewConfiguration(DistanceUnitType.METRIC)
            )
        )

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.SEARCH_BOX,
            settings = SearchEngineSettings()
        )
        val offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings()
        )
        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = binding.searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine
        )

        searchEngineUiAdapter.addSearchListener(object : SearchEngineUiAdapter.SearchListener {
            override fun onSuggestionsShown(
                suggestions: List<SearchSuggestion>,
                responseInfo: ResponseInfo
            ) = Unit

            override fun onSearchResultsShown(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) = Unit

            override fun onOfflineSearchResultsShown(
                results: List<OfflineSearchResult>,
                responseInfo: OfflineResponseInfo
            ) = Unit

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean = false

            override fun onSearchResultSelected(
                searchResult: SearchResult,
                responseInfo: ResponseInfo
            ) {
                selectDestination(searchResult.name, searchResult.coordinate)
            }

            override fun onOfflineSearchResultSelected(
                searchResult: OfflineSearchResult,
                responseInfo: OfflineResponseInfo
            ) {
                selectDestination(searchResult.name, searchResult.coordinate)
            }

            override fun onError(e: Exception) {
                binding.searchResultsCard.visibility = View.GONE
                Toast.makeText(
                    this@MainActivity,
                    R.string.search_error,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                selectDestination(historyRecord.name, historyRecord.coordinate)
            }

            override fun onPopulateQueryClick(
                suggestion: SearchSuggestion,
                responseInfo: ResponseInfo
            ) {
                binding.searchInput.setText(suggestion.name)
                binding.searchInput.setSelection(binding.searchInput.text?.length ?: 0)
            }

            override fun onFeedbackItemClick(responseInfo: ResponseInfo) = Unit
        })

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                value: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                value: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit

            override fun afterTextChanged(value: Editable?) {
                val query = value?.toString().orEmpty().trim()
                binding.clearSearch.visibility =
                    if (query.isEmpty()) View.GONE else View.VISIBLE

                if (ignoreSearchTextChanges || activeGuidance) return

                if (query.length >= 2) {
                    binding.statusHint.visibility = View.GONE
                    binding.searchResultsCard.visibility = View.VISIBLE
                    searchEngineUiAdapter.search(query)
                } else {
                    binding.searchResultsCard.visibility = View.GONE
                    binding.statusHint.visibility = View.VISIBLE
                }
            }
        })

        binding.searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchInput.text?.length.orZero() >= 2) {
                binding.searchResultsCard.visibility = View.VISIBLE
                binding.statusHint.visibility = View.GONE
            }
        }
    }

    private fun initializeControls() {
        binding.stop.setOnClickListener { stopActiveGuidance() }
        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            voiceMuted = !voiceMuted
        }
        binding.soundButton.unmute()

        binding.clearSearch.setOnClickListener {
            ignoreSearchTextChanges = true
            binding.searchInput.setText("")
            ignoreSearchTextChanges = false
            binding.clearSearch.visibility = View.GONE
            binding.searchResultsCard.visibility = View.GONE
            destinationAnnotationManager.deleteAll()
            showStatus(getString(R.string.map_hint_ready), persist = true)
        }

        binding.mapModeButton.setOnClickListener {
            threeDimensionalMode = !threeDimensionalMode
            binding.mapModeButton.text = if (threeDimensionalMode) "3D" else "2D"
            binding.mapModeButton.contentDescription = getString(
                if (threeDimensionalMode) R.string.map_mode_3d else R.string.map_mode_2d
            )

            val currentZoom = binding.mapView.mapboxMap.cameraState.zoom
            binding.mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .pitch(if (threeDimensionalMode) 55.0 else 0.0)
                    .zoom(if (threeDimensionalMode) maxOf(15.0, currentZoom) else currentZoom)
                    .build()
            )
            binding.mapView.mapboxMap.style?.setStyleImportConfigProperty(
                STANDARD_STYLE_IMPORT_ID,
                "show3dObjects",
                Value.valueOf(threeDimensionalMode)
            )
        }

        binding.customizeButton.setOnClickListener { showMapCustomizationSheet() }
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun selectDestination(name: String, coordinate: Point) {
        ignoreSearchTextChanges = true
        binding.searchInput.setText(name)
        binding.searchInput.setSelection(binding.searchInput.text?.length ?: 0)
        ignoreSearchTextChanges = false
        binding.searchInput.clearFocus()
        binding.searchResultsCard.visibility = View.GONE
        hideKeyboard()
        showDestinationMarker(coordinate)
        showStatus(getString(R.string.destination_selected, name), persist = true)
        requestRoute(coordinate)
    }

    private fun showDestinationMarker(destination: Point) {
        destinationAnnotationManager.deleteAll()
        destinationAnnotationManager.create(
            CircleAnnotationOptions()
                .withPoint(destination)
                .withCircleRadius(9.0)
                .withCircleColor("#1267E5")
                .withCircleStrokeWidth(3.0)
                .withCircleStrokeColor("#FFFFFF")
        )
    }

    private fun hideKeyboard() {
        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    @SuppressLint("MissingPermission")
    private fun startTripSessionWhenReady() {
        if (!navigationAttached || tripSessionStarted || !hasPreciseLocationPermission()) {
            return
        }
        mapboxNavigation.startTripSession()
        tripSessionStarted = true
        showStatus(getString(R.string.map_hint_waiting), persist = true)
    }

    private fun requestRoute(destination: Point) {
        if (!isPublicTokenConfigured()) {
            Toast.makeText(this, R.string.token_missing, Toast.LENGTH_LONG).show()
            return
        }

        val originLocation = navigationLocationProvider.lastLocation
        if (originLocation == null) {
            Toast.makeText(this, R.string.gps_not_ready, Toast.LENGTH_SHORT).show()
            return
        }

        val origin = Point.fromLngLat(originLocation.longitude, originLocation.latitude)
        showStatus(getString(R.string.route_calculating), persist = true)
        binding.routeLoading.visibility = View.VISIBLE

        val optionsBuilder = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            .language(Locale.ITALIAN.language)
            .coordinatesList(listOf(origin, destination))

        originLocation.bearing?.let { bearing ->
            optionsBuilder.bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(bearing)
                        .degrees(45.0)
                        .build(),
                    null
                )
            )
        }

        mapboxNavigation.requestRoutes(
            optionsBuilder.build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    binding.routeLoading.visibility = View.GONE
                    showStatus(getString(R.string.route_cancelled))
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    binding.routeLoading.visibility = View.GONE
                    showStatus(getString(R.string.route_error), persist = true)
                    Toast.makeText(
                        this@MainActivity,
                        R.string.route_error,
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    beginActiveGuidance(routes)
                }
            }
        )
    }

    private fun beginActiveGuidance(routes: List<NavigationRoute>) {
        if (routes.isEmpty()) return

        activeGuidance = true
        mapboxNavigation.setNavigationRoutes(routes)
        binding.routeLoading.visibility = View.GONE
        binding.statusHint.visibility = View.GONE
        binding.searchCard.visibility = View.GONE
        binding.searchResultsCard.visibility = View.GONE
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun stopActiveGuidance() {
        if (!activeGuidance) return

        activeGuidance = false
        mapboxNavigation.setNavigationRoutes(emptyList())
        binding.routeLoading.visibility = View.GONE
        binding.searchCard.visibility = View.VISIBLE
        binding.soundButton.visibility = View.GONE
        binding.routeOverview.visibility = View.GONE
        binding.maneuverView.visibility = View.GONE
        binding.tripProgressCard.visibility = View.GONE
        destinationAnnotationManager.deleteAll()
        ignoreSearchTextChanges = true
        binding.searchInput.setText("")
        ignoreSearchTextChanges = false
        binding.clearSearch.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        showStatus(getString(R.string.navigation_stopped))
        navigationCamera.requestNavigationCameraToFollowing()
    }

    private fun clearRouteGraphics() {
        binding.mapView.mapboxMap.style?.let { style ->
            routeLineApi.clearRouteLine { clearValue ->
                routeLineView.renderClearRouteLineValue(style, clearValue)
            }
            routeArrowView.render(style, routeArrowApi.clearArrows())
        }
    }

    private fun showStatus(message: String, persist: Boolean = false) {
        binding.statusHint.text = message
        binding.statusHint.visibility = View.VISIBLE
        binding.statusHint.removeCallbacks(hideStatusRunnable)

        if (!persist) {
            binding.statusHint.postDelayed(hideStatusRunnable, 2_500L)
        }
    }

    private val hideStatusRunnable = Runnable {
        if (activeGuidance) {
            binding.statusHint.visibility = View.GONE
        } else {
            binding.statusHint.text = getString(R.string.map_hint_ready)
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()
        if (!hasPreciseLocationPermission()) {
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
            permissions += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }

        if (permissions.isEmpty()) {
            startTripSessionWhenReady()
            return
        }
        locationPermissionRequest.launch(permissions.toTypedArray())
    }

    private fun hasPreciseLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun isPublicTokenConfigured(): Boolean =
        getString(R.string.mapbox_access_token).startsWith("pk.") &&
            !getString(R.string.mapbox_access_token).contains("not-configured")

    override fun onDestroy() {
        binding.statusHint.removeCallbacks(hideStatusRunnable)
        autoFollowHandler.removeCallbacks(returnToFollowingRunnable)
        destinationAnnotationManager.deleteAll()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        super.onDestroy()
    }
}
