package com.contacts.aomata.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.get
import androidx.recyclerview.widget.RecyclerView
import com.contacts.aomata.R
import com.contacts.aomata.model.Contact
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_contact.view.*
import kotlinx.android.synthetic.main.row_contact_data.view.*

class ContactsAdapter(context: Context) : RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var contacts = ArrayList<Contact>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(layoutInflater.inflate(R.layout.row_contact, parent, false))
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val contact = contacts[position]
        with(holder.itemView) {
            tvContactName.text = contact.name
            llContactDetails.removeAllViews()
            contact.numbers.forEach {
                val detail = layoutInflater.inflate(R.layout.row_contact_data,llContactDetails,false)
                detail.imgIcon.setImageResource(R.drawable.ic_baseline_local_phone_24)
                detail.tvContactData.text = it
                llContactDetails.addView(detail)
            }
            contact.emails.forEach {
                val detail = layoutInflater.inflate(R.layout.row_contact_data,llContactDetails,false)
                detail.imgIcon.setImageResource(R.drawable.ic_baseline_email_24)
                detail.tvContactData.text = it
                llContactDetails.addView(detail)
            }

            contact.dob.forEach {
                val detail = layoutInflater.inflate(R.layout.row_contact_data,llContactDetails,false)
                detail.imgIcon.setImageResource(R.drawable.ic_baseline_date_range_24)
                detail.tvContactData.text = it
                llContactDetails.addView(detail)
            }

            contact.photo.forEach {
                val detail = layoutInflater.inflate(R.layout.row_contact_data,llContactDetails,false)
                detail.imgIcon.setImageBitmap(it);
                llContactDetails.addView(detail)
            }

        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
