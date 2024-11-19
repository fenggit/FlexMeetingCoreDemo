package io.agora.flexmeetingcoredemo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.agora.flexmeetingcoredemo.databinding.FcrActivityMainBinding

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 16:19
 */
class FcrMainActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FcrActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, FcrCreateRoomFragment()).commit()
    }
}