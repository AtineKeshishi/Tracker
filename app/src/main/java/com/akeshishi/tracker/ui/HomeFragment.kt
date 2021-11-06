package com.akeshishi.tracker.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.akeshishi.tracker.R
import com.akeshishi.tracker.adapters.MainAdapter
import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.base.extensions.makeGone
import com.akeshishi.tracker.base.extensions.makeVisible
import com.akeshishi.tracker.databinding.FragmentHomeBinding
import com.akeshishi.tracker.ui.viewmodels.MainViewModel
import com.akeshishi.tracker.util.*
import com.akeshishi.tracker.util.Constants.ACTION_DIALOG_TAG
import com.akeshishi.tracker.util.Constants.DIALOG_TAG
import com.akeshishi.tracker.util.Constants.KEY_ITEM_ID
import com.akeshishi.tracker.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.akeshishi.tracker.util.Utility.getShareData
import com.akeshishi.tracker.util.Utility.hasLocationPermissions
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var viewBinding: FragmentHomeBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mainAdapter: MainAdapter

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            restoreDialogAfterRotation(sharedPref.getInt(KEY_ITEM_ID, -1))
        }

        requestPermissions()
        setupToolbar()
        loadData()
        sortData()
        onButtonClick()
        backPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) = EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
        else
            requestPermissions()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        /** NO_ACTION */
    }

    private fun requestPermissions() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_rationale),
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_rationale),
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    private fun setupToolbar() {
        viewBinding.toolbar.sortBy.makeVisible()
        viewBinding.toolbar.txtTitle.makeGone()
    }

    private fun sortData() {
        viewBinding.toolbar.spFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { /** NO_ACTION */ }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> viewModel.sortActions(SortType.DATE)
                        1 -> viewModel.sortActions(SortType.RUNNING_TIME)
                        2 -> viewModel.sortActions(SortType.DISTANCE)
                        3 -> viewModel.sortActions(SortType.AVG_SPEED)
                        4 -> viewModel.sortActions(SortType.CALORIES_BURNED)
                    }
                }
            }
    }

    private fun loadData() {
        viewModel.actions.observe(viewLifecycleOwner) {
            setupAdapter(it)
        }
    }

    private fun setupAdapter(list: List<Action>) {
        mainAdapter = MainAdapter(requireContext(), list) { itemId ->
            sharedPref.edit().putInt(KEY_ITEM_ID, itemId).apply()
            showActionMenu(itemId)
        }
        viewBinding.rvList.adapter = mainAdapter
        showEmptyPage(mainAdapter.itemCount)
    }

    private fun showEmptyPage(itemCount: Int) {
        if (itemCount == 0) {
            viewBinding.rvList.makeGone()
            viewBinding.emptyPage.defaultEmptyPage.makeVisible()
            viewBinding.emptyPage.imgEmpty.makeVisible()
        } else {
            viewBinding.rvList.makeVisible()
            viewBinding.emptyPage.defaultEmptyPage.makeGone()
        }
    }

    private fun showDeleteDialog(id: Int) {
        CustomDialogFragment().apply {
            title = R.string.delete_item_title
            message = R.string.delete_item_message
            buttonFunction = { viewModel.deleteItem(id) }
        }.show(parentFragmentManager, DIALOG_TAG)

    }

    private fun showActionMenu(id: Int) {
        CustomActionDialogFragment().apply {
            shareFunction = { share(id) }
            deleteFunction = { showDeleteDialog(id) }
        }.show(parentFragmentManager, ACTION_DIALOG_TAG)
    }

    private fun restoreDialogAfterRotation(id: Int) {
        val actionDialog = parentFragmentManager.findFragmentByTag(ACTION_DIALOG_TAG) as
                CustomActionDialogFragment?

        actionDialog?.let {
            it.shareFunction = { share(id) }
            it.deleteFunction = { showDeleteDialog(id) }
        }

        val deleteDialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as
                CustomDialogFragment?
        deleteDialog?.let {
            it.title = R.string.delete_item_title
            it.message = R.string.delete_item_message
            it.buttonFunction = { viewModel.deleteItem(id) }
        }
    }

    private fun share(itemId: Int) {
        viewModel.getItem(itemId).observe(viewLifecycleOwner) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getShareData(it))
            }
            startActivity(Intent.createChooser(intent, null))
        }
    }

    private fun onButtonClick() {
        viewBinding.fab.setOnClickListener {
            if (hasLocationPermissions(requireContext()))
                findNavController().navigate(R.id.homeFragmentToTrackingFragment)
            else
                requestPermissions()
        }
    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        exitProcess(0)
                    }
                }
            )
    }
}