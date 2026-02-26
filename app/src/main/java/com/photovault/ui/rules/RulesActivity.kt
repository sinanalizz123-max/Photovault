package com.photovault.ui.rules

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.photovault.databinding.ActivityRulesBinding

class RulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRulesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.rvRules.layoutManager = LinearLayoutManager(this)

        // Enable drag-to-reorder for priority
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean {
                // TODO: reorder adapter items and update priority_index in Room
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.rvRules)

        binding.btnAddRule.setOnClickListener { showAddRuleDialog() }

        // TODO: Observe rules from Room DB
        // checkForConflicts()
    }

    private fun showAddRuleDialog() {
        // TODO: Show bottom sheet to select rule type and configure
    }

    private fun checkForConflicts() {
        // TODO: Run conflict detection and show/hide conflict banner
        binding.tvConflictInfo.visibility = View.VISIBLE
    }
}
