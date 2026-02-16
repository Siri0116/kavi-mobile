package com.kavi.mobile

import android.app.Application
import com.kavi.mobile.data.KaviDatabase
import com.kavi.mobile.ai.ConversationContext
import kotlinx.coroutines.launch

class KaviApplication : Application() {

    lateinit var database: KaviDatabase
    lateinit var conversationContext: ConversationContext

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Database
        database = KaviDatabase.getDatabase(this)
        
        // Initialize Conversation Context (Memory)
        conversationContext = ConversationContext(database)
        
        // Load recent history in background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            conversationContext.loadRecentHistory()
        }
    }
}
