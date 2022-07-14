package com.dreamreco.joodiary.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.dreamreco.joodiary.MainActivity
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.ActivityBioScreenLockBinding
import com.dreamreco.joodiary.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.*
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@AndroidEntryPoint
class BioScreenLockActivity : AppCompatActivity() {

    private val binding by lazy { ActivityBioScreenLockBinding.inflate(layoutInflater) }

    // 생체 인식 인증 추가를 위한 코드
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var typeface: Typeface? = null

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                authenticateToEncrypt()  //생체 인증 가능 여부확인 다시 호출
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 테마 설정 코드
        when (MyApplication.prefs.getString(THEME_TYPE, THEME_BASIC)) {
            // 기본 테마
            THEME_BASIC -> {
                setTheme(R.style.Theme_JooDiary)
            }
            // 테마 1
            THEME_1 -> {
                setTheme(R.style.NewCustomAppTheme)
            }
            // 테마 2
            THEME_2 -> {
                setTheme(R.style.SoundCustomAppTheme)
                binding.bioToolbarLogo.imageTintList = ContextCompat.getColorStateList(this,R.color.white)
                binding.viewNumber1.imageTintList = ContextCompat.getColorStateList(this,R.color.white)
            }
        }


        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = getFontType(this)
        setGlobalFont(binding.root, typeface!!)

        // 로그인 기본 설정
        setBiometricLogin()

        // 암호화 설정
        generateSecretKey(
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        )

        // 생체 인식 진행
        authenticateToEncrypt()
    }


    private fun authenticateToEncrypt() {
        val biometricManager = BiometricManager.from(this)
        // BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // 인증창 띄우기
                val cipher = getCipher()
                val secretKey = getSecretKey()
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                biometricPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(cipher)
                )
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, getString(R.string.BIOMETRIC_ERROR_NONE_ENROLLED) , Toast.LENGTH_SHORT).show()
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                loginLauncher.launch(enrollIntent)
            }
        }
    }

    private fun setBiometricLogin() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                // 로그인 에러 시,
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    finishAffinity()
                }

                // 로그인 성공 시,
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    // 암호화 정보
                    val encryptedInfo: ByteArray = result.cryptoObject?.cipher?.doFinal(
                        "일반텍스트-스트링??".toByteArray(Charset.defaultCharset())
                    )!!
                    Log.e(
                        "MY_APP_TAG", "Encrypted information: " +
                                Arrays.toString(encryptedInfo)
                    )

                    MyApplication.prefs.setString(LOGIN_STATE, LOGIN_CLEAR)
                    val intent = Intent(this@BioScreenLockActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                // 로그인 실패 시,
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                }
            })

        // 로그인 창 설정
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.promptInfo_title))
            .setSubtitle(getString(R.string.promptInfo_sub_title))
            .setNegativeButtonText(getString(R.string.promptInfo_negativeButtonText))
            .build()
    }

    // 인증 암호화 함수들
    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    // 로그인 하지 않고, 뒤로가기를 눌렀을 때, 동작하는 코드
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

