package com.example.uatu

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONArray
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    private lateinit var charList: MutableList<String>
    private lateinit var charNames: MutableList<String>
    private lateinit var charDescs: MutableList<String>
    private lateinit var rvChars: RecyclerView
    private lateinit var editText: EditText
    private lateinit var button: Button
    private var offset = 0
    private val limit = 20
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.title = "Uatu The Watcher's Codex"
        }

        getCharURL()

        charList = mutableListOf()
        charNames = mutableListOf()
        charDescs = mutableListOf()
        rvChars = findViewById(R.id.rv_char)
        editText = findViewById(R.id.search_field)
        button = findViewById(R.id.button)

        button.setOnClickListener {
            val searchText = editText.text.toString()
            if(searchText == ""){
                getCharURL()
            } else {
                search(searchText)
            }
        }

    }

    private fun getCharURL() {
        val client = AsyncHttpClient()

        val fields = getFields()

        client["https://gateway.marvel.com/v1/public/characters?$fields",
                object : JsonHttpResponseHandler() {
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Headers,
                        json: JsonHttpResponseHandler.JSON
                    ) {
                        isLoading = true
                        Log.d("Char Success", "$json")
                        val data = json.jsonObject.getJSONObject("data")

                        val charImageArray = data.getJSONArray("results")

                        success(charImageArray, charList, charNames, charDescs)

                        val adapter = CharAdapter(charList, charNames, charDescs)
                        rvChars.adapter = adapter
                        val layoutManager = LinearLayoutManager(this@MainActivity)
                        rvChars.layoutManager = layoutManager
                        val itemDecoration = DividerItemDecoration(
                            this@MainActivity,
                            DividerItemDecoration.VERTICAL
                        )
                        rvChars.addItemDecoration(itemDecoration)

                        isLoading = false
                        endOfScroll(layoutManager)
                        adapter.notifyItemRangeInserted(
                            charList.size -
                                    charImageArray.length(), charImageArray.length()
                        )
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

    private fun success(
        charImageArray: JSONArray, cList: MutableList<String>,
        cName: MutableList<String>, cDesc: MutableList<String>
    ) {

        for (i in 0 until charImageArray.length()) {
            val characterObject = charImageArray.getJSONObject(i)

            val thumbnailObject = characterObject.getJSONObject("thumbnail")
            val imagePath = thumbnailObject.getString("path")
            val imageExtension = thumbnailObject.getString("extension")
            val imageUrl = "$imagePath.$imageExtension"
            val iDesc = characterObject.getString("description")
            cList.add(imageUrl)

            cName.add(characterObject.getString("name"))
            if (iDesc != "") {
                cDesc.add(iDesc)
            } else {
                cDesc.add("No description provided")
            }
        }
    }

    private fun getFields(): String {
        val pubKey = "32c0bd2ee2eeb90d17f8528ed5bf89c8"
        val pvtKey = "62bb70ab3925d53d663174ece3fddc9db311f786"
        val input = "1$pvtKey$pubKey"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val hash = bytes.joinToString("") { "%02x".format(it) }

        return "ts=1&apikey=$pubKey&hash=$hash&offset=$offset&limit=$limit"
    }

    private fun endOfScroll(layoutManager: LinearLayoutManager) {
        rvChars.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading) { // Check if a loading operation is not already in progress
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        // This condition is met when the end of the list is reached
                        loadMoreData() // Load more data
                    }
                }
            }
        })
    }

    private fun loadMoreData() {
        offset += limit
        getCharURL()
    }

    private fun search(searchText: String) {

        val client = AsyncHttpClient()
        val fields = getFields()

        client["https://gateway.marvel.com/v1/public/characters?nameStartsWith=$searchText&$fields",
                object : JsonHttpResponseHandler() {
                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        errorResponse: String,
                        throwable: Throwable?
                    ) {
                        Log.d("Char Error", errorResponse)
                    }

                    override fun onSuccess(
                        statusCode: Int,
                        headers: Headers,
                        json: JsonHttpResponseHandler.JSON
                    ) {
                        Log.d("Char Success", "$json")
                        val data = json.jsonObject.getJSONObject("data")

                        val charImageArray = data.getJSONArray("results")
                        var cList: MutableList<String> = mutableListOf()
                        var cName: MutableList<String> = mutableListOf()
                        var cDesc: MutableList<String> = mutableListOf()

                        success(charImageArray, cList, cName, cDesc)

                        val adapter = CharAdapter(cList, cName, cDesc)
                        rvChars.adapter = adapter
                        val layoutManager = LinearLayoutManager(this@MainActivity)
                        rvChars.layoutManager = layoutManager
                        rvChars.addItemDecoration(
                            DividerItemDecoration(
                                this@MainActivity,
                                DividerItemDecoration.VERTICAL
                            )
                        )
                    }

                }
        ]

    }

}