package com.example.takeeat.ui.recipe

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.bumptech.glide.Glide
import com.example.takeeat.R
import com.example.takeeat.RecipeItem
import com.example.takeeat.databinding.ActivityRecipedetailBinding
import com.example.takeeat.ui.refrigerator.RefItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRecipedetailBinding
    private lateinit var recipeItem : RecipeItem
    private lateinit var ingreAdapter : RecipeDetailIngreAdapter
    private lateinit var recipeStepAdapter : RecipeStepAdapter
    var writerbookmarked = false
    var recipebookmarked = false
    var inMyRefItem = ArrayList<RefItem>()
    var tagList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recipeItem = intent.getSerializableExtra("Recipe_Data") as RecipeItem
        inMyRefItem = intent.getSerializableExtra("InMyRef") as ArrayList<RefItem>
        if(inMyRefItem.size!=0){
            for (i in inMyRefItem) {
                if(!tagList.contains(i.itemtag))
                    tagList.add(i.itemtag!!)
            }
        }
        ingreAdapter = RecipeDetailIngreAdapter(recipeItem.recipeIngredients)
        ingreAdapter.inMyRef = inMyRefItem
        Log.d("Responsee : detailac taglist : ",tagList.toString())
        ingreAdapter.refTag = tagList

        binding = ActivityRecipedetailBinding.inflate(layoutInflater)
        val actionBar = supportActionBar
        actionBar!!.hide()

        Glide.with(this).load(recipeItem.imgURL).into(binding.recipedetailMainImage)
        binding.recipedetailRecipeName.text = recipeItem.recipeName
        binding.recipedetailRecipeWriter.text = recipeItem.recipeWriter
        binding.recipedetailRecipeSummary.text = recipeItem.recipeSummary

        binding.recipedetailIngreRecyclerView.adapter = ingreAdapter
        binding.recipedetailDifficultyText.text = recipeItem.recipeDifficulty
        binding.recipedetailTimeText.text = recipeItem.recipeTime
        binding.recipedetailAmount.text = recipeItem.recipeServing + "인분"
        binding.recipedetailRating.text = String.format("%.1f", recipeItem.recipeRating)
        val recipeViewPager = binding.recipedetailRecipeStepViewPager
        if(recipeItem.recipeStep!=null) {
            recipeStepAdapter = RecipeStepAdapter(recipeItem.recipeStep!!)
            recipeStepAdapter.setRecipeIngre(recipeItem.recipeIngredients)
            recipeStepAdapter.recipeID = recipeItem.recipeId
            recipeStepAdapter.inMyRef=inMyRefItem
            recipeViewPager.adapter = recipeStepAdapter

        }

        bookmark_bool()
        Log.d("Response","제료"+recipeItem.recipeIngredients.toString())

        binding.recipedetailRecipeBookmark.setOnClickListener {
            //이거 누르면 레시피 구독 아래 코드를 태스크 핸들러에 넣으면 될듯 합니다
            if (recipebookmarked) {
                binding.recipedetailRecipeBookmark.setImageResource(R.drawable.ic_baseline_bookmark_border_24)
                bookmark_off(recipeItem.recipeId)
                Toast.makeText(this, "북마크에서 삭제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                binding.recipedetailRecipeBookmark.setImageResource(R.drawable.ic_baseline_bookmark_24)
                bookmark_on(recipeItem.recipeId)
                Toast.makeText(this, "북마크에 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
            recipebookmarked = !recipebookmarked
        }

        binding.recipedetailWriterBookmark.setOnClickListener{
            //이거 누르면 작성자 구독 아래 코드를 태스크 핸들러에 넣으면 될듯 합니다
            if (writerbookmarked) {
                binding.recipedetailWriterBookmark.setImageResource(R.drawable.ic_baseline_bookmark_border_24)
                bookmark_off(recipeItem.recipeWriter!!)
                Toast.makeText(this, "북마크에서 삭제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                binding.recipedetailWriterBookmark.setImageResource(R.drawable.ic_baseline_bookmark_24)
                bookmark_on(recipeItem.recipeWriter!!)
                Toast.makeText(this, "북마크에 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
            writerbookmarked = !writerbookmarked
        }
        setContentView(binding.root)

    }



    fun bookmark_on(bookmarkId: String) {
        val handler = Handler()
        Thread(Runnable {

            val url: URL =
                URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/recipe/bookmark_on")
            var conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)


            var job = JSONObject()
            job.put("user_id", AWSMobileClient.getInstance().username)
            job.put("bookmark_id", URLEncoder.encode(bookmarkId, "UTF-8"))
            job.put("bookmark", "true")


            var requestBody = job.toString()
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()

            Log.d("Responseeee : code = ", conn.responseCode.toString())

            conn.disconnect()



        }).start()
    }

    fun bookmark_off(bookmarkId: String) {
        Thread(Runnable {

            val url: URL =
                URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/recipe/bookmark_off")
            var conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)


            var job = JSONObject()
            job.put("user_id", AWSMobileClient.getInstance().username)
            job.put("bookmark_id", URLEncoder.encode(bookmarkId, "UTF-8"))


            var requestBody = job.toString()
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()

            Log.d("Responseeee : code = ", conn.responseCode.toString())

            conn.disconnect()


        }).start()
    }


    fun bookmark_bool() {
        val handler = Handler()

        Thread(Runnable {

            val url: URL = URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/recipe/bookmark_get")
            var conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)

            var job = JSONObject()
            job.put("user_id", AWSMobileClient.getInstance().username)

            var requestBody = job.toString()
            Log.d("Response : requestBody = ", requestBody)
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()


            val streamReader = InputStreamReader(conn.inputStream)
            val buffered = BufferedReader(streamReader)
            val content = StringBuilder()
            while(true) {
                val line = buffered.readLine() ?: break
                content.append(line)
            }
            val data =content.toString()
            val jsonArr = JSONArray(data)

            for (i in 0 until jsonArr.length()) {
                val jsonObj = jsonArr.getJSONObject(i)
                val bmi = jsonObj.getString("bookmark_id").toString()

                if(bmi == recipeItem.recipeWriter!!){
                    writerbookmarked = true
                    Log.d("Responseeee user : ", "true")
                }
                if(bmi == recipeItem.recipeId){
                    recipebookmarked = true
                    Log.d("Responseeee id : ", "true")
                }
            }

            handler.post{
                if(writerbookmarked == true){
                    binding.recipedetailWriterBookmark.setImageResource(R.drawable.ic_baseline_bookmark_24)
                }
                if(recipebookmarked == true){
                    binding.recipedetailRecipeBookmark.setImageResource(R.drawable.ic_baseline_bookmark_24)

                }
            }

            conn.disconnect()

        }).start()

    }
}