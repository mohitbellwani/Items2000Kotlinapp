package com.example.itemlistof2000

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private val client: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        exec("https://db.ezobooks.in/kappa/image/task", recyclerView)
    }

    private fun exec(url: String, recyclerView: RecyclerView) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RequestFailure", "Failed to execute request: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val strResponse = response.body?.string()
                Log.d("Response", strResponse.toString()) // Add this line to log the response

                if (strResponse != null) {
                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(strResponse)
                            val jsonArray = jsonResponse.getJSONArray("items")

                            val itemList = mutableListOf<Item>()

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val itemName = jsonObject.getString("itemName")
                                val itemPrice = "$" + jsonObject.getInt("itemPrice")
                                val url = jsonObject.getString("url")

                                val item = Item(itemName, itemPrice, url)
                                itemList.add(item)
                            }

                            val adapter = ItemAdapter(itemList)
                            recyclerView.adapter = adapter
                        } catch (e: JSONException) {
                            Log.e("JSONException", e.toString()) // Add this line to log any JSON parsing errors
                            onFailure(call, IOException("Error parsing JSON response"))
                        }
                    }
                } else {
                    onFailure(call, IOException("Empty response body"))
                }
            }
        })
        client.dispatcher.executorService.shutdown()
    }
}
