package pe.edu.upc.rent2go_kotlin.common.ui.map

import pe.edu.upc.rent2go_kotlin.R
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay

/**
 * Map coordinates fix (TASK 4 follow-up, 2026-07-06) — replaces Google Maps Compose
 * (com.google.maps.android:maps-compose) with OSMDroid (OpenStreetMap), which renders
 * real map tiles WITHOUT requiring any API key. This removes the recurring blank-map
 * bug caused by the placeholder MAPS_API_KEY in local.properties: there is no key to
 * misconfigure anymore.
 *
 * Mirrors the small subset of the Google Maps Compose API actually used in this app
 * (camera position state, markers, long-press) so call sites stay close to the
 * original GoogleMap/Marker/rememberCameraPositionState usage.
 */

/** Simple lat/lng pair, replacement for com.google.android.gms.maps.model.LatLng. */
data class MapLatLng(val latitude: Double, val longitude: Double) {
    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
}

/** Holds the current camera center/zoom and lets callers animate it, like CameraPositionState. */
class MapCameraState(initialCenter: MapLatLng, initialZoom: Double = 12.0) {
    var center by mutableStateOf(initialCenter)
        internal set
    var zoom by mutableStateOf(initialZoom)
        internal set

    private var mapView: MapView? = null

    internal fun attach(view: MapView) {
        mapView = view
    }

    internal fun detach() {
        mapView = null
    }

    fun moveTo(target: MapLatLng, zoomLevel: Double = zoom) {
        center = target
        zoom = zoomLevel
        mapView?.controller?.setCenter(target.toGeoPoint())
        mapView?.controller?.setZoom(zoomLevel)
    }

    fun animateTo(target: MapLatLng, zoomLevel: Double = zoom) {
        center = target
        zoom = zoomLevel
        mapView?.controller?.animateTo(target.toGeoPoint())
        mapView?.controller?.setZoom(zoomLevel)
    }
}

@Composable
fun rememberMapCameraState(initialCenter: MapLatLng, initialZoom: Double = 12.0): MapCameraState {
    return remember { MapCameraState(initialCenter, initialZoom) }
}

/** Declarative marker description consumed by [OsmMapView]. */
data class MapMarker(
    val position: MapLatLng,
    val title: String? = null,
    val snippet: String? = null
)

/**
 * OSMDroid-backed map composable. Renders real OpenStreetMap tiles, supports markers,
 * a draggable/zoomable viewport, and an optional long-press callback (used by the
 * geo-radius search feature). No API key required.
 *
 * @param onMapLongClick invoked with the tapped-and-held geographic point.
 * @param gesturesEnabled disables all pan/zoom/rotate gestures for read-only previews
 *   (equivalent to the old MapUiSettings(scrollGesturesEnabled = false, ...) usage).
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    cameraState: MapCameraState,
    markers: List<MapMarker> = emptyList(),
    gesturesEnabled: Boolean = true,
    showZoomControls: Boolean = true,
    onMapLongClick: ((MapLatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentMarkers by rememberUpdatedState(markers)
    val currentOnLongClick by rememberUpdatedState(onMapLongClick)

    DisposableEffect(Unit) {
        // Required once per process by osmdroid to configure tile cache/user-agent.
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(gesturesEnabled)
                zoomController.setVisibility(
                    if (showZoomControls) org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                    else org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
                )
                setBuiltInZoomControls(showZoomControls)
                isClickable = gesturesEnabled
                controller.setZoom(cameraState.zoom)
                controller.setCenter(cameraState.center.toGeoPoint())
                cameraState.attach(this)

                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        if (p != null) {
                            currentOnLongClick?.invoke(MapLatLng(p.latitude, p.longitude))
                            return true
                        }
                        return false
                    }
                }
                overlays.add(MapEventsOverlay(eventsReceiver))
            }
        },
        update = { mapView ->
            cameraState.attach(mapView)
            mapView.setMultiTouchControls(gesturesEnabled)
            mapView.isClickable = gesturesEnabled

            // Rebuild marker overlays (keep the first overlay, the MapEventsOverlay).
            val eventsOverlay = mapView.overlays.firstOrNull { it is MapEventsOverlay }
            mapView.overlays.clear()
            if (eventsOverlay != null) {
                mapView.overlays.add(eventsOverlay)
            }
            // Google Maps-style teardrop pin (replaces OSMDroid's default "hand" marker
            // icon). Re-resolved per update() call since it's cheap (drawable cache) and
            // keeps this block self-contained; anchor is CENTER/BOTTOM so the pin's tip
            // (bottom-center of the vector, see ic_map_pin.xml) touches the exact coordinate.
            val pinDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.ic_map_pin)
            currentMarkers.forEach { markerData ->
                val marker = Marker(mapView).apply {
                    position = markerData.position.toGeoPoint()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = pinDrawable
                    title = markerData.title
                    snippet = markerData.snippet
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        },
        onRelease = { mapView ->
            cameraState.detach()
            mapView.onDetach()
        }
    )
}
