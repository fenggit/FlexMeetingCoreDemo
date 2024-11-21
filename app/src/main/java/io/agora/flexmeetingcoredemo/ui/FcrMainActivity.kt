package io.agora.flexmeetingcoredemo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.flexmeetingcoredemo.R
import io.agora.flexmeetingcoredemo.data.ResponseThrowable
import io.agora.flexmeetingcoredemo.databinding.FcrActivityMainBinding
import io.agora.flexmeetingcoredemo.vm.FcrRoomViewModel

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 16:19
 */
class FcrMainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this)[FcrRoomViewModel::class.java]
    }
    private val binding by lazy {
        FcrActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnCreateRoom.setOnClickListener {
            FcrCreateRoomFragment().apply {
                supportFragmentManager.beginTransaction().add(binding.fragmentContainer.id, this)
                    .addToBackStack(FcrCreateRoomFragment::class.java.name).commit()
            }
        }
        binding.btnJoinRoom.setOnClickListener {
            FcrJoinRoomFragment().apply {
                supportFragmentManager.beginTransaction().add(binding.fragmentContainer.id, this).addToBackStack(FcrJoinRoomFragment::class.java.name)
                    .commit()
            }
        }
        viewModel.joinRoomStatus.observe(this) {
            if (it.isSuccess) {
                showRoomFragment()
            }
            if (it.isFailure) {
                val exception = it.exceptionOrNull()
                if (exception is ResponseThrowable) {
                    // handle exception
                    if (exception.code == 1101021) {
                        Toast.makeText(this, R.string.room_not_exist, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "${exception.code}  ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        viewModel.leaveRoom.observe(this) {
            if (it) {
                removeAllFragment()
            }
        }
    }

    private fun removeAllFragment() {
        val beginTransaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach {
            beginTransaction.remove(it)
        }
        beginTransaction.commit()

    }

    private fun showRoomFragment() {
        supportFragmentManager.popBackStack()
        FcrRoomFragment().apply {
            supportFragmentManager.beginTransaction().add(binding.fragmentContainer.id, this)
                .addToBackStack(FcrRoomFragment::class.java.name).commit()
        }
    }
}