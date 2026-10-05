package com.yumetsuki.bcu.androidutil.charagroup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.supports.AutoMarquee
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.CharaGroup
import common.util.stage.LvRestrict

class LvrListPager : Fragment() {

    companion object {
        fun newInstance(pid: String) : LvrListPager {
            val cs = LvrListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            cs.arguments = bundle

            return cs
        }
    }
    private var pid = Identifier.DEF

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val c = context ?: return null
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = UserProfile.getPack(pid) ?: return view
        if (p.lvrs.isEmpty)
            return view

        nores.visibility = View.GONE
        val csList = p.lvrs
        val adapter = LvRestrictionAdapter(c, csList.list)
        list.adapter = adapter
        list.onItemClickListener = AdapterView.OnItemClickListener { _, _, posit, _ ->
            if(SystemClock.elapsedRealtime() - StaticStore.cslistClick < StaticStore.INTERVAL)
                return@OnItemClickListener
            StaticStore.cslistClick = SystemClock.elapsedRealtime()

            val intent = Intent()
            intent.putExtra("Data", JsonEncoder.encode(csList[posit].id).toString())
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }
        return view
    }

    internal class LvRestrictionAdapter(private val c : Context, private val imgs : List<LvRestrict>) : ArrayAdapter<LvRestrict>(c, R.layout.listlayout, imgs) {
        inner class ViewHolder(row: View) {
            val text: TextView = row.findViewById(R.id.spinnertext)
        }


        override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
            val holder: ViewHolder
            val row: View

            if (view == null) {
                val inf = LayoutInflater.from(context)
                row = inf.inflate(R.layout.list_layout_text_icon, parent, false)
                holder = ViewHolder(row)
                row.tag = holder
            } else {
                row = view
                holder = row.tag as ViewHolder
            }
            holder.text.text = StaticStore.generateIdName(imgs[pos].id, c) + " - " + imgs[pos].name.toString()

            return row
        }
    }
}