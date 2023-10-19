package com.example.uatu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var charList: MutableList<String>
    private lateinit var rvChars: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCharURL()

        charList = mutableListOf()
        rvChars = findViewById(R.id.rv_char)
    }

    private fun getCharURL() {
        val client = AsyncHttpClient()

        val pubKey = "32c0bd2ee2eeb90d17f8528ed5bf89c8"
        val pvtKey = "62bb70ab3925d53d663174ece3fddc9db311f786"
        val input = "1$pvtKey$pubKey"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val hash = bytes.joinToString("") { "%02x".format(it)}

        client["https://gateway.marvel.com/v1/public/characters?ts=1&apikey=$pubKey&hash=$hash",
                object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                        Log.d("Char Success", "$json")
                        val data = json.jsonObject.getJSONObject("data")

                        val charImageArray = data.getJSONArray("results")

                        for (i in 0 until charImageArray.length()) {
                            val characterObject = charImageArray.getJSONObject(i)
                            val thumbnailObject = characterObject.getJSONObject("thumbnail")
                            val imagePath = thumbnailObject.getString("path")
                            val imageExtension = thumbnailObject.getString("extension")
                            val imageUrl = "$imagePath.$imageExtension"
                            charList.add(imageUrl)
                        }
                        val adapter = CharAdapter(charList)
                        rvChars.adapter = adapter
                        rvChars.layoutManager = GridLayoutManager(this@MainActivity, 2)
                        rvChars.addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        errorResponse: String,
                        throwable: Throwable?
                    ) {
                        Log.d("Char Error", errorResponse)
                    }

                }]
    }
}