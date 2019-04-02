package com.lib.adloader.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class MediaModel {


    @SerializedName("autoplay")
    @Expose
    var autoplay: Boolean = false
    @SerializedName("media")
    @Expose
    var media: List<Medium> = emptyList()
    @SerializedName("playlist")
    @Expose
    var playlist: Playlist = Playlist()
    @SerializedName("ad_priority")
    @Expose
    var adPriority: String = ""
    @SerializedName("auth")
    @Expose
    var auth: String = ""
    @SerializedName("mute")
    @Expose
    var mute: Boolean = false
    @SerializedName("volume")
    @Expose
    var volume: String = ""
    @SerializedName("tracking")
    @Expose
    var tracking: Tracking = Tracking()
    @SerializedName("related")
    @Expose
    var related: String = ""
    @SerializedName("logo")
    @Expose
    var logo: Logo = Logo()
    @SerializedName("sharing")
    @Expose
    var sharing: Boolean = false


    inner class Ads {

        @SerializedName("marketplace")
        @Expose
        var marketplace: List<Marketplace> = emptyList()
        @SerializedName("publisher")
        @Expose
        var publisher: List<String> = emptyList()

    }

    inner class Logo {

        @SerializedName("img")
        @Expose
        var img: String = ""
        @SerializedName("url")
        @Expose
        var url: String = ""

    }

    inner class Marketplace {

        @SerializedName("id")
        @Expose
        var id: Int = 0
        @SerializedName("url")
        @Expose
        var url: String = ""
        @SerializedName("client")
        @Expose
        var client: String = ""
        @SerializedName("time")
        @Expose
        var time: Int = 0
        @SerializedName("type")
        @Expose
        var type: String = ""
        @SerializedName("fallback")
        @Expose
        var fallback: Boolean = false

    }

    inner class Medium {

        @SerializedName("id")
        @Expose
        var id: Long = 0
        @SerializedName("title")
        @Expose
        var title: String = ""
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String = ""
        @SerializedName("duration")
        @Expose
        var duration: Int = 0
        @SerializedName("file")
        @Expose
        var file: String = ""
        @SerializedName("ads")
        @Expose
        var ads: Ads = Ads()
        @SerializedName("description")
        @Expose
        var description: String = ""

    }

    inner class Playlist {

        @SerializedName("id")
        @Expose
        var id: Int = 0
        @SerializedName("name")
        @Expose
        var name: String = ""
        @SerializedName("description")
        @Expose
        var description: String = ""
        @SerializedName("show")
        @Expose
        var show: Boolean = false
        @SerializedName("position")
        @Expose
        var position: String = ""
        @SerializedName("repeat")
        @Expose
        var repeat: Boolean = false
        @SerializedName("theme")
        @Expose
        var theme: String = ""

    }

    inner class Tracking {

        @SerializedName("throttle")
        @Expose
        var throttle: Int = 0
        @SerializedName("readyDelay")
        @Expose
        var readyDelay: Int = 0
        @SerializedName("url")
        @Expose
        var url: String = ""
        @SerializedName("events")
        @Expose
        var events: List<String> = emptyList()
        @SerializedName("ignoreEvents")
        @Expose
        var ignoreEvents: List<Any> = emptyList()

    }

}