package com.yumetsuki.bcu

import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yumetsuki.bcu.androidutil.Definer
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.charagroup.LvrListPager
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.io.ErrorLogWriter
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData.UserPack
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LvRestrictList : AppCompatActivity() {

    lateinit var pack : UserPack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE)

        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_lvrestrict_list)
        pack = UserProfile.getUserPack(intent.extras?.getString("pack")) ?: return

        lifecycleScope.launch {
            //Prepare
            val groupList = findViewById<NestedScrollView>(R.id.lvrlistscroll)
            val status = findViewById<TextView>(R.id.status)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val tab = findViewById<TabLayout>(R.id.lvrlisttab)
            val pager = findViewById<ViewPager2>(R.id.lvrlistpager)
            val bck = findViewById<FloatingActionButton>(R.id.lvrback)
            progression.isIndeterminate = true

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@LvRestrictList, { _ -> }, { t -> runOnUiThread { status.text = t }})
            }

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            val keys = getExistingPack()
            pager.adapter = LvrListTab()
            pager.offscreenPageLimit = keys.size

            TabLayoutMediator(tab, pager) { t, position ->
                val def = getString(R.string.pack_default)

                t.text = when(position) {
                    0 -> def
                    else -> StaticStore.getPackName(keys[position])
                }
            }.attach()

            if(keys.size == 1) {
                tab.visibility = View.GONE

                val collapse = findViewById<CollapsingToolbarLayout>(R.id.lvrcollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }

            bck.setOnClickListener {
                finish()
            }

            StaticStore.setAppear(groupList)
            StaticStore.setDisappear(progression, status)
        }
    }

    private fun getExistingPack() : ArrayList<String> {
        val res = ArrayList<String>()
        res.add(Identifier.DEF)

        if (!pack.lvrs.isEmpty)
            res.add(pack.sid)
        for(str in pack.desc.dependency)
            if(!UserProfile.getUserPack(str).lvrs.isEmpty)
                res.add(str)
        return res
    }

    inner class LvrListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return LvrListPager.newInstance(keys[position])
        }
    }
}