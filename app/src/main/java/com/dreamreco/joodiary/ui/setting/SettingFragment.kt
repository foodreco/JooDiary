package com.dreamreco.joodiary.ui.setting

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentListBinding
import com.dreamreco.joodiary.databinding.FragmentSettingBinding
import com.dreamreco.joodiary.ui.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private val settingViewModel by viewModels<SettingViewModel>()
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }
}