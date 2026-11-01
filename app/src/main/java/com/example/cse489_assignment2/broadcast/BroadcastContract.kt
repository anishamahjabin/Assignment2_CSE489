package com.example.cse489_assignment2.broadcast

object BroadcastContract {
    const val ACTION_CUSTOM_MESSAGE =
        "com.example.cse489_assignment2.action.CUSTOM_MESSAGE"
    const val EXTRA_MESSAGE = "extra_message"
    const val ARG_RECEIVER_TYPE = "receiver_type"
    const val ARG_MESSAGE = "message"
}

enum class ReceiverType {
    CUSTOM,
    BATTERY
}
