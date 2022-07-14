package com.dreamreco.joodiary.ui.customDialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.BackUpLoadingDialogBinding
import com.dreamreco.joodiary.databinding.GetImageDialogBinding
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.ui.setting.SettingViewModel
import com.dreamreco.joodiary.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BackUpLoadingDialog : DialogFragment() {

    private val settingViewModel by viewModels<SettingViewModel>()
    private val binding by lazy { BackUpLoadingDialogBinding.inflate(layoutInflater) }
    private var _saveFileUri = MutableLiveData<Uri?>(null)
    private var saveFileUri : Uri? = null
    private var typeface: Typeface? = null

    // 뒤로가기 관련 변수
    private var cancelState = 0

    // Dialog 배경 투명하게 하는 코드??
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 폰트 설정 및 적용 코드
        typeface = getFontType(requireContext())
        typeface?.let { setGlobalFont(binding.root, it) }

        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setBackUp()
        _saveFileUri.observe(viewLifecycleOwner){
            // Uri 값에 주소가 전달되면 바로 작동
            if (it != null) {
                saveFileUri = it
                settingViewModel.getBackUpText()
            }
        }

        settingViewModel.textForBackUp.observe(viewLifecycleOwner){
            if ((it != "")&&(it != null)) {
                saveRoomToTextFile(it)
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setBackUp() {
        Toast.makeText(requireContext(),"저장할 위치를 선택하세요.",Toast.LENGTH_SHORT).show()
        val now = System.currentTimeMillis()
        val date = Date(now)
        @SuppressLint("SimpleDateFormat")
        val sdfNow = SimpleDateFormat("yy-MM-dd HH:mm:ss")
        val formatDate = sdfNow.format(date)

        /**
         * SAF 파일 편집
         */

        val fileName = getString(R.string.app_name)+" $formatDate.txt"

        // ACTION_CREATE_DOCUMENT 인텐트를 실행하여 사용자가 원하는 폴더를 선택하여 파일을 생성
        // 우선 파일부터 생성됨
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        pathAndWriteText.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val pathAndWriteText =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode == Activity.RESULT_OK) {
                _saveFileUri.value = it.data?.data as Uri
            }
        }

    private fun saveRoomToTextFile(text:String) {
        // 여기서 파일 내용을 수정하게 됨
        val pfd = requireContext().contentResolver.openFileDescriptor(saveFileUri!!, "w")
        val fileOutputStream = FileOutputStream(pfd!!.fileDescriptor)
        fileOutputStream.write(text.toByteArray())
        fileOutputStream.close()
        pfd.close()

        Toast.makeText(requireContext(), getString(R.string.backup_completed),Toast.LENGTH_SHORT).show()

        // write 가 완료되면 dialog 종료
        dismiss()
    }

    override fun onResume() {
        super.onResume()
        cancelState += 1
        if ((cancelState == 2) && (saveFileUri == null)) {
            // 두번째 resume + 저장 uri 가 없다면
            // 유저가 도중에 뒤로가기 한 상황
            Toast.makeText(requireContext(),getString(R.string.backup_canceled),Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

}