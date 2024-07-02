package intercom

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum
import crew.User

@GrailsCompileStatic
@TaackFieldEnum
class IntercomRepo {
    Date dateCreated
    Date lastUpdated

    User userCreated
    User userUpdated

    IntercomUser owner

    String name
    String keyWords

    Set<IntercomUser> allowedEditors
    static hasMany = [
            allowedEditors: IntercomUser
    ]

    static constraints = {
        name unique: true
    }

    static mapping = {
        autoTimestamp true
    }
}
