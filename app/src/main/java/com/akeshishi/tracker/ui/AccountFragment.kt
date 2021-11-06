package com.akeshishi.tracker.ui

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akeshishi.tracker.MainActivity
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.extensions.*
import com.akeshishi.tracker.databinding.FragmentAccountBinding
import com.akeshishi.tracker.ui.viewmodels.MainViewModel
import com.akeshishi.tracker.util.Constants
import com.akeshishi.tracker.util.Constants.KEY_HEIGHT
import com.akeshishi.tracker.util.Constants.KEY_NAME
import com.akeshishi.tracker.util.Constants.KEY_WEIGHT
import com.akeshishi.tracker.util.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.pow
import kotlin.system.exitProcess

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private lateinit var viewBinding: FragmentAccountBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            restoreDialogAfterRotation()
        }

        setupToolbar()
        loadUserInfo()
        saveChanges()
        loadBMI()
        backPressed()
        onButtonClick()
    }

    private fun setupToolbar() {
        viewBinding.toolbar.txtTitle.text = getString(R.string.personal_info)
        viewBinding.toolbar.txtCancel.makeVisible()
        viewBinding.toolbar.txtCancel.text = getString(R.string.logout)
    }

    private fun changeUserInfo(): Boolean {
        val name = viewBinding.edtName.text.toString()
        val weight = viewBinding.edtWeight.text.toString()
        val height = viewBinding.edtHeight.text.toString()

        if (name.isEmpty() || weight.isEmpty() || height.isEmpty()) return false

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putFloat(KEY_HEIGHT, height.toFloat())
            .apply()

        return true
    }

    private fun saveChanges() {
        viewBinding.btnApplyChanges.setOnClickListener {
            requireView().hideKeyboard()
            if (changeUserInfo()) {
                loadBMI()
            } else requireView().showSnackBar("Please fill the fields")
        }
    }

    private fun loadUserInfo() {
        viewBinding.edtName.setText(sharedPref.getString(KEY_NAME, ""))
        viewBinding.edtWeight.setText(sharedPref.getFloat(KEY_WEIGHT, 0f).toString())
        viewBinding.edtHeight.setText(sharedPref.getFloat(KEY_HEIGHT, 0f).toString())
    }

    private fun calculateBMI(): String {
        val weight = sharedPref.getFloat(KEY_WEIGHT, 0f)
        val height = sharedPref.getFloat(KEY_HEIGHT, 0f)
        return DecimalFormat("#.#").format((weight / height.pow(2)) * 10000)
    }

    private fun bmiRange(bmi: Float): String {
        return when {
            bmi < 16.0 -> "Severely Underweight"
            bmi > 16.0 && bmi < 18.4 -> "Underweight"
            bmi > 18.5 && bmi < 24.9 -> "Normal"
            bmi > 25.0 && bmi < 29.9 -> "Overweight"
            bmi > 30.0 && bmi < 34.9 -> "Moderately Obese"
            bmi > 35.0 && bmi < 39.9 -> "Severely Obese"
            bmi > 40.0 -> "Morbidly Obese"
            else -> "invalid"
        }
    }

    private fun normalWeight(): Pair<String, String> {
        val height = sharedPref.getFloat(KEY_HEIGHT, 0f)
        val minimumWeight = DecimalFormat("#.##").format(18.5 * (height / 100).pow(2))
        val maximumWeight = DecimalFormat("#.##").format(24.9 * (height / 100).pow(2))
        return Pair(minimumWeight, maximumWeight)
    }

    private fun setStatusTint(bmiRange: String) {
        when (bmiRange) {
            "Severely Underweight", "Underweight" -> changeStatusTint(R.color.bmi_underWeight)
            "Normal" -> changeStatusTint(R.color.bmi_normal)
            "Overweight" -> changeStatusTint(R.color.bmi_overWeight)
            "Moderately Obese", "Severely Obese", "Morbidly Obese" -> changeStatusTint(R.color.bmi_Obese)
        }
    }

    private fun changeStatusTint(color: Int) {
        viewBinding.imgStatus.setColorFilter(requireContext().resources.getColor(color))
    }

    private fun loadBMI() {
        viewBinding.txtBMI.text = calculateBMI()
        viewBinding.txtCategory.text = bmiRange(calculateBMI().toFloat())
        viewBinding.txtNormalWeight.text = HtmlCompat.fromHtml(
            getString(
                R.string.healthy_weight,
                normalWeight().first,
                normalWeight().second
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        setStatusTint(bmiRange(calculateBMI().toFloat()))
    }

    private fun onButtonClick() {
        viewBinding.toolbar.txtCancel.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun logout() {
        sharedPref.edit().clear().apply()
        viewModel.deleteAllItems()
        isFirstTime = true
        lifecycleScope.launch {
            delay(1000)
            exitProcess(0)
        }
    }

    private fun showLogoutDialog() {
        CustomDialogFragment().apply {
            title = R.string.logout
            message = R.string.logout_message
            buttonFunction = { logout() }
        }.show(parentFragmentManager, Constants.DIALOG_TAG)
    }

    private fun restoreDialogAfterRotation() {
        val dialog = parentFragmentManager.findFragmentByTag(Constants.DIALOG_TAG) as
                CustomDialogFragment?
        dialog?.let {
            it.title = R.string.logout
            it.message = R.string.logout_message
            it.buttonFunction = { logout() }
        }
    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        (requireActivity() as MainActivity).viewBinding
                            .bottomNavigation.selectedItemId = R.id.homeFragment
                    }
                }
            )
    }
}