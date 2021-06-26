package pl.piotrnarecki.sailorsweather

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (!isLocationEnabled()) {

            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        } else {

            Toast.makeText(
                this,
                "Your location provider is already turned on.",
                Toast.LENGTH_SHORT
            ).show()


        }

//        CallAPILoginAsyncTask("User", "Password").execute()

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