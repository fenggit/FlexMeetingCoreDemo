package io.agora.flexmeetingcoredemo.ui

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.permissionx.guolindev.PermissionX
import io.agora.agoracore.core2.bean.FcrUserRole
import io.agora.core.common.log.LogX
import io.agora.flexmeetingcoredemo.R
import io.agora.flexmeetingcoredemo.databinding.FcrFragmentCreateRoomBinding
import io.agora.flexmeetingcoredemo.ui.base.BaseFragment
import io.agora.flexmeetingcoredemo.vm.FcrRoomViewModel
import java.util.UUID

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 16:21
 */
class FcrCreateRoomFragment : BaseFragment() {
    private companion object {
        const val TAG = "FcrCreateRoomFragment"
    }

    private lateinit var binding: FcrFragmentCreateRoomBinding
    private lateinit var viewModel: FcrRoomViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[FcrRoomViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FcrFragmentCreateRoomBinding.inflate(inflater, container, false)
        binding.root.setOnClickListener {}
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreateRoom.setOnClickListener {
            createJoinRoom()
        }
        viewModel.joinRoomStatus.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                LogX.i(TAG, "createJoinRoom success")
            }
            if (it.isFailure) {
                LogX.i(TAG, "createJoinRoom failure")
            }

        }
    }

    private fun createJoinRoom() {
        PermissionX.init(this)
            .permissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val roomName = binding.etRoomName.text.toString().trim()
                    val nickName = UUID.randomUUID().toString().substring(0, 8)
                    if (checkInfo(roomName, nickName, requireContext())) {
                        viewModel.createJoinRoom(requireContext(), userName = nickName, roomName = roomName, userRole = FcrUserRole.HOST)
                    }
                }
            }
    }

    /**
     * 检查输入信息
     * @param roomName 房间名称
     * @param userName 用户名
     */
    private fun checkInfo(roomName: String, userName: String, context: Context): Boolean {
        if (roomName.isEmpty()) {
            Toast.makeText(context, getString(R.string.meeting_name_empty_tips), Toast.LENGTH_SHORT).show()
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