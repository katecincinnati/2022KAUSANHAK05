package com.example.takeeat.ui.refrigerator

import RefrigeratorAdapter
import RefrigeratorIconAdapter
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amazonaws.mobile.client.AWSMobileClient
import com.example.takeeat.*
import com.example.takeeat.R
import com.example.takeeat.databinding.FragmentRefrigeratorBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class RefrigeratorFragment : Fragment() {

    private var _binding: FragmentRefrigeratorBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mainFab:FloatingActionButton
    lateinit var directFab:FloatingActionButton
    lateinit var galleryFab:FloatingActionButton
    lateinit var cameraFab:FloatingActionButton
    lateinit var directText:TextView
    lateinit var galleryText:TextView
    lateinit var cameraText:TextView
    lateinit var currentPhotoPath: String
    //lateinit var refrigeratorSearch: SearchView
    lateinit var refrigeratorSwitch: Switch
    lateinit var refrigeratorSwitchLabel1: TextView
    lateinit var refrigeratorSwitchLabel2: TextView
    lateinit var refrigeratorSortButton: ImageButton
    lateinit var refrigeratorItemTypes: ConstraintLayout
    lateinit var refrigeratorDevider: View
    lateinit var refDB: RefItemAppDatabase
    //lateinit var testbutton: Button


    var isFabOpen = false
    val CAMERA = arrayOf(Manifest.permission.CAMERA)
    val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val CAMERA_CODE = 98
    val STORAGE_CODE = 99
    val GALLERY_CODE = 200
    var ocrApiGwUrl:String = BuildConfig.OCR_API_GW_URL
    var ocrSecretKey:String = BuildConfig.OCR_SECRETKEY

    lateinit var itemTestList: ArrayList<RefItem>
    var filteredTestList: MutableList<RefItem> = listOf<RefItem>().toMutableList()
    private val fetchList = ArrayList<RefItem>().apply {
        add(RefItem("로딩중...", null,Date(2022,3,25),0,"개", null))
    }

    val linearLayoutManager = LinearLayoutManager(context)
    val gridLayoutManager = GridLayoutManager(context,4)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val refrigeratorViewModel =
            ViewModelProvider(this).get(RefrigeratorViewModel::class.java)

        _binding = FragmentRefrigeratorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.refrigeratorrecyclerview
        //혹시 다른 리스트로 만들어서 붙이실꺼면 타입만 ArrayList<RefItem>에 맞게 아래 어댑터에 붙이시면 됩니다

        recyclerView.layoutManager = linearLayoutManager


        //getDB(recyclerView)
        refDB = context?.let { RefItemAppDatabase.getDatabase(it) }!!


        //filteredTestList = itemTestList.clone() as MutableList<RefItem>
        //recyclerView.adapter = RefrigeratorAdapter(filteredTestList)

        //refrigeratorSearch = binding.refrigeratorSearch
        refrigeratorSwitch = binding.refrigeratorSwitch
        refrigeratorSwitchLabel1 = binding.refrigeratorTextView1
        refrigeratorSwitchLabel2 = binding.refrigeratorTextView2
        refrigeratorSortButton = binding.refrigeratorSortButton
        refrigeratorItemTypes = binding.refrigItemTypes
        refrigeratorDevider = binding.refrigDivider1
        //testbutton = binding.testbutton


        mainFab= binding.refrigeratorfab
        directFab = binding.directSubFab
        galleryFab = binding.gallerySubFab
        cameraFab = binding.cameraSubFab
        directText = binding.textDirect
        galleryText = binding.textGallery
        cameraText = binding.textCamera
        val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        refrigeratorSwitch.isChecked = false

        mainFab.setOnClickListener (View.OnClickListener {
            if(isFabOpen){
                mainFab.startAnimation(rotateForward)
                directFab.startAnimation(fabClose)
                galleryFab.startAnimation(fabClose)
                cameraFab.startAnimation(fabClose)
                directText.startAnimation(fabClose)
                galleryText.startAnimation(fabClose)
                cameraText.startAnimation(fabClose)
                directFab.hide()
                directText.isVisible = false
                directFab.isClickable = false
                galleryFab.hide()
                galleryText.isVisible = false
                galleryFab.isClickable = false
                cameraFab.hide()
                cameraText.isVisible = false
                cameraFab.isClickable = false
                isFabOpen=false
            }
            else{
                mainFab.startAnimation(rotateBackward)
                directFab.startAnimation(fabOpen)
                galleryFab.startAnimation(fabOpen)
                cameraFab.startAnimation(fabOpen)
                directText.startAnimation(fabOpen)
                galleryText.startAnimation(fabOpen)
                cameraText.startAnimation(fabOpen)
                directFab.show()
                directText.isVisible = true
                directFab.isClickable = true
                galleryFab.show()
                galleryText.isVisible = true
                galleryFab.isClickable = true
                cameraFab.show()
                cameraText.isVisible = true
                cameraFab.isClickable = true
                isFabOpen=true

            }
        })
        directFab.setOnClickListener (View.OnClickListener {
            val intent = Intent(getActivity(), AddRefrigeratorActivity::class.java)
            var nonOcr = ArrayList<RefItem>()
            nonOcr.add(RefItem(null,null,null,null,null, null))
            intent.putExtra("OCR_RESULT",nonOcr)
            startActivity(intent)
        })
        galleryFab.setOnClickListener (View.OnClickListener {
            callImage(GALLERY_CODE)
        })

        cameraFab.setOnClickListener (View.OnClickListener {
            callImage(CAMERA_CODE)
        })

        refrigeratorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                recyclerView.layoutManager = gridLayoutManager
                recyclerView.adapter = RefrigeratorIconAdapter(filteredTestList)
                refrigeratorItemTypes.visibility = View.GONE
                refrigeratorDevider.visibility = View.GONE

            } else {
                recyclerView.layoutManager = linearLayoutManager
                recyclerView.adapter = RefrigeratorAdapter(filteredTestList)
                refrigeratorItemTypes.visibility = View.VISIBLE
                refrigeratorDevider.visibility = View.VISIBLE
            }
        }

        refrigeratorSortButton.setOnClickListener(View.OnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("분류 기준을 선택해주세요")
            val sortTypes: Array<String> = listOf("이름 (오름순)","이름 (내림순)","유통기한 (오름순)","유통기한 (내림순)","개수 (오름순)","개수 (내림순)").toTypedArray()
            builder.setItems(sortTypes, DialogInterface.OnClickListener{dialog, index ->
                val newTestList: MutableList<RefItem> = listOf<RefItem>().toMutableList()
                for(x in fetchList) newTestList.add(x)
                when(index)
                {
                    0 -> newTestList.sortBy { it.itemname }
                    1 -> newTestList.sortByDescending { it.itemname }
                    2 -> newTestList.sortBy { it.itemexp }
                    3 -> newTestList.sortByDescending { it.itemexp }
                    4 -> newTestList.sortBy { it.itemamount }
                    5 -> newTestList.sortByDescending { it.itemamount }
                }
                if(!newTestList.isNullOrEmpty()) {
                    filteredTestList.clear()
                    for(x in newTestList) filteredTestList.add(x)

                    if(recyclerView.layoutManager == linearLayoutManager){
                        //recyclerView.adapter = RefrigeratorAdapter(filteredTestList)
                        recyclerView.swapAdapter(RefrigeratorAdapter(filteredTestList),true)
                    }
                    else if(recyclerView.layoutManager == gridLayoutManager){
                        //recyclerView.adapter = RefrigeratorIconAdapter(filteredTestList)
                        recyclerView.swapAdapter(RefrigeratorIconAdapter(filteredTestList),true)
                    }
                }
            })
            val alertDialog = builder.create()
            alertDialog.show()
        })





        setHasOptionsMenu(true)


        return root
    }


    override fun onStart(){
        super.onStart()
        //refrigeratorSearch.setQuery("", false)
        //refrigeratorSearch.isIconified = true
        getDB(binding.refrigeratorrecyclerview)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.removeItem(R.id.app_bar_search_refrigerator)
        menu.removeItem(R.id.app_bar_search_recipe)
        menu.removeItem(R.id.app_bar_search_myrecipe)
        menu.removeItem(R.id.cart_button)
        menu.removeItem(R.id.notification_button)
        inflater.inflate(R.menu.search_menu, menu)

        val searchButtonRecipe = menu.findItem(R.id.app_bar_search_recipe)
        val searchButtonMyrecipe = menu.findItem(R.id.app_bar_search_myrecipe)
        val refrigeratorSearch = menu.findItem(R.id.app_bar_search_refrigerator)
        searchButtonRecipe.isVisible = false
        searchButtonMyrecipe.isVisible = false

        menu.findItem(R.id.cart_button).setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {

            val shoppingintent: Intent = Intent(context, ShoppingListActivity::class.java)
            startActivity(shoppingintent)
            true
        })

        menu.findItem(R.id.notification_button).setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {

            val notificationintent: Intent = Intent(context, NotificationActivity::class.java)
            startActivity(notificationintent)
            true
        })

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (refrigeratorSearch.actionView as SearchView).apply {
            //Assumes current activity is the searchable activity
            setQuery("", false)
            isIconified = true

            fun filterList(newText:String){
                val newTestList: MutableList<RefItem> = listOf<RefItem>().toMutableList()
                val recyclerView = binding.refrigeratorrecyclerview
                if(newText != "") {
                    for(x in fetchList){
                        if(x.itemname!!.contains(newText)) newTestList.add(x)
                    }
                }
                else {
                    for(x in fetchList) newTestList.add(x)
                }

                if(!newTestList.isNullOrEmpty()) {
                    filteredTestList.clear()
                    for(x in newTestList) filteredTestList.add(x)

                    if(recyclerView.layoutManager == linearLayoutManager){
                        recyclerView.adapter = RefrigeratorAdapter(filteredTestList)
                    }
                    else if(recyclerView.layoutManager == gridLayoutManager){
                        recyclerView.adapter = RefrigeratorIconAdapter(filteredTestList)
                    }
                }
            }

            setIconifiedByDefault(true)
            queryHint = "품목 이름을 입력하세요"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    filterList(query)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    filterList(newText)
                    return false
                }

            })
        }


        return super.onCreateOptionsMenu(menu,inflater)
    }

    fun callImage(taskCode:Int){
        if (checkPermission(CAMERA+STORAGE, CAMERA_CODE)) {
            when(taskCode){
                GALLERY_CODE -> {
                    var intent = Intent(Intent.ACTION_PICK).apply {
                    setDataAndType(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*"
                    )
                    startActivityForResult(this, GALLERY_CODE)
                    }
                }
                CAMERA_CODE ->{
                    cameraCall()

                }
            }
        }
    }



    fun checkPermission(permissions: Array<out String>, type: Int): Boolean
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                Log.d("Response","CheckPermissionstart"+permission)
                if (ContextCompat.checkSelfPermission(context as Activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context as Activity, permissions, type)
                    Log.d("Response","Permissionfailed")
                    return false;
                }
            }
        }

        return true;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "카메라 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            STORAGE_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "저장소 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                        //finish() 앱을 종료함
                    }
                }
            }
        }
    }



    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpeg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun cameraCall() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (context?.let { takePictureIntent.resolveActivity(it.packageManager) } != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            }
            catch (ex: IOException) { // 파일을 만드는데 오류가 발생한 경우
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(requireContext(),BuildConfig.APPLICATION_ID +".provider",photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_CODE)
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //findViewById(R.id.result_image).setImageURI(photoUri);
        lateinit var imgUri:Uri
        lateinit var img:Bitmap
        try {
            when (requestCode) {

                GALLERY_CODE ->{
                    if (resultCode === Activity.RESULT_OK) {
                        if (data != null) {
                            imgUri = data.getData()!!
                            img = MediaStore.Images.Media
                                .getBitmap(activity?.contentResolver, imgUri)



                        };
                    }
                    else{
                        Toast.makeText(context, "사진을 다시 선택해주세요", Toast.LENGTH_LONG).show()
                    }

                }
                CAMERA_CODE -> {
                    if (resultCode === Activity.RESULT_OK) {
                        val file = File(currentPhotoPath)
                        img = MediaStore.Images.Media.getBitmap(activity?.contentResolver, Uri.fromFile(file))

                    }
                    else{
                        Toast.makeText(context, "사진을 다시 촬영해주세요", Toast.LENGTH_LONG).show()
                    }
                }

            }

            val base64encodedimg = bitmapto64(img)
            val handler = Handler()
            Thread(Runnable{
                val ocrResult = doOcr(base64encodedimg)

                handler.post{
                    if(ocrResult != null) {
                        val intent = Intent(getActivity(), AddRefrigeratorActivity::class.java)
                        intent.putExtra("OCR_RESULT",ocrResult)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(context, "오류가 발생했습니다 다시 시도해주세요", Toast.LENGTH_LONG).show()
                    }

                }


            }).start()
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }


    fun bitmapto64(tbitmap: Bitmap):String{
        var bitmap: Bitmap = scaleBitmapDown(tbitmap,640)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        return base64encoded
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth =
                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight =
                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension
            resizedWidth = maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }


    fun doOcr(base64img:String):ArrayList<RefItem>?{
        try {
            val url: URL = URL(ocrApiGwUrl)
            var con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.setUseCaches(false)
            con.setDoInput(true)
            con.setDoOutput(true)
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            con.setRequestProperty("X-OCR-SECRET", ocrSecretKey)
            Log.d("Response",ocrApiGwUrl)
            Log.d("Response",ocrSecretKey as String)

            var json = JSONObject()
            json.put("version", "V2")
            json.put("requestId", UUID.randomUUID().toString())
            json.put("timestamp", System.currentTimeMillis())
            var image = JSONObject()
            image.put("format", "jpeg")
            image.put("data", base64img)
            image.put("name", "test")
            var images = JSONArray()
            images.put(image)
            json.put("images", images)
            var requestBody = json.toString()
            val wr = DataOutputStream(con.getOutputStream())
            wr.writeBytes(requestBody)
            wr.flush()
            wr.close()

            var responseCode = con.getResponseCode()
            val br: BufferedReader
            if (responseCode == 200) {
                br = BufferedReader(InputStreamReader(con.getInputStream()))
                //Log.d("Response","Success"+br.readLine())
            } else {
                br = BufferedReader(InputStreamReader(con.getErrorStream()))
                Log.d("Response","fail")
                return null
            }
            //Log.d("Response","?"+br.readLine())
            var resultJson= JSONObject(br.readLine())
            Log.d("Response","why error?")
            var resultImage = resultJson.getJSONArray("images")
            Log.d("Response", resultImage.toString())
            var resultField = resultImage.getJSONObject(0).getJSONObject("receipt").getJSONObject("result").getJSONArray("subResults").getJSONObject(0).getJSONArray("items")

            //var inputLine:String = br.readLine()
            Log.d("Response",resultField.toString())
            Log.d("Response","??")
            val resultSize = resultField.length()
            Log.d("Response","FieldSize"+resultSize.toString())
            var response  = ArrayList<RefItem>()
            for(i in 0 until resultSize) {
                resultJson = resultField.getJSONObject(i)
                val itemName =
                    resultJson.getJSONObject("name").getJSONObject("formatted").getString("value")
                //Log.d("Response", "Name:" + itemName + resultJson.getJSONObject("count").toString())
                val count = resultJson.optJSONObject("count")
                var itemAmount: Int = 1
                if (count != null) {
                    itemAmount =
                        count.getJSONObject("formatted")
                            .getInt("value")
                }

                response.add(RefItem(itemName,null,null,itemAmount,null, null))
            }
            br.close()
            Log.d("Response",response.toString())
            return response
        }catch(e:Exception){
            Log.d("Response","Exception" + e.toString())
            //Toast.makeText(context, "오류가 발생했습니다 다시 시도해주세요", Toast.LENGTH_LONG).show()
            return null
        }

    }

    //여기가 item 리스트입니다 db가져오는 코드에서 for문으로 itemTestList.add(RefItem(이름, 태그(현제는 null), Date(년,월,일), 갯수, 단위))를 해주시면 추가되요
    fun get_ref_item(): ArrayList<RefItem> {
        val itemTestList = ArrayList<RefItem>()

        try {
            val url:URL = URL("https://b62cvdj81b.execute-api.ap-northeast-2.amazonaws.com/ref-api-test/ref" + "/" + AWSMobileClient.getInstance().username)

            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"

            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val streamReader = InputStreamReader(urlConnection.inputStream)
                val buffered = BufferedReader(streamReader)

                val content = StringBuilder()
                while(true) {
                    val line = buffered.readLine() ?: break
                    content.append(line)
                }

                val data =content.toString()
                val jsonArr = JSONArray(data)
                val i = 0
                for (i in 0 until jsonArr.length()) {

                    val jsonObj = jsonArr.getJSONObject(i)
                    val datestr: String = jsonObj.getString("item_exdate")
                    var date: Date? = null
                    var tag : String? = null
                    if(datestr != "NULL"){
                        val numm = datestr.split("-")
                        date = Date(numm[0].toInt(),numm[1].toInt()-1,numm[2].toInt())
                    }
                    if(jsonObj.getString("item_tag")=="NULL"){
                        tag = "기타"
                    }else{
                        tag  = jsonObj.getString("item_tag")
                    }

                    itemTestList.add(RefItem(
                        jsonObj.getString("item_name"),
                        tag,
                        date,
                        jsonObj.getString("item_amount").toInt(),
                        jsonObj.getString("item_unit"),
                        jsonObj.getString("item_id")
                    ))

                }

                buffered.close()
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return itemTestList
    }
    fun getDB(recyclerView:RecyclerView){
        val handler = Handler()


        Thread(Runnable{
            GlobalScope.launch(Dispatchers.IO) {
                fetchList.clear()
                itemTestList = get_ref_item()
                refDB.clearAllTables()
                for (x in itemTestList) {
                    fetchList.add(x)
                    refDB.refdbDao().insertItem(RefDBItem(x.itemid?.toLong(), x.itemname, x.itemexp?.time))
                }

                handler.post() {
                    Log.d("Response : itemlist", itemTestList.toString())

                    //Log.d("refDB", refDB.refdbDao().getAll().toString())

                    filteredTestList = fetchList.clone() as MutableList<RefItem>
                    //recyclerView.adapter = RefrigeratorAdapter(filteredTestList)
                    if (recyclerView.layoutManager == gridLayoutManager) {
                        recyclerView.adapter = RefrigeratorIconAdapter(filteredTestList)
                    } else {
                        recyclerView.adapter = RefrigeratorAdapter(filteredTestList)
                    }

                    //val intent = Intent(getActivity(), AddRefrigeratorActivity::class.java)
                    //intent.putExtra("OCR_RESULT",ocrResult)
                    //startActivity(intent)
                }
            }




        }).start()

    }


}

@Entity(tableName = "refDBlist")
public data class RefDBItem (
    @PrimaryKey val itemid : Long?,
    @ColumnInfo var itemname: String?,
    @ColumnInfo var itemexp: Long?
)
@Dao
interface  RefDBDao{
    @Query("SELECT * FROM refDBlist")
    fun getAll(): List<RefDBItem>
    /*@Query("SELECT * FROM notificationitem WHERE itemName")
    fun findByItemName()*/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(vararg RefDBItem: RefDBItem)
    @Delete
    fun delete(RefDBItem: RefDBItem)
    @Query("DELETE FROM refDBlist")
    fun deleteAll()
    @Update
    fun updateItem(vararg RefDBItem: RefDBItem)

}
@Database(entities = [RefDBItem::class], version = 1)
abstract class RefItemAppDatabase : RoomDatabase(){
    abstract fun refdbDao() : RefDBDao

    companion object{
        @Volatile
        private var INSTANCE: RefItemAppDatabase? = null

        fun getDatabase(context: Context) : RefItemAppDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null) return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,RefItemAppDatabase::class.java,"refDB").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

/*private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // empty migration.
    }
}*/

