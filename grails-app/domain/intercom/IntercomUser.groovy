package intercom

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum
import org.taack.User

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
        pubKeyContent maxSize: 8 * 8192, widget: 'textarea'
    }
}
