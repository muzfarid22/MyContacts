package com.contacts.aomata.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.contacts.aomata.model.Contact
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ContactsViewModel(val mApplication: Application) : AndroidViewModel(mApplication) {

    private val _contactsLiveData = MutableLiveData<ArrayList<Contact>>()
    val contactsLiveData: LiveData<ArrayList<Contact>> = _contactsLiveData

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchContacts() {
        viewModelScope.launch {
            val contactsListAsync = async { getPhoneContacts() }
            val contactNumbersAsync = async { getContactNumbers() }
            val contactEmailAsync = async { getContactEmails() }
            val contactDOBAsync = async { getDOB() }
            val contactPHOTOAsync = async { getPhoto()}

            val contacts = contactsListAsync.await()
            val contactNumbers = contactNumbersAsync.await()
            val contactEmails = contactEmailAsync.await()
            val contactDOB = contactDOBAsync.await()
            val contactPHOTO = contactPHOTOAsync.await()

            contacts.forEach {
                contactNumbers[it.id]?.let { numbers ->
                    it.numbers = numbers
                }
                contactEmails[it.id]?.let { emails ->
                    it.emails = emails
                }
                contactDOB[it.id]?.let { dob ->
                    it.dob = dob
                }
                contactPHOTO[it.id]?.let { photo ->
                    it.photo = photo
                }
            }
            _contactsLiveData.postValue(contacts)
        }
    }

    private suspend fun getPhoneContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val contactsCursor = mApplication.contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        if (contactsCursor != null && contactsCursor.count > 0) {
            val idIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (contactsCursor.moveToNext()) {
                val id = contactsCursor.getString(idIndex)
                val name = contactsCursor.getString(nameIndex)
                if (name != null) {
                    contactsList.add(Contact(id, name))
                }
            }
            contactsCursor.close()
        }
        return contactsList
    }

    private suspend fun getContactNumbers(): HashMap<String, ArrayList<String>> {
        val contactsNumberMap = HashMap<String, ArrayList<String>>()
        val phoneCursor: Cursor? = mApplication.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (phoneCursor != null && phoneCursor.count > 0) {
            val contactIdIndex = phoneCursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (phoneCursor.moveToNext()) {
                val contactId = phoneCursor.getString(contactIdIndex)
                val number: String = phoneCursor.getString(numberIndex)
                //check if the map contains key or not, if not then create a new array list with number
                if (contactsNumberMap.containsKey(contactId)) {
                    contactsNumberMap[contactId]?.add(number)
                } else {
                    contactsNumberMap[contactId] = arrayListOf(number)
                }
            }
            //contact contains all the number of a particular contact
            phoneCursor.close()
        }
        return contactsNumberMap
    }

    private suspend fun getContactEmails(): HashMap<String, ArrayList<String>> {
        val contactsEmailMap = HashMap<String, ArrayList<String>>()
        val emailCursor = mApplication.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            null,
            null,
            null)
        if (emailCursor != null && emailCursor.count > 0) {
            val contactIdIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (emailCursor.moveToNext()) {
                val contactId = emailCursor.getString(contactIdIndex)
                val email = emailCursor.getString(emailIndex)
                //check if the map contains key or not, if not then create a new array list with email
                if (contactsEmailMap.containsKey(contactId)) {
                    contactsEmailMap[contactId]?.add(email)
                } else {
                    contactsEmailMap[contactId] = arrayListOf(email)
                }
            }
            //contact contains all the emails of a particular contact
            emailCursor.close()
        }
        return contactsEmailMap
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun getDOB(): Map<String, List<String>> {
        val nameBirth = mutableMapOf<String, List<String>>()

        // Retrieve name and id
        val resolver: ContentResolver = mApplication.contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cursor != null) {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE))
                    // Retrieve the birthday
                    val bd = mApplication.contentResolver
                    val bdc: Cursor? = bd.query(ContactsContract.Data.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Event.DATA),
                        ContactsContract.Data.CONTACT_ID + " = " + id + " AND " + ContactsContract.Data.MIMETYPE + " = '" +
                                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Event.TYPE +
                                " = " + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY, null, ContactsContract.Data.DISPLAY_NAME
                    )

                    if (bdc != null) {
                        if (bdc.count > 0) {
                            while (bdc.moveToNext()) {
                                // Using a list as key will prevent collisions on same name
                                val birthday: String = bdc.getString(0)
                                val person = listOf<String>(birthday)
                                nameBirth[id] = person
                            }
                        }
                        bdc.close()
                    }
                }
            }
        }
        cursor?.close()
        return nameBirth
    }

    @SuppressLint("Range")
    private fun getPhoto(): Map<String, List<Bitmap>>{
        val nameBirth = mutableMapOf<String, List<Bitmap>>()
        val resolver: ContentResolver = mApplication.contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                var photo: Bitmap? = null
                try {
                    val inputStream: InputStream? =
                        ContactsContract.Contacts.openContactPhotoInputStream(
                          mApplication.contentResolver,
                            ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI,
                                java.lang.Long.valueOf(id)
                            )
                        )
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream)
                        val person = listOf<Bitmap>( photo)
                        nameBirth[id] = person
                    }
                    if (inputStream != null) inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (cursor != null) {
            cursor.close()
        }
        return nameBirth
    }
}