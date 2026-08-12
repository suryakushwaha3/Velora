package com.example.velora.socket

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {
    private lateinit var socket: Socket

    fun setSocket() {
        try {
            socket = IO.socket("http://192.168.29.249:3000")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun getSocket(): Socket {
        return socket
    }

    fun establishConnection() {
        if (::socket.isInitialized) {
            if (!socket.connected()) {
                socket.connect()

                // Connection successful hone par server ko batayein ki user online ho gaya hai
                socket.on(Socket.EVENT_CONNECT) {
                    socket.emit("user_status", true)
                }

                // Disconnect hone par offline status update karein
                socket.on(Socket.EVENT_DISCONNECT) {
                    // Optional handling

                }
            }
        }
    }

    fun closeConnection() {
        if (::socket.isInitialized) {
            socket.emit("user_status", false) // App band hone par offline bhejein
            socket.disconnect()
        }
    }
}