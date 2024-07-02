package intercom


import attachment.TaackDocument
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileStatic
import taack.ast.annotation.TaackFieldEnum

@CompileStatic
enum IntercomDocumentKind {
    PAGE, SLIDESHOW
}

@TaackFieldEnum
@GrailsCompileStatic
class IntercomRepoDoc extends TaackDocument {

    IntercomRepo intercomRepo
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

}
