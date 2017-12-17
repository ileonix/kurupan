package com.egco428.kurupan
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_data.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.ceylonlabs.imageviewpopup.ImagePopup

class DataActivity : AppCompatActivity(){
    var check: Boolean = false
    var id : String = ""
    private var locationManager : LocationManager? = null
    lateinit var msgList : MutableList<dataClass>

    val mDatabase : DatabaseReference = FirebaseDatabase.getInstance().getReference()
    val myRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("kurupans")

    val storageRef: StorageReference? = FirebaseStorage.getInstance().getReference("images")
    var imageRef = storageRef!!.child("phenom.jpg")

    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var photoImageView1: ImageView
    lateinit var photoImageView2: ImageView
    lateinit var photoImageView3: ImageView
    lateinit var photoImageView4: ImageView
    var imageSelect : Int = 0

    lateinit var native_resolution1: Bitmap
    lateinit var native_resolution2: Bitmap
    lateinit var native_resolution3: Bitmap
    lateinit var native_resolution4: Bitmap

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typeeditText = findViewById<View>(R.id.typeeditText) as Spinner  // Create an ArrayAdapter using the string array and a default spinner layout
        val type_adapter = ArrayAdapter.createFromResource(this,
                R.array.state_type, android.R.layout.simple_spinner_item)   // Specify the layout to use when the list of choices appears
        type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Apply the adapter to the spinner
        typeeditText.adapter = type_adapter

