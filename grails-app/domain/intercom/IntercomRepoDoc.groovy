package intercom

import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileStatic
import taack.ast.annotation.TaackFieldEnum
import org.taack.User

@CompileStatic
enum IntercomDocumentCategory {
    USER_GUIDE,
    HOW_TOS,
    NEWS,
    TECHNICAL

}

@CompileStatic
enum IntercomDocumentKind {
    PAGE, SLIDESHOW
}

@TaackFieldEnum
@GrailsCompileStatic
class IntercomRepoDoc {
    Date dateCreated
    User userCreated
    IntercomRepo intercomRepo
    IntercomDocumentCategory category
    IntercomDocumentKind kind
    String abstractDesc
    String baseFilePath

    Date lastRevWhen
    String lastRevMessage
    String lastRevAuthor

    String docTitle
    String subtitle
    String authors

    IntercomTheme theme

    static constraints = {
        lastRevWhen nullable: true
        lastRevMessage nullable: true
        lastRevAuthor nullable: true
        docTitle nullable: true
        subtitle nullable: true
        authors nullable: true
        theme nullable: true
    }

    static mapping = {
        autoTimestamp true
    }

}
