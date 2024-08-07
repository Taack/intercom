package intercom

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum
import crew.User

@GrailsCompileStatic
@TaackFieldEnum
class IntercomUser {
    @Override
    String toString() {
        return "${baseUser.username}"
    }
    Date dateCreated
    Date lastUpdated

    User userCreated
    User userUpdated
    User baseUser

    String pubKeyContent

    static constraints = {
        baseUser unique: true
        pubKeyContent maxSize: 8 * 8192, widget: 'textarea'
    }
}
