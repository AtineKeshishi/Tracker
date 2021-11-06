package com.akeshishi.tracker.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.extensions.*
import com.akeshishi.tracker.databinding.FragmentSetupBinding
import com.akeshishi.tracker.util.Constants.FIRST_TIME_LOGIN
import com.akeshishi.tracker.util.Constants.KEY_HEIGHT
import com.akeshishi.tracker.util.Constants.KEY_NAME
import com.akeshishi.tracker.util.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var viewBinding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSetupBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onButtonClick()
        navigateToHomePage()
    }

    private fun saveUserInfo(): Boolean {
        val name = viewBinding.edtName.text.toString()
        val weight = viewBinding.edtWeight.text.toString()
        val height = viewBinding.edtHeight.text.toString()

        if (name.isEmpty() || weight.isEmpty() || height.isEmpty()) return false

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putFloat(KEY_HEIGHT, height.toFloat())
            .putBoolean(FIRST_TIME_LOGIN, false)
            .apply()

        return true
    }

    private fun navigateToHomePage() {
        if (!isFirstTime)
            findNavController().navigate(R.id.setupFragmentToHomeFragment)

        viewBinding.txtNext.setOnClickListener {
            if (saveUserInfo()) {
                findNavController().navigate(R.id.setupFragmentToHomeFragment)
            } else Toast.makeText(requireContext(), "Please fill the fields", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun reset() {
        if (viewBinding.edtName.text.isNotEmpty() ||
            viewBinding.edtWeight.text.isNotEmpty() ||
            viewBinding.edtHeight.text.isNotEmpty()
        )
            viewBinding.txtReset.makeVisible()

        viewBinding.txtReset.setOnClickListener {
            viewBinding.edtName.text.clear()
            viewBinding.edtWeight.text.clear()
            viewBinding.edtHeight.text.clear()
            viewBinding.txtReset.makeGone()
            viewBinding.txtNext.makeUnClickable()
            viewBinding.txtNext.setTextColor(resources.getColor(R.color.blue_gray_500))
        }
    }

    private fun onButtonClick(){
        viewBinding.txtNext.makeUnClickable()
        viewBinding.edtName.textChangeMonitor { reset() }
        viewBinding.edtHeight.textChangeMonitor {
            viewBinding.txtNext.setTextColor(resources.getColor(R.color.amber_800))
            viewBinding.txtNext.makeClickable()
        }
    }
}