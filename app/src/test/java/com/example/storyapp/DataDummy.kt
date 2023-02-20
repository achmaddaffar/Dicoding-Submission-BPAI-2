package com.example.storyapp

import com.example.storyapp.data.remote.response.ListStoryItem

object DataDummy {

    fun generateDummyQuoteResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "https://w7.pngwing.com/pngs/798/436/png-transparent-computer-icons-user-profile-avatar-profile-heroes-black-profile-thumbnail.png",
                "created at $i",
                "user $i",
                "description $i",
                i.toDouble(),
                "$i",
                i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}