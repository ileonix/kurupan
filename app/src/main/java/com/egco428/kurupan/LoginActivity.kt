package com.egco428.kurupan

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var dataReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var data = intent.extras
        var id = ""
        id = data.getString("ID")
// ใช้สำหรับเคลียร์ ค่าในช่องข้อมูล
        btn_cancel.setOnClickListener {
            edt_username.setText("")
            edt_password.setText("")
        }
        btn_signin.setOnClickListener {
            //ส่งค่าไปบังอีกหน้า
            var intent = Intent(this, DataActivity::class.java)
            intent.putExtra("logincheck",true)
            intent.putExtra("ID",id)
            //เอาตัวแปรมารับค่าช่องไว้ จะได้เรียกใช้ง่ายๆ
            val username_edt: String = edt_username.text.toString()
            val password_edt: String = edt_password.text.toString()
            dataReference = FirebaseDatabase.getInstance().getReference("user") // อ้างกิงที่ user in firebase
            var myRef = dataReference
            var msgList : MutableList<user> = mutableListOf()

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    var notNull = false
                    var check:Boolean=false
                    msgList.clear()
                    for (child: DataSnapshot in snapshot!!.children) { //loop check null
                        val dumpData: user = child.getValue(user::class.java)!!
                        msgList.add(dumpData!!)
                        notNull = true
                    }
                    if (notNull == false){
                        Log.i("Firebase:", "data not found")
                    }else{
                        if (msgList.isNotEmpty()){
                            msgList.sortByDescending { it.password }
                            var ii = 0

                            for( i in msgList){ //loop check value in firebase username and password
                                if(username_edt.equals(msgList[ii].username) && password_edt.equals(msgList[ii].password)){
                                    check = true
                                }
                                ii = ii + 1
                            }
                            if (check==true) //login sucesses
                            {
                                Log.d("Firebase","Login success") //intent here
                                Toast.makeText(baseContext, "Login success", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            }else{ //logig fail
                                Log.d("Firebase", "Fail")
                                Toast.makeText(baseContext, "Login fail", Toast.LENGTH_SHORT).show()
                            }
                            check = false
                        }
                    }
                }
                override fun onCancelled(e: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
        }
    }
}
class user { //class for get value from firebase to class for use
    var username: String = ""
    var password: String = ""
    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    constructor(username: String, password: String) {
        this.username = username
        this.password = password
    }
}
