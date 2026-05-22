package com.app.fitt_buddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class RecipesFragment : Fragment() {

    private var tags: List<TextView> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recipe_vault, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tagAll = view.findViewById<TextView>(R.id.tag_all)
        val tagProtein = view.findViewById<TextView>(R.id.tag_protein)
        val tagCalories = view.findViewById<TextView>(R.id.tag_calories)
        val tagVegan = view.findViewById<TextView>(R.id.tag_vegan)

        tags = listOf(tagAll, tagProtein, tagCalories, tagVegan)

        tags.forEach { tag ->
            tag.isClickable = true
            tag.isFocusable = true
            tag.setOnClickListener {
                Log.d("RecipesFragment", "Tag clicked: ${tag.text}")
                selectTag(tag)
            }
        }
    }

    private fun selectTag(selectedTag: TextView) {
        tags.forEach { tag ->
            if (tag == selectedTag) {
                tag.setBackgroundResource(R.drawable.bg_capsule_selected)
                tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark))
            } else {
                tag.setBackgroundResource(R.drawable.bg_card_unselected)
                tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }
}