package de.rubeen.bsc.entities.web

class AppUserEntity {
    var id: Int? = null
    var name: String? = null
    var mail: String? = null
    var avatar: String? = null
    var position: String? = null

    constructor()

    constructor(id: Int?, name: String, mail: String, avatar: String, position: String) {
        this.id = id
        this.name = name
        this.mail = mail
        this.avatar = avatar
        this.position = position
    }
}
