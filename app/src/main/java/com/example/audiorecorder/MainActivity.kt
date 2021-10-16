package com.example.audiorecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileObserver
import android.os.SystemClock
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.audiorecorder.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var mr: MediaRecorder         //Media Recorder class is used to record any type of media
    private lateinit var binding: ActivityMainBinding           //view binding
    private lateinit var dateTime: LocalDateTime          //Using date and time for naming purposes
    private lateinit var allRecsAdapter: AllRecsAdapter


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)            //to use binding in code we set the content view to binding.root instead of layout

        val context = applicationContext

        dateTime = LocalDateTime.now()          //gets the date at time of onCreate()

        val audio = File(this.getExternalFilesDir(null),"audio")
        if (!audio.exists()){
            audio.mkdir()
        }
        val path = this.getExternalFilesDir(null).toString() + "/audio/myRec$dateTime.3gp"


        mr = MediaRecorder()

        binding.btnStart.isEnabled = false
        binding.btnStop.isEnabled = false

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        binding.btnStart.isEnabled = true

        allRecsAdapter = AllRecsAdapter()

        binding.btnStart.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mr.setAudioSource(MediaRecorder.AudioSource.MIC)
                mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
                mr.setOutputFile(path)
                mr.prepare()
                mr.start()
                lifecycleScope.launch {
                    delay(1000)
                    binding.btnStop.isEnabled = true
                }
                binding.btnStart.isEnabled = false
                binding.tvRec.text = "Press Stop to Save the Rec"
                binding.simpleChronometer.base = SystemClock.elapsedRealtime()
                binding.simpleChronometer.start()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                )
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.RECORD_AUDIO),
                        123
                    )
            }
        }

        binding.btnStop.setOnClickListener {
            mr.stop()
            binding.btnStart.isEnabled = true
            binding.btnStop.isEnabled = false
            binding.simpleChronometer.stop()
            allRecsAdapter.notifyDataSetChanged()
            setupRecyclerView()
        }

        setupRecyclerView()

        binding.ivRefresh.setOnClickListener {
            setupRecyclerView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            binding.btnStart.isEnabled = true
    }

    private fun setupRecyclerView() {
        val dir = this.getExternalFilesDir(null).toString() + "/audio"
        val files = File(dir).listFiles()

        val fileNames = arrayOfNulls<String>(files.size)
        files?.mapIndexed { index, file ->
            fileNames[index] = file?.name
        }


        if (files.isNotEmpty() && fileNames.isNotEmpty()) {
            val allRecs = arrayListOf<Rec>()
            for (index in files.indices) {
                val rec = Rec(fileNames[index]!!, Uri.fromFile(files[index]!!))
                allRecs.add(rec)
            }
            allRecsAdapter.setArray(allRecs)

            binding.rvAllRecs.apply {
                adapter = allRecsAdapter
                layoutManager = GridLayoutManager(this@MainActivity, 1)
            }

            allRecsAdapter.notifyDataSetChanged()

        } else {
            Toast.makeText(this, "No recordings to show", Toast.LENGTH_LONG).show()
        }
    }
}