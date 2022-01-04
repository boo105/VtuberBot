package Api

import Data.APIKeys
import com.google.gson.Gson
import java.io.File

object APIKeyManager {
    private val apikeysString = File("./src/main/resources/APIKey.json").readText(Charsets.UTF_8)
    private val apiKeys = Gson().fromJson(apikeysString, APIKeys::class.java)

    fun getYoutubeAPIKEY() : String {
        return apiKeys.youtubeKey
    }

    fun getHoloDexAPIKey() : String {
        return apiKeys.holodexKey
    }
}