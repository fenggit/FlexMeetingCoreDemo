package io.agora.flexmeetingcoredemo.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.permissionx.guolindev.PermissionX
import io.agora.agoracore.core2.bean.FcrUserRole
import io.agora.core.common.helper.ToastManager
import io.agora.flexmeetingcoredemo.R
import io.agora.flexmeetingcoredemo.databinding.FcrFragmentJoinRoomBinding
import io.agora.flexmeetingcoredemo.ui.base.BaseFragment
import io.agora.flexmeetingcoredemo.vm.FcrRoomViewModel
import java.util.UUID

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/20 16:31
 */
class FcrJoinRoomFragment : BaseFragment() {
    private lateinit var binding: FcrFragmentJoinRoomBinding
    private val viewModel by lazy {
        ViewModelProvider(requireActivity())[FcrRoomViewModel::class.java]
    }
    private val role: FcrUserRole
        get() {
            return when (binding.radioGroup.checkedRadioButtonId) {
                R.id.btnHost -> FcrUserRole.HOST
                R.id.btnParticipant -> FcrUserRole.PARTICIPANT
                else -> FcrUserRole.PARTICIPANT
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FcrFragmentJoinRoomBinding.inflate(inflater, container, false)
        binding.root.setOnClickListener {}
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnJoinRoom.setOnClickListener {
            joinRoom()
        }
    }

    private fun joinRoom() {
        PermissionX.init(this)
            .permissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val roomName = binding.etRoomNameOrId.text.toString().trim()
                    val nickName = UUID.randomUUID().toString().substring(0, 8)
                    if (checkInfo(roomName, nickName)) {
                        viewModel.joinRoom(requireContext(), null, roomName, role, nickName)
                    }
                } else {
                    Toast.makeText(context, getString(R.string.no_enough_permissions), Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * 检查输入信息
     * @param roomName 会议号
     * @param userName 用户名
     */
    private fun checkInfo(roomName: String, userName: String): Boolean {
        if (roomName.isEmpty()) {
            Toast.makeText(context, getString(R.string.room_id_empty_tips), Toast.LENGTH_SHORT).show()
            return false
        }
        if (userName.isEmpty()) {
            Toast.makeText(context, getString(R.string.nickname_empty_tips), Toast.LENGTH_SHORT).show()
            return false
        }
        if (userName.length < 2 || userName.length > 20) { // 2-20
            Toast.makeText(context, getString(R.string.nickname_tips_content_length), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}