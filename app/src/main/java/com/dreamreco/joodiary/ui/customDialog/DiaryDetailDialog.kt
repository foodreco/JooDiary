package com.dreamreco.joodiary.ui.customDialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.DiaryDetailDialogBinding
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DiaryDetailDialog : DialogFragment() {
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private val binding by lazy { DiaryDetailDialogBinding.inflate(layoutInflater) }
    private val args by navArgs<DiaryDetailDialogArgs>()

    private val spinnerDrinkTypeList = mutableListOf<String>()
    private val spinnerPOAList = mutableListOf<String>()
    private val spinnerVODList = mutableListOf<String>()
    private var selectedDrinkType = ""
    private var selectedPOA = ""
    private var selectedVOD = ""
    private var diaryImportance = MutableLiveData<Boolean>(false)
    private var diaryImportanceForUpdate = false

    // 원본 사진이 저장되는 Uri
    // update 변수 역할도 함
    private var photoUri: Uri? = null

    // 날짜 변경 관련 변수 설정
    private var updatedYear: Int = 0
    private var updatedMonth: Int = 0
    private var updatedDay: Int = 0

    private var typeface: Typeface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 날짜 변경 관련 변수 설정
        updatedYear = args.diaryBase.date.year
        updatedMonth = args.diaryBase.date.month
        updatedDay = args.diaryBase.date.day

        // 폰트 설정 및 적용 코드
        typeface = getFontType(requireContext())
        typeface?.let { setGlobalFont(binding.root, it) }

        with(binding) {
            drinkTypeTextInputLayout.typeface = typeface
            VODTextInputLayout.typeface = typeface
            POATextInputLayout.typeface = typeface

            if (getThemeType() == THEME_2) {
                drinkTypeTextInputLayout.defaultHintTextColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.theme2_primary_text_color
                )
                drinkTypeTextInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.theme2_primary_text_color)
                drinkTypeTextInputLayout.boxStrokeWidth = 0
                drinkTypeTextInputLayout.boxStrokeWidthFocused = 0

                POATextInputLayout.defaultHintTextColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.theme2_primary_text_color
                )
                POATextInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.theme2_primary_text_color)
                POATextInputLayout.boxStrokeWidth = 0
                POATextInputLayout.boxStrokeWidthFocused = 0

                VODTextInputLayout.defaultHintTextColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.theme2_primary_text_color
                )
                VODTextInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.theme2_primary_text_color)
                VODTextInputLayout.boxStrokeWidth = 0
                VODTextInputLayout.boxStrokeWidthFocused = 0
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // diaryBase 요소 세팅
        basicDiarySetting()

        // 스피너 세팅
        spinnerSetting()

        // 새 데이터 생성 시, 타이틀 키보드 팝업
        if (args.diaryBase.title == "") {
            binding.titleText.setFocusAndShowKeyboard(requireContext())
        }

        with(binding) {
            // 이미지 터치 시 작동
            diaryImageView.setOnClickListener {
                // 만약 이미지가 존재하지 않으면,
                if (photoUri == null) {
                    // 키보드 올라가 있다면 내리고,
                    titleText.clearFocusAndHideKeyboard(requireContext())
                    // 바로 이미지 가져오기 작동
                    takeImage()
                } else {
                    // 이미지가 존재한다면, FullImageDialog 이동
                    val argument = photoUri.toString()
                    val action =
                        DiaryDetailDialogDirections.actionDiaryDetailDialogToFullImageDialog(
                            argument
                        )
                    findNavController().navigate(action)
                }
            }
        }

        with(calendarViewModel) {

            // 저장 시 타이틀이 중복되면 작동하는 코드
            listDuplication.observe(viewLifecycleOwner) { isDuplicated ->
                if (isDuplicated) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.title_duplication),
                        Toast.LENGTH_SHORT
                    ).show()
                    listDuplicationDone()
                }
            }

            // 저장 완료 후, 이전으로 돌아가는 코드
            insertOrUpdateEventDone.observe(viewLifecycleOwner) { done ->
                if (done) {
                    // 키보드 올라가 있다면 내리고,
                    binding.titleText.clearFocusAndHideKeyboard(requireContext())
                    findNavController().navigateUp()
                }
            }
            // 삭제 완료 후, 이전으로 돌아가는 코드
            dialogDeleteCompleted.observe(viewLifecycleOwner) { done ->
                if (done) {
                    // 키보드 올라가 있다면 내리고,
                    binding.titleText.clearFocusAndHideKeyboard(requireContext())
                    findNavController().navigateUp()
                }
            }

            // 이미지를 불러오는 코드(Dialog 에서 결정 시, 옵저버로 작동)
            getImageChangeSignal().observe(viewLifecycleOwner) { loadType ->
                when (loadType) {
                    LOAD_IMAGE_FROM_GALLERY -> {
                        getImageFromGalleryOrCamera(GET_DATA_PERMISSIONS)
                        calendarViewModel.loadImageTypeReset()
                    }
                    LOAD_IMAGE_FROM_CAMERA -> {
                        getImageFromGalleryOrCamera(CAMERA_PERMISSION)
                        calendarViewModel.loadImageTypeReset()
                    }
                    IMAGE_DELETE -> {
                        setImageNullState()
                        calendarViewModel.loadImageTypeReset()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.changed_apply_confirm),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // 1. 툴바 관련 코드
        with(binding) {
            // #1 Top Toolbar
            toolbarTitleTextView.text = getString(
                R.string.diary_date,
                args.diaryBase.date.year,
                args.diaryBase.date.month,
                args.diaryBase.date.day
            )

            updatedYear = args.diaryBase.date.year
            updatedMonth = args.diaryBase.date.month
            updatedDay = args.diaryBase.date.day

            // 툴바 날짜 터치 시, 변경
            toolbarTitleTextView.setOnClickListener {
                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
                    // 캘린더 날짜 선택 후 확인 시,
                    toolbarTitleTextView.text = getString(R.string.diary_date, year, month + 1, day)
                    updatedYear = year
                    updatedMonth = month + 1
                    updatedDay = day
                }, args.diaryBase.date.year, args.diaryBase.date.month - 1, args.diaryBase.date.day)
                datePickerDialog.show()
            }

            with(dialogToolbar) {

                // X 터치 시 이전으로 돌아가는 코드
                setNavigationIcon(R.drawable.ic_close)
                if (getThemeType() == THEME_2) {
                    navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
                }
                setNavigationOnClickListener {
                    // 키보드 올라가 있다면 내리고,
                    titleText.clearFocusAndHideKeyboard(requireContext())
                    // 뒤로 가기 이동
                    findNavController().navigateUp()
                }


                menuDataSave.setOnClickListener {
                    updateData()
                }

            }

            // #2 Bottom Toolbar
            btnBottomToolbarImportance.setOnClickListener {
                checkImportance()
            }
            btnBottomToolbarDelete.setOnClickListener {
                deleteData()
            }

            // 해당 Data 의 정보를 가져와, imageButton 에 반영하는 코드
            // 수정 상태로 들어왔을 때만 작동하는 코드
            // 1.삭제여부 2.중요도 옵저버
            if (args.diaryBase.title != "") {
                btnBottomToolbarDelete.visibility = View.VISIBLE
                with(calendarViewModel) {
                    getDiaryBaseData(args.diaryBase.date, args.diaryBase.title)
                    diaryBaseImportance.observe(viewLifecycleOwner) { isImportant ->
                        if (isImportant != null) {
                            when (isImportant) {
                                true -> {
                                    diaryImportance.value = true
                                    diaryImportanceForUpdate = true
                                }
                                false -> {
                                    diaryImportance.value = false
                                    diaryImportanceForUpdate = false
                                }
                            }
                        }
                    }
                }
            }

            // 중요 여부를 반영하는 코드
            diaryImportance.observe(viewLifecycleOwner) { isImportance ->
                if (isImportance) {
                    btnBottomToolbarImportance.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_star
                        )
                    )
                    if (getThemeType() == THEME_2) {
                        btnBottomToolbarImportance.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.theme2_primary_touch_color
                            )
                        )
                    } else {
                        btnBottomToolbarImportance.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.important_color
                            )
                        )
                    }
                } else {
                    btnBottomToolbarImportance.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_star_border
                        )
                    )
                    if (getThemeType() == THEME_2) {
                        btnBottomToolbarImportance.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.theme2_primary_text_color
                            )
                        )
                    }
                }
            }
        }


        return binding.root
    }
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간

    private fun setImageNullState() {
        photoUri = null
        with(binding) {
            diaryImageView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            diaryImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_add_photo_52
                )
            )
        }
    }

    private fun spinnerSetting() {
        with(calendarViewModel) {
            getMyDrink(getString(R.string.spinner_first), getString(R.string.spinner_second))

            // 리스트 저장이 받는게 끝나면, 스피너 #1 을 동작시키는 코드
            getMyDrinkDoneEvent.observe(viewLifecycleOwner) {
                if (it) {
                    spinnerDrinkTypeListCall()

                    //스피너
                    // 폰트 지정 spinnerAdapter 를 가져오는 코드
                    val spinnerAdapter = getSpinnerAdapterWithCustomFont(spinnerDrinkTypeList)

                    binding.spinnerDrinkType.adapter = spinnerAdapter

                    // 전달된 데이터 선택 시, 해당값 출력하기
                    binding.spinnerDrinkType.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener { //object 선언 언제 하는거야????
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                // 스피너에서 선택한 그룹명
                                selectedDrinkType =
                                    spinnerDrinkTypeList[position] // 스피너의 어떤 데이터를 선택했느냐를 position 으로 알수가 있다. 그래서 get(position)으로 데이터 찾는거임

                                // 스피너 그룹명 선택에 따른 경우
                                when (selectedDrinkType) {
                                    // 1. " " 선택 시, 아무 변화 없음
                                    getString(R.string.spinner_first) -> {
                                        binding.drinkType.isEnabled = false
                                        binding.drinkType.setText(args.diaryBase.myDrink?.drinkType)
                                    }

                                    // 2. 직접입력 선택 시,
                                    getString(R.string.spinner_second) -> {
                                        // editText 사용가능하게 함
                                        binding.drinkType.isEnabled = true
                                        // 포커스 지정
                                        binding.drinkType.requestFocus()
                                        // 포커스 끝으로 보냄
                                        binding.drinkType.setSelection(binding.drinkType.text.length)
                                        // 키보드 올리기
                                        val mInputMethodManager =
                                            context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        mInputMethodManager.showSoftInput(
                                            binding.drinkType,
                                            InputMethodManager.SHOW_IMPLICIT
                                        )
                                    }
                                    // 3. 그 외, 추가된 실제 group 선택 시,
                                    else -> {
                                        binding.drinkType.isEnabled = false
                                        binding.drinkType.setText(selectedDrinkType)
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                }
            }
            // 리스트 저장이 받는게 끝나면, 스피너 #2 을 동작시키는 코드
            getMyDrinkDoneEvent.observe(viewLifecycleOwner) {
                if (it) {
                    spinnerPOAListCall()

                    //스피너
                    // 폰트 지정 spinnerAdapter 를 가져오는 코드
                    val spinnerAdapter = getSpinnerAdapterWithCustomFont(spinnerPOAList)

                    binding.spinnerPOA.adapter = spinnerAdapter

                    // 전달된 데이터 선택 시, 해당값 출력하기
                    binding.spinnerPOA.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener { //object 선언 언제 하는거야????
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                // 스피너에서 선택한 그룹명
                                selectedPOA =
                                    spinnerPOAList[position] // 스피너의 어떤 데이터를 선택했느냐를 position 으로 알수가 있다. 그래서 get(position)으로 데이터 찾는거임

                                // 스피너 그룹명 선택에 따른 경우
                                when (selectedPOA) {
                                    // 1. " " 선택 시, 아무 변화 없음
                                    getString(R.string.spinner_first) -> {
                                        binding.POA.isEnabled = false
                                        binding.POA.setText(args.diaryBase.myDrink?.POA)
                                    }

                                    // 2. 직접입력 선택 시,
                                    getString(R.string.spinner_second) -> {
                                        // editText 사용가능하게 함
                                        binding.POA.isEnabled = true
                                        // 포커스 지정
                                        binding.POA.requestFocus()
                                        // 포커스 끝으로 보냄
                                        binding.POA.setSelection(binding.POA.text.length)
                                        // 키보드 올리기
                                        val mInputMethodManager =
                                            context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        mInputMethodManager.showSoftInput(
                                            binding.POA,
                                            InputMethodManager.SHOW_IMPLICIT
                                        )
                                    }
                                    // 3. 그 외, 추가된 실제 group 선택 시,
                                    else -> {
                                        binding.POA.isEnabled = false
                                        binding.POA.setText(selectedPOA)
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                }
            }
            // 리스트 저장이 받는게 끝나면, 스피너 #3 을 동작시키는 코드
            getMyDrinkDoneEvent.observe(viewLifecycleOwner) {
                if (it) {
                    spinnerVODListCall()

                    //스피너
                    // 폰트 지정 spinnerAdapter 를 가져오는 코드
                    val spinnerAdapter = getSpinnerAdapterWithCustomFont(spinnerVODList)

                    binding.spinnerVOD.adapter = spinnerAdapter

                    // 전달된 데이터 선택 시, 해당값 출력하기
                    binding.spinnerVOD.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener { //object 선언 언제 하는거야????
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                // 스피너에서 선택한 그룹명
                                selectedVOD =
                                    spinnerVODList[position] // 스피너의 어떤 데이터를 선택했느냐를 position 으로 알수가 있다. 그래서 get(position)으로 데이터 찾는거임

                                // 스피너 그룹명 선택에 따른 경우
                                when (selectedVOD) {
                                    // 1. " " 선택 시, 아무 변화 없음
                                    getString(R.string.spinner_first) -> {
                                        binding.VOD.isEnabled = false
                                        binding.VOD.setText(args.diaryBase.myDrink?.VOD)
                                    }

                                    // 2. 직접입력 선택 시,
                                    getString(R.string.spinner_second) -> {
                                        // editText 사용가능하게 함
                                        binding.VOD.isEnabled = true
                                        // 포커스 지정
                                        binding.VOD.requestFocus()
                                        // 포커스 끝으로 보냄
                                        binding.VOD.setSelection(binding.VOD.text.length)
                                        // 키보드 올리기
                                        val mInputMethodManager =
                                            context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        mInputMethodManager.showSoftInput(
                                            binding.VOD,
                                            InputMethodManager.SHOW_IMPLICIT
                                        )
                                    }
                                    // 3. 그 외, 추가된 실제 group 선택 시,
                                    else -> {
                                        binding.VOD.isEnabled = false
                                        binding.VOD.setText(selectedVOD)
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                }
            }
        }
    }

    // 폰트가 적용된 스피너
    private fun getSpinnerAdapterWithCustomFont(list: List<String>): ArrayAdapter<String> {
        val spinnerAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item, list
        ) {
            // 선택된 스피너 font 적용
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val text = view.findViewById<View>(R.id.spinner_item) as TextView
                text.typeface = typeface
                if (getThemeType() == THEME_2) {
                    text.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme2_primary_background_color
                        )
                    )
                    text.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme2_primary_text_color
                        )
                    )
                }
                return view
            }

            // 드랍다운 스피너 font 적용
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val text = view.findViewById<View>(R.id.spinner_item) as TextView
                text.typeface = typeface
                if (getThemeType() == THEME_2) {
                    text.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme2_primary_background_color
                        )
                    )
                    text.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme2_primary_text_color
                        )
                    )
                }
                return view
            }
        }
        return spinnerAdapter
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun basicDiarySetting() {
        with(binding) {
            // 제목
            titleText.setText(args.diaryBase.title)
            // 내용
            contentText.setText(args.diaryBase.content)
            // 이미지
            if (args.diaryBase.image != null) {
                try {
                    photoUri = args.diaryBase.image
                    with(diaryImageView) {
                        imageTintList = null
                        diaryImageView.setImageBitmap(
                            decodeSampledBitmapFromInputStream(
                                args.diaryBase.image!!,
                                500,
                                500,
                                requireContext()
                            )
                        )
                    }
                } catch (e: FileNotFoundException) {
                    // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
                    // FileNotFoundException 에러 발생
                    setImageNullState()
                }
            } else {
                setImageNullState()
            }
            // 주종
            drinkType.setText(args.diaryBase.myDrink?.drinkType)
            // 도수
            POA.setText(args.diaryBase.myDrink?.POA)
            // 주량
            VOD.setText(args.diaryBase.myDrink?.VOD)


            // 키보드 다음 버튼 시, 포커스 지정 이동 #1
            drinkType.setOnEditorActionListener { textView, action, event ->
                var handled = false
                if (action == EditorInfo.IME_ACTION_NEXT) {
                    // 키보드 내리고
                    drinkType.clearFocusAndHideKeyboard(requireContext())

                    // spinnerPOA 로 포커스 이동
                    spinnerPOA.performClick()
                    handled = true
                }
                handled
            }

            // 키보드 다음 버튼 시, 포커스 지정 이동 #2
            POA.setOnEditorActionListener { textView, action, event ->
                var handled = false
                if (action == EditorInfo.IME_ACTION_NEXT) {
                    // 키보드 내리고
                    POA.clearFocusAndHideKeyboard(requireContext())

                    // spinnerPOA 로 포커스 이동
                    spinnerVOD.performClick()
                    handled = true
                }
                handled
            }

        }
    }

    // 권한 허용 및 카메라 작동 코드
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getImageFromGalleryOrCamera(permissions: Array<String>) {
        when (permissions) {
            GET_DATA_PERMISSIONS -> {
                if (!checkNeedPermissionBoolean(permissions)) {
                    // 허용 안되어 있는 경우, 요청
                    requestMultiplePermissionsForGallery.launch(
                        permissions
                    )
                } else {
                    // 허용 되어있는 경우, 코드 작동
                    // 갤러리 작동
                    getImageFromGallery()
                }
            }
            CAMERA_PERMISSION -> {
                if (!checkNeedPermissionBoolean(permissions)) {
                    // 허용 안되어 있는 경우, 요청
                    requestMultiplePermissionsForCamera.launch(
                        permissions
                    )
                } else {
                    // 허용 되어있는 경우, 코드 작동
                    // 카메라 작동
                    getImageFromCamera()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("IntentReset")
    private fun getImageFromGallery() {

//        val intent = Intent(Intent.ACTION_PICK)
//        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        intent.type = "image/*"
//        getImageFromGallery.launch(intent)

        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT // 이걸로 제한해서 열어줘야 함??
        getImageFromGallery.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode == Activity.RESULT_OK) {
                photoUri = it.data?.data as Uri
                setImageFromUri()
            }
        }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun getImageFromCamera() {
        // 원본 사진 공용 저장소 특정 폴더에 저장
        val values = ContentValues()

        // 저장 파일 이름 설정
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, newJpgFileName())

        // 저장 타입 설정
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")

        // 저장 경로 설정
//        values.put(
//            MediaStore.MediaColumns.RELATIVE_PATH,
//            "Pictures/${getString(R.string.app_name)}"
//        )

        values.put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            "Pictures/${getString(R.string.app_name)}"
        )



        photoUri =
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraAndSaveFile.launch(takePictureIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val cameraAndSaveFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode == Activity.RESULT_OK) {
                setImageFromUri()
            }
        }

    @SuppressLint("SimpleDateFormat")
    private fun newJpgFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setImageFromUri() {
        // 원본 사진은 지정 경로에 저장됨.
        with(binding) {
            diaryImageView.imageTintList = null
            diaryImageView.setImageBitmap(
                photoUri?.let {
                    decodeSampledBitmapFromInputStream(
                        it,
                        1500,
                        1500,
                        requireContext()
                    )
                }
            )
        }
    }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<String>): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // 허용 요청 코드 및 작동 #1(갤러리 가져오기)
    @RequiresApi(Build.VERSION_CODES.P)
    private val requestMultiplePermissionsForGallery =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_access),
                    Toast.LENGTH_SHORT
                ).show()
                // 갤러리 작동
                getImageFromGallery()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_gallery),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // GetImageDialog 를 열어 이미지를 세팅하는 함수
    private fun takeImage() {
        val bottomSheetDialog = GetImageDialog()
        bottomSheetDialog.show(childFragmentManager, "GetImageDialog")
    }


    // 허용 요청 코드 및 작동 #2(카메라 사용하기)
    @RequiresApi(Build.VERSION_CODES.P)
    private val requestMultiplePermissionsForCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_access),
                    Toast.LENGTH_SHORT
                ).show()
                // 카메라 작동
                getImageFromCamera()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // SharedPreferences 로부터 요소를 불러와서 spinner 에 추가하는 함수
    private fun spinnerDrinkTypeListCall() {
        spinnerDrinkTypeList.clear()
        val lastList = MyApplication.prefs.getString("drinkType", "None")
        val arrJson = JSONArray(lastList)
        for (i in 0 until arrJson.length()) {
            spinnerDrinkTypeList.add(arrJson.optString(i))
        }
    }

    // SharedPreferences 로부터 요소를 불러와서 spinner 에 추가하는 함수
    private fun spinnerPOAListCall() {
        spinnerPOAList.clear()
        val lastList = MyApplication.prefs.getString("POA", "None")
        val arrJson = JSONArray(lastList)
        for (i in 0 until arrJson.length()) {
            spinnerPOAList.add(arrJson.optString(i))
        }
    }

    // SharedPreferences 로부터 요소를 불러와서 spinner 에 추가하는 함수
    private fun spinnerVODListCall() {
        spinnerVODList.clear()
        val lastList = MyApplication.prefs.getString("VOD", "None")
        val arrJson = JSONArray(lastList)
        for (i in 0 until arrJson.length()) {
            spinnerVODList.add(arrJson.optString(i))
        }
    }

    // 저장 버튼 터치 시, 데이터를 업데이트하는 함수
    private fun updateData() {
        val newTitle = binding.titleText.text.trim().toString()

        // MyDrink 변수를 다 채우거나, 다 비우기 위한 로직
        var drinkTypeInt = 0
        var POAInt = 0
        var VODInt = 0
        if (binding.drinkType.text.trim().toString() != "") {
            drinkTypeInt = 1
        }
        if (binding.POA.text.trim().toString() != "") {
            POAInt = 1
        }
        if (binding.VOD.text.trim().toString() != "") {
            VODInt = 1
        }
        val total = drinkTypeInt + POAInt + VODInt


        if (newTitle == "") {
            // 제목이 빈칸이면
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_title),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!((total == 3) || (total == 0))) {
            // 주량기록이 완벽하지 않을 경우,(하나도 입력하지 않은 경우는 예외)
            Toast.makeText(
                requireContext(),
                getString(R.string.drink_records_error),
                Toast.LENGTH_SHORT
            ).show()
        } else if (total == 3) {
            // 주량 기록을 다 채웠는데,
            // drinkType 이 "데이터 없음" 일 때,
            if (updatedList().myDrink?.drinkType == getString(R.string.get_empty_DrinkTypePieChartList)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.change_drinkType_string),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                calendarViewModel.insertOrUpdateData(updatedList(), args.diaryBase)
            }
        } else {
            // 빈칸이 아닐 때, DB update 진행
            calendarViewModel.insertOrUpdateData(updatedList(), args.diaryBase)
        }
    }

    // 반영할 최신 updatedList
    private fun updatedList(): DiaryBase {
        val newImage = photoUri
        val newDate = MyDate(
            updatedYear,
            updatedMonth,
            updatedDay
        )

        val newTitle = binding.titleText.text.trim().toString()
        val newContent = binding.contentText.text.trim().toString()
        var newMyDrink: MyDrink? = MyDrink(
            binding.drinkType.text.trim().toString(),
            binding.POA.text.trim().toString(),
            binding.VOD.text.trim().toString()
        )

        // 반약 빈칸이면 MyDrink null 처리됨
        if (binding.drinkType.text.trim().toString() == "") {
            newMyDrink = null
        }

        val newImportance = diaryImportanceForUpdate
        var newBitmap: Bitmap? = null

        // photoUri 가 존재하면, bitmap 으로 변환하는 코드
        if (photoUri != null) {
            newBitmap = decodeSampledBitmapFromInputStream(photoUri!!, 50, 50, requireContext())
        }

        val updateList = DiaryBase(
            newImage,
            newDate,
            CalendarDay.from(updatedYear, updatedMonth - 1, updatedDay),
            newTitle,
            newContent,
            newMyDrink,
            newImportance,
            intToDateInt(updatedYear, updatedMonth, updatedDay),
            newBitmap,
            args.diaryBase.id
        )
        return updateList
    }


    private fun deleteData() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 해당 데이터 삭제 코드
            val deleteDate = MyDate(
                args.diaryBase.date.year,
                args.diaryBase.date.month,
                args.diaryBase.date.day
            )
            val deleteTitle = args.diaryBase.title
            calendarViewModel.deleteData(deleteDate, deleteTitle)
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ -> }
        builder.setTitle(getString(R.string.data_delete))
        builder.setMessage(getString(R.string.data_format_setMessage))
        builder.create().show()
    }


    // 실시간 반영 아님, 저장 시 기록됨
    private fun checkImportance() {
        diaryImportance.value = !diaryImportance.value!!
        diaryImportanceForUpdate = !diaryImportanceForUpdate
    }
}








