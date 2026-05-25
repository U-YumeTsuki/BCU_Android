package com.yumetsuki.bcu.androidutil.charagroup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.supports.AutoMarquee
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.CharaGroup

class CgListPager : Fragment() {

    companion object {
        fun newInstance(pid: String) : CgListPager {
            val cs = CgListPager()
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

        val p = UserProfile.getPack(pid)
        var index = -1

        return view
    }

    internal class CastleAdapter(private val c : Context, private val imgs : List<CharaGroup>) : ArrayAdapter<CharaGroup>(c, R.layout.listlayout, imgs) {
        inner class ViewHolder(row: View) {
            val id = row.findViewById<AutoMarquee>(R.id.CGID)!!
            val title = row.findViewById<TextView>(R.id.cgdesc)!!
            val image = row.findViewById<ImageView>(R.id.cgexpand)!!
            val fadeout = row.findViewById<View>(R.id.cgdesc)!!
        }
    }
}