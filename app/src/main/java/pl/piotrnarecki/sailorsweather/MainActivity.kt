package pl.piotrnarecki.sailorsweather

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {


    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)




        if (!isLocationEnabled()) {

            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        } else {


            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            // TODO (STEP 7: Call the location request function here.)
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "You have denied location permission. Please allow it is mandatory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()


        }

//        CallAPILoginAsyncTask("User", "Password").execute()

    }


    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")


            getLocationWeatherDetails()

        }
    }


    private fun getLocationWeatherDetails() {

        if (Constants.isNetworkAvailable(context = this)) {
            Toast.makeText(
                this@MainActivity,
                "You have connected to the internet",
                Toast.LENGTH_SHORT
            ).show()
        }else{

            Toast.makeText(
                this@MainActivity,
                "No internet connection available",
                Toast.LENGTH_SHORT
            ).show()


        }


    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }


    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )


    }


    private inner class CallAPILoginAsyncTask(val username: String, val password: String) :
        AsyncTask<Any, Void, String>() {

        private lateinit var customProgressDialog: Dialog


        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()


        }


        override fun doInBackground(vararg p0: Any?): String {

            var result: String

            var connection: HttpURLConnection? = null

            try {
                val url = URL("https://run.mocky.io/v3/3bc458ab-619d-47ad-9898-346586ea0cb6")

                connection = url.openConnection() as HttpURLConnection

                connection.doInput = true;
                connection.doOutput = true;


                //  przesylanie na serwer
                connection.instanceFollowRedirects = false

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.useCaches = false

                val writeOutputStream = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put(password, password)

                writeOutputStream.writeBytes(jsonRequest.toString())

                writeOutputStream.flush()
                writeOutputStream.close()


                //
                val httpResults: Int = connection.responseCode

                if (httpResults == HttpURLConnection.HTTP_OK) {

                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()

                    var line: String?

                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")

                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {

                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }


                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"

            } catch (e: Exception) {
                result = "Error: ${e.message}"

            } finally {
                connection?.disconnect()
            }

            return result
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            cancelProgressDialog()


            if (result != null) {
                Log.i("JSON RESPONSE RESULT", result)
            }


            // nowe, z uzyciem GSON
            val responeData = Gson().fromJson(result, ResponseData::class.java)


            // stare podejscie
            val jsonObject = JSONObject(result)
            val name = jsonObject.optString("name")


        }

        private fun showProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)

            customProgressDialog.setContentView(R.layout.dialog_custom_progress)

            customProgressDialog.show()

        }


        private fun cancelProgressDialog() {
            customProgressDialog.dismiss()

        }


    }


}