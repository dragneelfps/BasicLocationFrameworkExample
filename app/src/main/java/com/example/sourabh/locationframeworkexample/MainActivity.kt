package com.example.sourabh.locationframeworkexample

import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager

    private lateinit var locationListener: LocationListener

    private val minDistance = 0f
    private val minTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        if(hasPermssion()){
            startListening()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startListening()
        }
    }

    fun hasPermssion(): Boolean{
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun init(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                location?.let {
                    latitude.text = it.latitude.toString()
                    longitude.text = it.longitude.toString()
                    if(it.hasAltitude()){
                        altitude.text = parseAltitude(it)
                    }else{
                        altitude.text = "Unavailable"
                    }
                    if(it.hasSpeed()){
                        speed.text = parseSpeed(it)
                    }else{
                        speed.text = "Unavailable"
                    }
                    if(hasSdk(18)){
                        if(it.isFromMockProvider){
                            isMocked.text = "Yes"
                        }else{
                            isMocked.text = "No"
                        }
                    }else{
                        isMocked.text = "Unvailable"
                    }
                    time.text = it.time.toString()
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d("xyz", "Status changed to " + when(status){
                    LocationProvider.OUT_OF_SERVICE -> "Out of service"
                    LocationProvider.AVAILABLE -> "available"
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> "Temp unavailable"
                    else -> "Error"
                })
            }

            override fun onProviderEnabled(provider: String?) {
                Log.d("xyz", "Provide Enabled: $provider")
            }

            override fun onProviderDisabled(provider: String?) {
                Log.d("xyz","Provider disable: $provider")
            }
        }
    }

    fun startListening(){
        setNmeaListener()
        locationManager.requestLocationUpdates(getBestProvider(), minTime, minDistance, locationListener)
    }

    private fun parseSpeed(location: Location): String {
        var speed = location.speed.toString()
        if(hasSdk(26)) {
            if (location.hasSpeedAccuracy()) {
                speed = "$speed / ${location.speedAccuracyMetersPerSecond}"
            }
        }
        return speed
    }

    private fun parseAltitude(location: Location): String? {
        var alt = location.altitude.toString()
        if(hasSdk(26)) {
            if (location.hasVerticalAccuracy()) {
                alt = "$alt / ${location.verticalAccuracyMeters}"
            }
        }
        return alt
    }

    private fun hasSdk(requiredSdkVersion: Int): Boolean {
        return android.os.Build.VERSION.SDK_INT >= requiredSdkVersion
    }

    private lateinit var nmeaMessageListener: OnNmeaMessageListener

    private lateinit var nmeaListener: GpsStatus.NmeaListener

    fun setNmeaListener(){
        Log.d("xyz","Hello There")
        fun setNmea(message: String){
            nmea.text = message
        }
        if(hasSdk(24)){
            nmeaMessageListener = OnNmeaMessageListener { message, timestamp ->
                setNmea(message)
            }
            locationManager.addNmeaListener(nmeaMessageListener)
        }else{
            nmeaListener = GpsStatus.NmeaListener { timestamp, nmea ->
                setNmea(nmea)
            }
            locationManager.addNmeaListener(nmeaListener)
        }
    }

    fun isNetorkProviderEnable(): Boolean{
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getBestProvider(): String{
        return when(isNetorkProviderEnable()){
            true -> LocationManager.NETWORK_PROVIDER
            false -> LocationManager.GPS_PROVIDER
        }
    }

    fun Location?.string(): String{
        return this?.let { "Lat: ${this.latitude} Long: ${this.longitude} Altitude: ${this.altitude} Provider: ${this.provider}" } ?: ""
    }

    override fun onStop() {
        super.onStop()
        if(hasSdk(24)) {
            locationManager.removeNmeaListener(nmeaMessageListener)
        }else{
            locationManager.removeNmeaListener(nmeaListener)
        }
        locationManager.removeUpdates(locationListener)
    }

}
