package com.example.rutasapp_compose

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.rutasapp_compose.ui.theme.RutasApp_ComposeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.model.polylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.rutasapp_compose.Feature

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // accion cuando se aceptan los permisos
        } else {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //accion cuando se acepte
        }else{
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        setContent {
            RutasApp_ComposeTheme {
                var start by remember { mutableStateOf<LatLng?>(null) }
                var end by remember { mutableStateOf<LatLng?>(null) }
                var polylineOptions by remember { mutableStateOf<List<LatLng>>(emptyList()) }

                val initialLocation = LatLng(19.432608, -99.133209) // Ciudad de México
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
                }

                LaunchedEffect(key1 = cameraPositionState)  {
                    getCurrentLocation { currentLatLong ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLong, 12f)
                        Toast.makeText(this@MainActivity, "Ubicacion Actual: $currentLatLong", Toast.LENGTH_SHORT).show()

                    }
                }

                Column (
                    modifier = Modifier .fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Button(
                        onClick = {
                            start = null
                            end = null
                            polylineOptions = emptyList()

                            Toast.makeText(this@MainActivity, " Selecciona origen y destino", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "Seleccionar ruta")
                    }
                    Button(
                        onClick = {
                            getCurrentLocation { currentLatLong ->
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(currentLatLong, 12f)
                            }
                        }
                    ) {
                        Text(text = "Mi ubicacion centrada")
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState,
                        onMapClick = {
                            latLng -> when{
                                start == null -> start = latLng
                                end == null -> {
                                    end = latLng

                                    createRoute(start, end) { route ->
                                        polylineOptions = route
                                    }
                                }
                            }
                        }
                    ){
                        start?.let{
                            Marker(
                                state = MarkerState(position = it)
                            )
                        }
                        end?.let{
                            Marker(
                                state = MarkerState(position = it)
                            )
                        }

                        Polyline(points = polylineOptions)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(onLocationReceived: (LatLng) -> Unit){
        fusedLocationClient.lastLocation.addOnCompleteListener(this) {
            task -> if (task.isSuccessful && task.result != null){
                val currentLocation = task.result
            val currentLatLong = LatLng(currentLocation.latitude, currentLocation.longitude)
            onLocationReceived(currentLatLong)
        }else{
            Toast.makeText(this, "No se pudo obtener la ubicacion actual", Toast.LENGTH_SHORT).show()
        }
        }
    }

    fun createRoute(
        start: LatLng?,
        end: LatLng?,
        onRouteCreated: (List<LatLng>) -> Unit){
if (start != null && end != null){
    CoroutineScope(Dispatchers.IO).launch {
        val call = RetrofitClient.retrofit.getRoute(
            Constantes.API_KEY,
            "${start.longitude},${start.latitude}",
            "${end.longitude},${end.latitude}"

        )

       /* if (call.isSuccessful){
            val routeResponse = call.body()
            val polylinePoints = mutableListOf<LatLng>()

            routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
                polylinePoints.add(LatLng(it[1], it[0]))
            }
            onrouteCreated(polylinePoints)
        }else{

        }*/
    }
}
    }
}

