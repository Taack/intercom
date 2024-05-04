package intercom

import grails.compiler.GrailsCompileStatic
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.MethodClosure
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Value
import taack.domain.TaackAttachmentService
import taack.domain.TaackSearchService
import taack.solr.SolrFieldType
import taack.solr.SolrSpecifier
import taack.ui.base.UiBlockSpecifier

import javax.annotation.PostConstruct
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

@GrailsCompileStatic
class IntercomSearchService implements TaackSearchService.IIndexService {

    static lazyInit = false

    TaackSearchService taackSearchService
    TaackAttachmentService taackAttachmentService

    @Value('${intranet.root}')
    String intranetRoot

    Path getTxtRootPath() {
        Path.of intranetRoot, 'intercom', 'txt'
    }

    String getOutAsciidocPath() {
        intranetRoot + '/intercom/asciidoc/out'
    }

    Path getHtmlFilePath(IntercomRepoDoc doc) {
        if (doc.baseFilePath?.endsWith('adoc')) Path.of outAsciidocPath, doc.intercomRepo.name, doc.baseFilePath.replace('adoc', 'html')
        else null
    }

    Path getTxtFilePath(IntercomRepoDoc doc) {
        Path.of txtRootPath.toString(), doc.id + '.txt'
    }

    @PostConstruct
    private void init() {
        FileUtils.forceMkdir(txtRootPath.toFile())

        taackSearchService.registerSolrSpecifier(this, new SolrSpecifier(IntercomRepoDoc, IntercomController.&viewDoc as MethodClosure, this.&labeling as MethodClosure, { IntercomRepoDoc d ->
            d ?= new IntercomRepoDoc()
            String content = docTxtContent(d)
            if (content || !d.id) {
                indexField "File Name (NOA)", SolrFieldType.TXT_NO_ACCENT, d.baseFilePath_
                indexField "File Name (GEN)", SolrFieldType.TXT_GENERAL, d.baseFilePath_
                indexField "Doc Title (NOA)", SolrFieldType.TXT_NO_ACCENT, d.docTitle_
                indexField "Doc Title (GEN)", SolrFieldType.TXT_GENERAL, d.docTitle_
                indexField "Authors", SolrFieldType.TXT_NO_ACCENT, d.authors_
                indexField "File Content", SolrFieldType.TXT_GENERAL, "fileContent", content
                indexField "Abstract", SolrFieldType.TXT_GENERAL, d.abstractDesc_
                indexField "Date Created", SolrFieldType.DATE, 0.5f, true, d.dateCreated_
                indexField "User Created", SolrFieldType.POINT_STRING, "userCreated", 0.5f, true, d.userCreated?.username
            }
        }))
    }

    String labeling(Long id) {
        def u = IntercomRepoDoc.read(id)
        "IntercomRepoDoc: ${u.baseFilePath} in ${u.intercomRepo.name} ($id) by ${u.userCreated.username}"
    }

    @Override
    List<? extends GormEntity> indexThose(Class<? extends GormEntity> toIndex) {
        if (toIndex.isAssignableFrom(IntercomRepoDoc)) return IntercomRepoDoc.findAllByKind(IntercomDocumentKind.PAGE)
        else null
    }

    UiBlockSpecifier buildSearchBlock(String q) {
        taackSearchService.search(q, IntercomController.&search as MethodClosure, IntercomRepoDoc)
    }

    String docTxtContent(IntercomRepoDoc doc) {
        Path htmlPath = getHtmlFilePath(doc)
        if (!htmlPath || !htmlPath.toFile().exists()) return
        Path txtPath = getTxtFilePath(doc)

        BasicFileAttributes htmlAttr = Files.readAttributes(htmlPath, BasicFileAttributes.class)
        FileTime htmlTime = htmlAttr.creationTime()

        boolean recompute = !txtPath.toFile().exists()

        if (!recompute) {
            BasicFileAttributes txtAttr = Files.readAttributes(txtPath, BasicFileAttributes.class)
            FileTime txtTime = txtAttr.creationTime()
            recompute = txtTime < htmlTime
        }

        if (!recompute)
            return txtPath.toFile().text
        else {
            File txt = txtPath.toFile()

            String htmlTxtContent = htmlPath.toFile().text
            String htmlTxtHeader = """\
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                """.stripIndent()
            String htmlTxt = htmlTxtHeader + (doc.kind == IntercomDocumentKind.SLIDESHOW ? '</head>' : '') + htmlTxtContent

            try (InputStream stream = new ByteArrayInputStream(htmlTxt.bytes)) {
                log.info "creating ${txtPath} from ${htmlPath}"
                txt << taackAttachmentService.fileContentToStringWithoutOcr(stream)
                return txt.text
            }
        }
    }
}

