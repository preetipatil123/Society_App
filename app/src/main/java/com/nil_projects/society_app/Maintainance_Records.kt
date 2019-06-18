package com.nil_projects.society_app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.diegodobelo.expandingview.ExpandingItem
import com.diegodobelo.expandingview.ExpandingList
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custon_maintainancerecords.view.*
import kotlinx.android.synthetic.main.expanding_sub_item.*
import kotlinx.android.synthetic.main.expanding_sub_item.view.*
import kotlinx.android.synthetic.main.fragment_maintainance__records.*
import kotlinx.android.synthetic.main.fragment_maintainance__records.view.*
import kotlinx.android.synthetic.main.fragment_report.*
import java.text.SimpleDateFormat
import java.util.*

class Maintainance_Records : Fragment() {

    lateinit var mExpandingList: ExpandingList
    lateinit var datePickerdialog : DatePickerDialog
    var formate = SimpleDateFormat("MMM,yyyy", Locale.US)
    lateinit var spin_paidornotpaid : Spinner
    var spin_value : String = "None"
    lateinit var select_month : EditText
    lateinit var db : FirebaseFirestore
    lateinit var selectedMonth : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maintainance__records, container, false)


        db = FirebaseFirestore.getInstance()


        //     recycler_maintainance = view.findViewById<RecyclerView>(R.id.recyclerview_xml_maintainancerecords)
        mExpandingList = view.findViewById<ExpandingList>(R.id.expanding_list_main)
        spin_paidornotpaid = view.findViewById<Spinner>(R.id.spinner_paidornotpaid)
        select_month = view.findViewById<EditText>(R.id.date_maintain_records)

        select_month.setOnClickListener {
            datePicker()
            checkSpinner()
        }

        checkSpinner()

        val Spinoptions = arrayOf("None","Paid","Not Paid")

        spin_paidornotpaid.adapter = ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,Spinoptions) as SpinnerAdapter?
        spin_paidornotpaid.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(activity,"Please Select",Toast.LENGTH_LONG).show()
                fetchMaintainancepaidMonths()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spin_value = Spinoptions.get(position)
                checkSpinner()
            }
        }
        return view
    }

    fun checkSpinner()
    {
        if(spin_value.equals("Paid"))
        {
            mExpandingList.removeAllViews()
            fetchMaintainancepaidMonths()
        }
        else if(spin_value.equals("Not Paid"))
        {
            Log.d("Counts",mExpandingList.itemsCount.toString())
            fetchMaintainanceNotpaidMonths()
        }
        else {

        }
    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR,year)
            selectedDate.set(Calendar.MONTH,month)
            selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            val date = formate.format(selectedDate.time)
            Toast.makeText(activity,"date : " + date, Toast.LENGTH_SHORT).show()
            date_maintain_records.setText(date)
            selectedMonth = date
        },
                now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH))
        datePickerdialog.show()
    }


    private fun fetchMaintainancepaidMonths() {

        var flatNo : String
        var wingname : String

        var db = FirebaseFirestore.getInstance()
        db.collection("FlatUsers")
                .whereEqualTo("SocietyName", "SIDDHIVINAYAK MANAS CO-OP. HOUSING SOCIETY")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                        documentSnapshot.documents.forEach {
                            val city = it.toObject(UserClass :: class.java)
                            for(i in 0..11)
                                db.collection("FlatUsers").document(city!!.UserID).collection("PaidMonths")
                                        .whereEqualTo("MonthsPaid$i", select_month.text.toString())
                                        .get()
                                        .addOnSuccessListener {
                                            it.documents.forEach {
                                                val monthsdata = it.toObject(months :: class.java)

                                                flatNo = city!!.FlatNo
                                                wingname = city!!.Wing

                                                if (monthsdata != null) {
                                                    //           adapter.add(FetchNotificationItem(monthsdata))

                                                    addItem(monthsdata.ReceiptNumber, arrayOf(monthsdata.MonthsPaid0,monthsdata.MonthsPaid1,monthsdata.MonthsPaid2,monthsdata.MonthsPaid3
                                                            ,monthsdata.MonthsPaid4,monthsdata.MonthsPaid5,monthsdata.MonthsPaid6
                                                            ,monthsdata.MonthsPaid7,monthsdata.MonthsPaid8,monthsdata.MonthsPaid9
                                                            ,monthsdata.MonthsPaid10,monthsdata.MonthsPaid11),flatNo,wingname,
                                                            R.color.md_pink_400, R.drawable.ic_ghost)
                                                }
                                            }
                                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("SocietyFirestore", "Error getting documents.", exception)
                }
    }


    private fun fetchMaintainanceNotpaidMonths() {

        var flatNo : String
        var wingname : String

        db.collection("FlatUsers")
                .whereEqualTo("SocietyName", "SIDDHIVINAYAK MANAS CO-OP. HOUSING SOCIETY")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.documents.forEach {
                        val city = it.toObject(UserClass :: class.java)
                        fetchMonthData(city!!.UserID,city.FlatNo,city.Wing)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("SocietyFirestore", "Error getting documents.", exception)
                }
    }

    private fun fetchMonthData(userid : String,flatNo : String,wingname: String)
    {
        db.collection("FlatUsers").document(userid).collection("PaidMonths")
                .get()
                .addOnSuccessListener {
                    it.documents.forEach {
                        val monthsdata = it.toObject(months :: class.java)

                        if (monthsdata!!.MonthsPaid0 != selectedMonth && monthsdata.MonthsPaid1 != selectedMonth
                                && monthsdata.MonthsPaid2 != selectedMonth && monthsdata.MonthsPaid3 != selectedMonth
                                && monthsdata.MonthsPaid4 != selectedMonth && monthsdata.MonthsPaid5 != selectedMonth
                                && monthsdata.MonthsPaid6 != selectedMonth && monthsdata.MonthsPaid7 != selectedMonth
                                && monthsdata.MonthsPaid8 != selectedMonth && monthsdata.MonthsPaid9 != selectedMonth
                                && monthsdata.MonthsPaid10 != selectedMonth && monthsdata.MonthsPaid11 != selectedMonth) {

                            Log.d("Count",flatNo)

                            addItem("Pending", arrayOf(""),flatNo,wingname,
                                    R.color.md_pink_400, R.drawable.ic_ghost)

                        }
                    }
                }
    }


    private fun addItem(title: String, subItems: Array<String>,flatNo : String,wingname : String, colorRes: Int, iconRes: Int) {
        //Let's create an item with R.layout.expanding_layout
        val item = mExpandingList!!.createNewItem(R.layout.expanding_layout)

        //If item creation is successful, let's configure it
        if (item != null) {
            item.setIndicatorColorRes(colorRes)
            item.setIndicatorIconRes(iconRes)
            //It is possible to get any view inside the inflated layout. Let's set the text in the item
            (item.findViewById(R.id.title) as TextView).text = title
            (item.findViewById(R.id.custom_flatno) as TextView).text = flatNo
            (item.findViewById(R.id.custom_societyname) as TextView).text = wingname

            //We can create items in batch.
            item.createSubItems(subItems.size)
            for (i in 0 until item.subItemsCount) {
                //Let's get the created sub item by its index
                val view = item.getSubItemView(i)

                //Let's set some values in
                configureSubItem(item, view, subItems[i])
            }

//            item.findViewById<ImageView>(R.id.add_more_sub_items).setOnClickListener(View.OnClickListener {
//                showInsertDialog(object : OnItemCreated {
//                    override fun itemCreated(title: String) {
//                        val newSubItem = item.createSubItem()
//                        configureSubItem(item, newSubItem!!, title)
//                    }
//                })
//            })

//            item.findViewById<ImageView>(R.id.remove_item)
//                    .setOnClickListener(View.OnClickListener { mExpandingList!!.removeItem(item) })
        }
    }

    private fun configureSubItem(item: ExpandingItem?, view: View, subTitle: String) {
        (view.findViewById(R.id.sub_title) as TextView).text = subTitle
        if(subTitle == "")
        {
            item!!.removeSubItem(view)
        }
//        view.findViewById<ImageView>(R.id.remove_sub_item)
//                .setOnClickListener(View.OnClickListener { item!!.removeSubItem(view) })
    }

//    private fun showInsertDialog(positive: OnItemCreated) {
//        val text = EditText(activity)
//        val builder = AlertDialog.Builder(activity)
//        builder.setView(text)
//        builder.setTitle("Enter Title")
//        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which -> positive.itemCreated(text.text.toString()) })
//        builder.setNegativeButton(android.R.string.cancel, null)
//        builder.show()
//    }

//    internal interface OnItemCreated {
//        fun itemCreated(title: String)
//    }

//    inner class FetchNotificationItem(var Finalnotifi: months) : Item<ViewHolder>() {
//
//        override fun getLayout(): Int {
//            return R.layout.custon_maintainancerecords
//        }
//
//        override fun bind(viewHolder: ViewHolder, position: Int) {
//            viewHolder.itemView.receipt_no_custom.text = Finalnotifi.ReceiptNumber
//        }
//    }
}

class months(val MonthsPaid0: String,val MonthsPaid1 : String,val MonthsPaid2 : String,val MonthsPaid3: String,val MonthsPaid4: String,
             val MonthsPaid5: String,val MonthsPaid6: String,val MonthsPaid7: String,val MonthsPaid8: String,
             val MonthsPaid9: String,val MonthsPaid10: String,val MonthsPaid11: String,
             val ReceiptNumber : String)
{
    constructor() : this("","","","","","",
            "","","","","","","")
}