package de.rubeen.bsc.entities.web

class LoginUser {
    var id: Int? = null
    var email: String? = null
    var password: String? = null

    constructor()

    constructor(email: String, password: String) {
        this.email = email
        this.password = password
    }
}
