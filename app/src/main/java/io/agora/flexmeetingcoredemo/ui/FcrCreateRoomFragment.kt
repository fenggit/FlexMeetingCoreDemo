package io.agora.flexmeetingcoredemo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.flexmeetingcoredemo.databinding.FcrFragmentCreateRoomBinding
import io.agora.flexmeetingcoredemo.ui.base.BaseFragment

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/19 16:21
 */
class FcrCreateRoomFragment : BaseFragment() {
    private lateinit var binding: FcrFragmentCreateRoomBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FcrFragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}