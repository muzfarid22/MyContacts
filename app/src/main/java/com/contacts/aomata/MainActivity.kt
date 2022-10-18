package com.contacts.aomata

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.contacts.aomata.adapter.ContactsAdapter
import com.contacts.aomata.utils.hasPermission
import com.contacts.aomata.utils.requestPermissionWithRationale
import com.contacts.aomata.viewmodel.ContactsViewModel
import kotlinx.android.synthetic.main.main_activity_2.*

class MainActivity : AppCompatActivity() {
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private val CONTACTS_READ_REQ_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_2)
        init()
    }

    private fun init() {
        tvDefault.text = "Fetching contacts!!!"
        val adapter = ContactsAdapter(this)
        rvContacts.adapter = adapter
        contactsViewModel.contactsLiveData.observe(this, Observer {
            tvDefault.visibility = View.GONE
            adapter.contacts = it
        })
        if (hasPermission(Manifest.permission.READ_CONTACTS)) {
            contactsViewModel.fetchContacts()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.READ_CONTACTS,
                CONTACTS_READ_REQ_CODE,
                getString(R.string.app_name)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_READ_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            contactsViewModel.fetchContacts()
        }
    }
}