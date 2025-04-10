package com.example.digitaldetox

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatbotActivity : AppCompatActivity() {

    private lateinit var chatHistory: LinearLayout
    private lateinit var userInput: EditText
    private lateinit var sendBtn: Button

    private val apiKey = "AIzaSyBoMOO1KUu7vT4ZdWKwXsq0eVR2ZMOQyyg" // Make sure this is valid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        chatHistory = findViewById(R.id.chatHistory)
        userInput = findViewById(R.id.etMessage)
        sendBtn = findViewById(R.id.btnSend)

        sendBtn.setOnClickListener {
            val message = userInput.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage("You", message)
                userInput.text.clear()
                fetchGeminiReply(message) // ✅ Fixed method name
            }
        }
    }

    private fun addMessage(sender: String, message: String) {
        val layoutId = if (sender == "You") R.layout.item_user_message else R.layout.item_bot_message
        val view = layoutInflater.inflate(layoutId, chatHistory, false)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message
        chatHistory.addView(view)

        // Auto-scroll
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }



    private fun fetchGeminiReply(message: String) {
        lifecycleScope.launch {
            // Show "Bot is typing..." message
            val typingView = TextView(this@ChatbotActivity).apply {
                text = "Bot is typing..."
                setPadding(16, 8, 16, 8)
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            chatHistory.addView(typingView)

            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash-002",
                    apiKey = apiKey
                )

                val prompt = """
                You are a helpful digital assistant for a Digital Detox app.
                Your role is to answer questions related to:
                - Reducing screen time
                - Improving focus
                - Recovering from mobile addiction
                - Creating healthy tech habits

                If the question is completely unrelated, say:
                "Sorry, I can only help with Digital Detox related questions."

                Now answer this question clearly and helpfully:
                User: $message
            """.trimIndent()

                val response = withContext(Dispatchers.IO) {
                    model.generateContent(prompt)
                }

                val reply = response.text ?: "Sorry, I didn't understand that."

                // Remove "typing..."
                chatHistory.removeView(typingView)

                // Add the bot reply
                addMessage("Bot", reply)

            } catch (e: Exception) {
                Log.e("Chatbot", "Gemini API Error", e)
                chatHistory.removeView(typingView)
                val errorMsg = e.localizedMessage ?: "Something went wrong"
                addMessage("Bot", "Oops! Something went wrong: $errorMsg")
            }
        }
    }

}
