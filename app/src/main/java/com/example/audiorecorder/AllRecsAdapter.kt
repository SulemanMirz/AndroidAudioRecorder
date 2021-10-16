package com.example.audiorecorder

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.example.audiorecorder.databinding.RecPreviewBinding

class AllRecsAdapter : RecyclerView.Adapter<AllRecsAdapter.RecViewHolder>() {

    inner class RecViewHolder(var binding: RecPreviewBinding): RecyclerView.ViewHolder(binding.root)

    private var rec = arrayListOf<Rec>()

    private lateinit var context: Context

    fun setArray(setRec: ArrayList<Rec>) {
        rec = setRec
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        return RecViewHolder(
            RecPreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    private var mp: MediaPlayer? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        val bindRec = rec[position]
        holder.binding.apply {
            tvRecName.text = bindRec.name
            tvRecUri.text = bindRec.uri.toString()
            this.root.setOnClickListener {
                audioPlayer(bindRec.uri.toString())
            }
            this.root.setOnLongClickListener {
                try {

                    val popupMenu = PopupMenu(context,root)

                    (root.context as AppCompatActivity).menuInflater.inflate(R.menu.menu_long_click,popupMenu.menu)
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when(menuItem.itemId){
                            R.id.delete -> {
                                deleteRec(position, bindRec.uri)
                                return@setOnMenuItemClickListener true
                            }
                            else ->
                                return@setOnMenuItemClickListener false
                        }
                    }
                    true
                }catch (e: Exception){
                    e.printStackTrace()
                    false
                }
            }
            mp?.currentPosition?.let { progressBarAudioPlayer.setProgress(it,true) }
        }
    }



//    private fun menuItemSelected(item: MenuItem,position: Int,uri: Uri){
//        class Menu: AppCompatActivity(){
//            override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
//                return super.onCreateOptionsMenu(menu)
//            }
//
//            override fun onOptionsItemSelected(item: MenuItem): Boolean {
//                when(item.itemId){
//                    R.id.delete ->
//                        deleteRec(position, uri)
//                    else ->
//                        return false
//                }
//                return super.onOptionsItemSelected(item)
//            }
//        }
//    }

    private fun audioPlayer(path: String){
        if(mp?.isPlaying == false){
            mp = MediaPlayer()
            mp?.setDataSource(path)
            mp?.prepare()
            mp?.start()
        }else {
            mp?.reset()
            mp?.pause()
            mp = null
            mp = MediaPlayer()
            mp?.setDataSource(path)
            mp?.prepare()
            mp?.start()
        }
    }

    private fun deleteRec(position: Int,uri: Uri){
        val file = uri.toFile()
       try {
           if (file.exists()) {
               file.delete()
               rec.removeAt(position)
               notifyItemRemoved(position)
               Toast.makeText(context, "File deleted Successfully", Toast.LENGTH_SHORT).show()
           } else {
               Toast.makeText(context, "File not found to delete", Toast.LENGTH_SHORT).show()
           }
       }catch (e: Exception){
           e.printStackTrace()
           Toast.makeText(context, "Exception",Toast.LENGTH_SHORT).show()
       }
    }

    override fun getItemCount(): Int {
        return rec.size
    }

    override fun onViewDetachedFromWindow(holder: RecViewHolder) {
        super.onViewDetachedFromWindow(holder)

        if (mp?.isPlaying == true) {
            mp?.reset()
            mp?.stop()
        }
    }
}