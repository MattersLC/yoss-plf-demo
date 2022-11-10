package ito.plf.yossdemo.place

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ito.plf.yossdemo.R
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * A custom cluster renderer for Place objects.
 */
class PlaceRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Place>
) : DefaultClusterRenderer<Place>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: Place,
        markerOptions: MarkerOptions
    ) {
        when(item.category) {
            "Hospital" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
            "Turismo" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_48))
            "Plaza" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
            "Mercado" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
            "Universidad" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
            "Cariñosos" -> markerOptions.title(item.name)
                .position(item.latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.heart_48))
            else -> {
                markerOptions.title(item.name)
                    .position(item.latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador_t32))
            }
        }
    }

    /**
     * Method called right after the cluster item (the marker) is rendered.
     * This is where properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: Place, marker: Marker) {
        marker.tag = clusterItem
    }
}