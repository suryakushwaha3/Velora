package com.example.velora.Utils

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
 import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://wyhrvcqusyieyexsuelb.supabase.co",
        supabaseKey = "sb_publishable_gMWFPY_Yj4mWKDYZ8TwQXw_MWCGtxxR"
    ) {
        install(Auth)
        install(Postgrest)
    }
}