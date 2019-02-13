package com.example.itrack.common

import android.location.Location
import com.example.itrack.location.LocationStatisticsData
import com.google.android.gms.maps.model.LatLng
import java.math.BigDecimal

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

        fun distanceBetweenTwoLocations(v1: Location?, v2: Location?): Double {
            if (v1 == null || v2 == null) return 0.0

            if (v1.latitude == v2.latitude && v1.longitude == v2.longitude) {
                return 0.0
            } else {
                val theta = v1.longitude - v2.longitude
                var dist =
                    Math.sin(Math.toRadians(v1.latitude)) * Math.sin(Math.toRadians(v2.latitude)) + Math.cos(
                        Math.toRadians(
                            v1.latitude
                        )
                    ) * Math.cos(
                        Math.toRadians(v2.latitude)
                    ) * Math.cos(Math.toRadians(theta))
                dist = Math.acos(dist)
                dist = Math.toDegrees(dist)
                dist *= 60.0 * 1.1515 * 1.609344 * 1000
                return dist
            }
        }

        fun getStatisticsData(locations: List<Location>): LocationStatisticsData {
            if (locations.isEmpty()) {
                return LocationStatisticsData()
            }
            var maxSpeed = 0F
            var minSpeed = 0F
            var speedSum = 0F
            var maxAlt = 0.0
            var minAlt = 0.0
            var altSum = 0.0
            var totalDistance = 0.0
            var oldLocation: Location? = null
            locations.forEach {
                if (oldLocation == null) {
                    oldLocation = it
                    maxSpeed = it.speed
                    minSpeed = it.speed
                    maxAlt = it.altitude
                    minAlt = it.altitude
                }
                totalDistance += distanceBetweenTwoLocations(oldLocation, it)
                oldLocation = it
                if (it.speed > maxSpeed) {
                    maxSpeed = it.speed
                }
                if (it.speed < minSpeed) {
                    minSpeed = it.speed
                }
                speedSum += it.speed

                if (it.altitude > maxAlt) {
                    maxAlt = it.altitude
                }
                if (it.altitude < minAlt) {
                    minAlt = it.altitude
                }
                altSum += it.altitude
            }
            return LocationStatisticsData(
                maxSpeed,
                minSpeed,
                speedSum / locations.size,
                BigDecimal(totalDistance),
                maxAlt,
                minAlt,
                altSum / locations.size
            )
        }
    }
}