        val majorspin = findViewById<View>(R.id.majorspin) as Spinner  // Create an ArrayAdapter using the string array and a default spinner layout
        val major_adapter = ArrayAdapter.createFromResource(this,
                R.array.major_type, android.R.layout.simple_spinner_item)   // Specify the layout to use when the list of choices appears
        major_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Apply the adapter to the spinner
        majorspin.adapter = major_adapter

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            Toast.makeText(this,"Getting location...",Toast.LENGTH_SHORT).show()
        } catch(ex: SecurityException) {
            Toast.makeText(this, "No location available",Toast.LENGTH_SHORT).show()
        }

        val data = intent.extras
        check = data.getBoolean("logincheck")

        id = data.getString("ID")
        Log.d("id from main",id)
        imageView2.setOnClickListener {
            if (check == true){Log.d("hello","login")
                Toast.makeText(this, "Login Session", Toast.LENGTH_SHORT).show()
            }
            else{Log.d("hello","not login")
                Toast.makeText(this, "Guest Session", Toast.LENGTH_SHORT).show()
            }
        }
        ideditText.setText(id)

        msgList = mutableListOf()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                var notNull = false
                msgList.clear()
                for (child: DataSnapshot in snapshot!!.children) {
                    val dumpData: dataClass = child.getValue(dataClass::class.java)!!
                    if(dumpData.no == id ){
                        msgList.add(dumpData!!)
                        Log.i("Firebase:", "Found-------------" )
                        Log.i("Firebase:", "no :" + dumpData.no)
                        Log.i("Firebase:", "name :" + dumpData.name)
                        Log.i("Firebase:", "type :" + dumpData.type)
                        Log.i("Firebase:", "major :" + dumpData.major)
                        Log.i("Firebase:", "location :" + dumpData.location)
                    }
                    notNull = true
                }
                if (notNull == false){
                    Log.i("Firebase:", "data not found")
                }else{
                    if (msgList.isNotEmpty()){
                        msgList.sortByDescending { it.dateTime }
                        ideditText.setText(msgList[0].no)
                        nameeditText.setText(msgList[0].name)
                        var index = 0
                        if(msgList[0].type == "ใช้งาน"){
                            index = 0
                        }else if (msgList[0].type == "ชำรุด"){
                            index = 1
                        }else if (msgList[0].type == "ไม่จำเป็น"){
                            index = 2
                        }else if (msgList[0].type == "หาไม่พบ"){
                            index = 3
                        }
                        typeeditText.setSelection(index)//.setText(msgList[0].type)

                        var mindex = 0
                        if(msgList[0].major == "วิศวกรรมเครื่องกล"){
                            mindex = 0
                        }else if (msgList[0].major == "วิศวกรรมเคมี"){
                            mindex = 1
                        }else if (msgList[0].major == "วิศวกรรมโยธา"){
                            mindex = 2
                        }else if (msgList[0].major == "วิศวกรรมคอมพิวเตอร์"){
                            mindex = 3
                        }else if (msgList[0].major == "วิศวกรรมอุตสาหการ"){
                            mindex = 4
                        }else if (msgList[0].major == "วิศวกรรมไฟฟ้า"){
                            mindex = 5
                        }else if (msgList[0].major == "วิศวกรรมชีวการแพทย์"){
                            mindex = 6
                        }
                        majorspin.setSelection(mindex)
                        locationeditText.setText(msgList[0].location)
                        timeeditText.setText(msgList[0].dateTimeShow)
                    }
                }
                Log.i("Firebase:", "finish read data")
            }
            override fun onCancelled(e: DatabaseError?) {  }
        })

        // download picture from firebaseStorage ------------------------------------------------------------------------------------------------------------
        imageRef = storageRef!!.child(ideditText.text.toString()+"/1.jpg")
        imageRef.getBytes(java.lang.Long.MAX_VALUE).addOnSuccessListener { bytes -> bytes
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            native_resolution1 = bitmap
            imageView1.setImageBitmap(bitmap)
        }.addOnFailureListener { exception -> exception
            Toast.makeText(this,"Can't get image1 from storage.",Toast.LENGTH_SHORT).show()
        }
        imageRef = storageRef!!.child(ideditText.text.toString()+"/2.jpg")
        imageRef.getBytes(java.lang.Long.MAX_VALUE).addOnSuccessListener { bytes -> bytes
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            native_resolution2 = bitmap
            imageView2.setImageBitmap(bitmap)
        }.addOnFailureListener { exception -> exception
            Toast.makeText(this,"Can't get image2 from storage.",Toast.LENGTH_SHORT).show()
        }
        imageRef = storageRef!!.child(ideditText.text.toString()+"/3.jpg")
        imageRef.getBytes(java.lang.Long.MAX_VALUE).addOnSuccessListener { bytes -> bytes
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            native_resolution3 = bitmap
            imageView3.setImageBitmap(bitmap)

        }.addOnFailureListener { exception -> exception
            Toast.makeText(this,"Can't get image3 from storage.",Toast.LENGTH_SHORT).show()
        }
        imageRef = storageRef!!.child(ideditText.text.toString()+"/4.jpg")
        imageRef.getBytes(java.lang.Long.MAX_VALUE).addOnSuccessListener { bytes -> bytes
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            native_resolution4 = bitmap
            imageView4.setImageBitmap(bitmap)
        }.addOnFailureListener { exception -> exception
            Toast.makeText(this,"Can't get image4 from storage.",Toast.LENGTH_SHORT).show()
        }
        timeeditText.isEnabled = false

        // disable editText to edit value & hidden button ------------------------------------------------------------------------------------------------------------
        if (check == false){
            ideditText.isEnabled = false
            nameeditText.isEnabled = false
            typeeditText.isEnabled = false
            majorspin.isEnabled = false
            locationeditText.isEnabled = false
            timeeditText.isEnabled = false

            submitBtn.visibility = View.INVISIBLE
            submitBtn.isEnabled = false
            clearBtn.visibility = View.INVISIBLE
            clearBtn.isEnabled = false

        }

        //PhotoImageView Setup----------------------------------------------------------------------------------------------------------
        photoImageView1 = findViewById(R.id.imageView1)
        photoImageView2 = findViewById(R.id.imageView2)
        photoImageView3 = findViewById(R.id.imageView3)
        photoImageView4 = findViewById(R.id.imageView4)

        // 4 picture button command ------------------------------------------------------------------------------------------------------------
        imageView1.setOnClickListener {
            pictureChange(1)
        }
        imageView2.setOnClickListener {
            pictureChange(2)
        }
        imageView3.setOnClickListener {
            pictureChange(3)
        }
        imageView4.setOnClickListener {
            pictureChange(4)
        }
        //for zooming preview picture
        imageView1.setOnLongClickListener {
            preview(1)
        }
        imageView2.setOnLongClickListener {
            preview(2)
        }
        imageView3.setOnLongClickListener {
            preview(3)
        }
        imageView4.setOnLongClickListener {
            preview(4)
        }
        // save to firebase button ---------------------------------------------------------------------------------------------------------------
        submitBtn.setOnClickListener {
            writeNewObj(ideditText.text.toString(),nameeditText.text.toString()
                    ,typeeditText.getSelectedItem().toString(),majorspin.selectedItem.toString(),locationeditText.text.toString())
            uploadimage()
        }
        clearBtn.setOnClickListener {
            ideditText.setText(null)
            nameeditText.setText(null)
            typeeditText.setSelection(0)
            majorspin.setSelection(0)
            locationeditText.setText(null)
            timeeditText.setText(null)
        }
    }
    fun preview(id: Int):Boolean{
        val imagePopup = ImagePopup(this)
        if (id == 1){
            imagePopup.initiatePopup(imageView1.drawable)
        }else if (id == 2){
            imagePopup.initiatePopup(imageView2.drawable)
        }else if (id == 3){
            imagePopup.initiatePopup(imageView3.drawable)
        }else if (id == 4){
            imagePopup.initiatePopup(imageView4.drawable)
        }
        imagePopup.viewPopup()
        return true
    }
    //Function for get location [latitude:longtitude]
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (check == true){
                locationeditText.setText(""+location.latitude+":"+location.longitude)
            }
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    //Function picture change and setup ---------------------------------------------------------------------------------------------------------------------------------------------
    private fun pictureChange(imageNum : Int){
        if (check == true){
            Toast.makeText(this, "Login Session", Toast.LENGTH_SHORT).show()
            imageSelect = imageNum
            launchCamera()
        }
        else{
            Toast.makeText(this, "Guest Session", Toast.LENGTH_SHORT).show()
        }
    }
    //dont forget to set policy for read/write database on firebase before check another bug firebase -----------------------------------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O) //มีการเรียกใช้คุณสมบัติใหม่ใน android oreo api 26
    private fun writeNewObj(numberObj: String, nameObj: String, typeObj: String,majorObj: String, locationObj: String) {
        var datetime_current:String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())
        val user = dataClass(numberObj, nameObj,typeObj,majorObj,locationObj, System.currentTimeMillis().toString(), datetime_current)
        //timestamp datetime
        //datetime_current จะทำการดึงข้อมูลวันที่และเวลาเพื่อนำไป
        Log.d("TEST",ideditText.text.toString())
        mDatabase.child("kurupans").push().setValue(user)
    }
    //open camera ----------------------------------------------------------------------------------------------------------------------------------------------------
    fun launchCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE)
    }
    //ทำงานต่อเนื่องจาก function launchCamera ผ่าน REQUEST_IMAGE_CAPTURE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val extra = data!!.extras
            val photo = extra.get("data") as Bitmap
            when (imageSelect) {
                1 -> {
                    photoImageView1.setImageBitmap(photo)
                    native_resolution1 = photo}
                2 -> {
                    photoImageView2.setImageBitmap(photo)
                    native_resolution2 = photo
                }
                3 -> {
                    photoImageView3.setImageBitmap(photo)
                    native_resolution3 = photo
                }
                4 -> {
                    photoImageView4.setImageBitmap(photo)
                    native_resolution4 = photo
                }
                else -> Log.d("TEST","ERROR CASE OF CHANGE IMAGE BUTTON")
            }
        }
    }
    fun uploadimage(){
        uploadPerQ(native_resolution1,1)
        uploadPerQ(native_resolution2,2)
        uploadPerQ(native_resolution3,3)
        uploadPerQ(native_resolution4,4)

    }
    private fun uploadPerQ(photoImageView : Bitmap, fileName : Int){
        imageRef = storageRef!!.child(ideditText.text.toString()+"/"+fileName.toString()+".jpg")
        var fname:Int = fileName*25
        var bitmap : Bitmap = photoImageView
        var baos : ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        var data : ByteArray = baos.toByteArray()
        imageRef!!.putBytes(data)
                .addOnSuccessListener {
                    Toast.makeText(this,"Upload Success",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Upload Fail",Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener {
                    Toast.makeText(this,"Uploading "+fname+" %",Toast.LENGTH_SHORT).show()
                }
    } //ทำหน้าที่จัดการการอัฑโหลดรูปภาพทั้งสี่รูป โดยอัพรูปละรอบ
}
// data class as dataStructure for firebase read/write ------------------------------------------------------------------------------------------------------------
class dataClass {
    var no: String = ""
    var name: String = ""
    var type: String = ""
    var major: String = ""
    var location: String = ""
    var dateTime: String = ""
    var dateTimeShow: String = ""
    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    constructor(numberObj: String, nameObj: String, typeObj: String, majorObj: String, locationObj: String, dateTimeObj: String, dateTimeShow: String) {
        this.no = numberObj
        this.name = nameObj
        this.type = typeObj
        this.major = majorObj
        this.location = locationObj
        this.dateTime = dateTimeObj
        this.dateTimeShow = dateTimeShow
    }
}
