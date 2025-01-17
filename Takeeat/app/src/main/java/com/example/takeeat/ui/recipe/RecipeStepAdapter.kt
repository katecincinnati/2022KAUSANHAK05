package com.example.takeeat.ui.recipe

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.client.AWSMobileClient
import com.bumptech.glide.Glide
import com.example.takeeat.IngredientsInfo
import com.example.takeeat.R
import com.example.takeeat.RecipeProcess
import com.example.takeeat.ui.refrigerator.RefItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class RecipeStepAdapter(val data: ArrayList<RecipeProcess>): RecyclerView.Adapter<RecipeStepAdapter.PagerViewHolder>() {
    public var recipeID : String = "1"
    var inMyRef = ArrayList<RefItem>()
    lateinit var ingreList : ArrayList<IngredientsInfo>
    class PagerViewHolder(parent : ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recipestep, parent, false)){
        val stepText : TextView
        val explanation : TextView
        val image : ImageView
        val askRatingText : TextView
        val ratingBar : RatingBar
        val commitButton : Button
        val ratingText : TextView
        init{
            stepText = itemView.findViewById((R.id.recipestep_stepText))
            explanation = itemView.findViewById(R.id.recipestep_explanation)
            image = itemView.findViewById(R.id.recipestep_image)
            askRatingText = itemView.findViewById(R.id.recipestep_askRatingText)
            ratingText = itemView.findViewById(R.id.recipestep_ratingText)
            ratingBar = itemView.findViewById(R.id.recipestep_ratingBar)
            commitButton = itemView.findViewById(R.id.recipestep_ratingCommitButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Log.d("ResponseStep",position.toString())
        if(holder.absoluteAdapterPosition==data.size) {
            holder.stepText.visibility  = View.INVISIBLE
            holder.explanation.visibility  = View.INVISIBLE
            holder.image.visibility  = View.INVISIBLE
            holder.ratingText.visibility = View.VISIBLE
            holder.ratingText.text = holder.ratingBar.rating.toString()
            holder.askRatingText.visibility = View.VISIBLE
            holder.ratingBar.visibility = View.VISIBLE
            holder.ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
                holder.ratingText.text = fl.toString()
            }
            holder.commitButton.visibility = View.VISIBLE
            holder.commitButton.setOnClickListener {
                //여기 별점 db반영코드  holder.ratingBar.rating

                val rate = holder.ratingBar.rating
                post_recipe_rating(rate)
                recipe_rating_update(rate)
                post_recipe_recommend(rate)
                //get_recom_item(rate)


                val dialogBuilder = AlertDialog.Builder(holder.commitButton.context)
                dialogBuilder.setMessage("요리에 사용한 재료를 냉장고에 반영하시겠습니까?")
                dialogBuilder.setPositiveButton("예",DialogInterface.OnClickListener { dialogInterface, i ->
                    val intent = Intent(holder.commitButton.context, SubtractRefActivity::class.java)
                    val ingreListToSubtract : ArrayList<IngredientsInfo> = ArrayList<IngredientsInfo>()
                    for(ingre in ingreList){
                        if(ingre.ingreCount != null){
                            ingreListToSubtract.add(ingre)
                        }
                    }

                    if(inMyRef.size!=0) {
                        intent.putExtra("inMyRef", inMyRef)
                        intent.putExtra("Ingre_Data", ingreListToSubtract)
                        holder.commitButton.context.startActivity(intent)
                    }
                    else Toast.makeText(holder.commitButton.context, "냉장고에 일치하는 품목이 없습니다.", Toast.LENGTH_SHORT).show();
                })
                dialogBuilder.setNegativeButton("아니오",DialogInterface.OnClickListener { dialogInterface, i ->
                    Toast.makeText(holder.commitButton.context, "취소되었습니다", Toast.LENGTH_SHORT).show();
                })
                val alertDialog = dialogBuilder.create()
                alertDialog.show()

            }

        }
        else{

            data.get(position).let { item ->
                with(holder) {
                    holder.stepText.visibility  = View.VISIBLE
                    holder.explanation.visibility  = View.VISIBLE
                    holder.image.visibility  = View.VISIBLE
                    holder.ratingText.visibility = View.INVISIBLE
                    holder.askRatingText.visibility = View.INVISIBLE
                    holder.ratingBar.visibility = View.INVISIBLE
                    holder.commitButton.visibility = View.INVISIBLE
                    Log.d("ResponseStep","1")
                    stepText.text =
                        "Step " + (this.absoluteAdapterPosition + 1).toString() + "/" + (itemCount-1).toString()
                    explanation.text = item.recipeExplanation
                    Glide.with(holder.image.context).load(item.imgURL).into(image)
                }
            }



        }

    }

    override fun getItemCount(): Int {
        return data.size+1
    }

    fun setRecipeIngre(ingrelist:ArrayList<IngredientsInfo>){
        ingreList = ingrelist
    }

    fun post_recipe_rating(rate:Float){
        Thread(Runnable{

            val url: URL = URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/recipe/rating_post")
            var conn: HttpURLConnection =url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection","keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)


            var job = JSONObject()
            job.put("user_id", AWSMobileClient.getInstance().username)
            job.put("recipe_id", recipeID)
            job.put("rating", rate)


            var requestBody = job.toString()
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()

            Log.d("ResponseRating",conn.responseCode.toString())
            conn.disconnect()

        }).start()
    }

    fun recipe_rating_update(rate:Float){
        Thread(Runnable{
            val url: URL = URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/recipe/rating_update")
            var conn: HttpURLConnection =url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection","keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)


            var job = JSONObject()
            job.put("user_id", AWSMobileClient.getInstance().username)
            job.put("recipe_id", recipeID)
            job.put("rating", rate)


            var requestBody = job.toString()
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()
            Log.d("ResponseRating",conn.responseCode.toString())

            conn.disconnect()

        }).start()
    }

    fun post_recipe_recommend(rate:Float){
        Thread(Runnable{
            val url: URL = URL("http://52.78.228.196/recom")
            var conn: HttpURLConnection =url.openConnection() as HttpURLConnection
            conn.setUseCaches(false)
            conn.setRequestMethod("PUT")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Connection","keep-alive")
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoOutput(true)
            conn.setDoInput(true)

            var job = JSONObject()
            job.put("user_name", AWSMobileClient.getInstance().username)
            job.put("recipe_id", recipeID)
            job.put("rating", rate)

            Log.d("Responseeee : code = name", AWSMobileClient.getInstance().username)
            Log.d("Responseeee : code = id", recipeID)
            Log.d("Responseeee : code = rate", rate.toString())

            var requestBody = job.toString()
            val wr = DataOutputStream(conn.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()

            val streamReader = InputStreamReader(conn.inputStream)
            val buffered = BufferedReader(streamReader)
            val content = StringBuilder()
            while (true) {
                val line = buffered.readLine() ?: break
                content.append(line)
            }
            val data = content.toString()

            Log.d("Responseeee : code = get   :  ", data)

            buffered.close()

            Log.d("Responseeee : code = get", conn.responseCode.toString())

            conn.disconnect()



        }).start()
    }

}