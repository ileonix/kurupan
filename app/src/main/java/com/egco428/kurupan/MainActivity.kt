package com.egco428.kurupan

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_main.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    var mScannerView: ZXingScannerView? = null
    //มีการใช้ library ZXingScanner เพื่อทำการสแกนบาร์โค้ด โดยสามารถสแกนได้ทุกฟอร์แมตที่มีอยู่ ณ ปัจจุบัน

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(PermissionCameraRequest() && PermissionLocationRequest()){
            //ทำการร้องข้อสิทธิ์ในการเข้าถึงการใช้กล้อง
            val contentFrame = findViewById(R.id.content_frame) as ViewGroup
            mScannerView = ZXingScannerView(this)
            contentFrame.addView(mScannerView)
            mScannerView!!.setResultHandler(this)
        }
        val data = intent.extras
        var check = false
        var id = ""

        //Press Login then start LoginActivity
        loginButton.setOnClickListener {
            id = idEdit.text.toString()
            if(id != ""){
                Toast.makeText(this,"Login", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("ID",id)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this,"Please Input ID", Toast.LENGTH_SHORT).show()
            }
        }
        //Press Search then start DataActivity
        searchButton.setOnClickListener {
            id = idEdit.text.toString()
            if (id != ""){
                var intent = Intent(this, DataActivity::class.java)
                intent.putExtra("logincheck",false)
                intent.putExtra("ID",id)
                startActivity(intent)
            }else{
                Toast.makeText(this,"Please Input ID", Toast.LENGTH_SHORT).show()
            }
        }
        //restart mainActivity when barcode scanner error
        cancelBtn.setOnClickListener {
            finish()
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    //Source code from below is requirment for ZXingScanner
    val REQUEST_CODE_ASK_PERMISSIONS = 123

    fun PermissionCameraRequest():Boolean {
        val hasWriteContactsPermission = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showMessageOKCancel("You need to allow access to Camera",
                        DialogInterface.OnClickListener { dialog, which ->
                            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                                    REQUEST_CODE_ASK_PERMISSIONS)
                        })
                return true
            }
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_ASK_PERMISSIONS)
            return true
        }
        return true
    }
    fun PermissionLocationRequest():Boolean{
        val hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow access to Location",
                        DialogInterface.OnClickListener { dialog, which ->
                            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    REQUEST_CODE_ASK_PERMISSIONS)
                        })
                return true
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_ASK_PERMISSIONS)
            return true
        }
        return true
    }
    fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }
    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }
    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }
    override fun handleResult(result: Result) {
        Toast.makeText(applicationContext, result.text, Toast.LENGTH_SHORT).show()
        idEdit.setText(result.text)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
