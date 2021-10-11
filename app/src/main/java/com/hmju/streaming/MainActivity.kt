package com.hmju.streaming

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.caster).setOnClickListener {
            startActivity(Intent(this, CasterActivity::class.java))
        }

        findViewById<Button>(R.id.viewer).setOnClickListener {
            startActivity(Intent(this, ViewerActivity::class.java))
        }
    }
}