package com.example.itrack.common.base

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class LocationHelper {
    companion object {
        fun mostNorth(locations: List<Location>): Location {
            return locations.sortedBy { location ->
                location.latitude
            }.let { sortedList ->
                sortedList.last()
            }
        }

        fun mostSouth(locations: List<Location>): Location {
            return locations.sortedBy { location ->
                location.latitude
            }.let { sortedList ->
                sortedList.first()
            }
        }

        fun mostWest(locations: List<Location>): Location {
            return locations.sortedBy { location ->
                location.longitude
            }.let { sortedList ->
                sortedList.first()
            }
        }

        fun mostEast(locations: List<Location>): Location {
            return locations.sortedBy { location ->
                location.longitude
            }.let { sortedList ->
                sortedList.last()
            }
        }

        fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }
    }
}