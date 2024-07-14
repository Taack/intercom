package intercom

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import org.apache.commons.io.FileUtils
import org.asciidoctor.*
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import org.asciidoctor.log.LogHandler
import org.asciidoctor.log.LogRecord
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import taack.ui.TaackUiConfiguration

import javax.annotation.PostConstruct

@GrailsCompileStatic
class IntercomAsciidoctorConverterService {

    @Value('${intranet.root}')
    String intranetRoot

    String getGitRootPath() {
        intranetRoot + '/intercom/git-root'
    }

    String getAsciidocPath() {
        intranetRoot + '/intercom/asciidoc'
    }

    @Autowired
    TaackUiConfiguration taackUiConfiguration

    @Value('${exe.vega.bin}')
    String vegaBinPath

    private Random random = new Random()

    @PostConstruct
    private void init() {
        FileUtils.forceMkdir(new File(gitRootPath))
        FileUtils.forceMkdir(new File(asciidocPath + '/in'))
        FileUtils.forceMkdir(new File(asciidocPath + '/out'))
        initAsciidoctorJ()
    }

    static final class Slideshow {
        static final class RevealJSOptions {
            final String parallaxBackgroundImage
            final String parallaxBackgroundSize
            final Integer parallaxBackgroundHorizontal
            final Integer parallaxBackgroundVertical
            final Integer width
            final Integer height
            final Boolean controls
            final Boolean progress
            final Integer autoSlide

            RevealJSOptions(final Map<String, String> revealJsOptionMap) {
                parallaxBackgroundImage = revealJsOptionMap["parallaxBackgroundImage"]
                parallaxBackgroundSize = revealJsOptionMap["parallaxBackgroundSize"]
                parallaxBackgroundHorizontal = revealJsOptionMap["parallaxBackgroundHorizontal"] ? Integer.parseInt(revealJsOptionMap["parallaxBackgroundHorizontal"]) : null
                parallaxBackgroundVertical = revealJsOptionMap["parallaxBackgroundVertical"] ? Integer.parseInt(revealJsOptionMap["parallaxBackgroundVertical"]) : null
                width = revealJsOptionMap["width"] ? Integer.parseInt(revealJsOptionMap["width"]) : 960
                height = revealJsOptionMap["height"] ? Integer.parseInt(revealJsOptionMap["height"]) : 480
                autoSlide = revealJsOptionMap["autoSlide"] ? Integer.parseInt(revealJsOptionMap["autoSlide"]) : 3000
                controls = revealJsOptionMap["controls"] ? Boolean.parseBoolean(revealJsOptionMap["controls"]) : true
                progress = revealJsOptionMap["progress"] ? Boolean.parseBoolean(revealJsOptionMap["progress"]) : true
            }

            String toJson() {
                StringBuffer out = new StringBuffer()
                if (parallaxBackgroundImage) out.append("parallaxBackgroundImage: '/img/${parallaxBackgroundImage}',\n")
                if (parallaxBackgroundSize) out.append("parallaxBackgroundSize: '${parallaxBackgroundSize}',\n")
                if (parallaxBackgroundHorizontal) out.append("parallaxBackgroundHorizontal: ${parallaxBackgroundHorizontal},\n")
                if (parallaxBackgroundVertical) out.append("parallaxBackgroundVertical: ${parallaxBackgroundVertical},\n")
                if (width) out.append("width: ${width},\n")
                if (height) out.append("height: ${height * 3 / 2},\n")
                if (controls != null) out.append("controls: ${controls},\n")
                if (progress != null) out.append("progress: ${progress},\n")
                if (autoSlide != null) out.append("autoSlide: ${autoSlide},\n")
                out.toString()
            }
        }

        final File fileHtml
        final RevealJSOptions revealJsOptions

        Slideshow(final File fileHtml, final Map<String, String> revealJsOptionMap) {
            this.fileHtml = fileHtml
            this.revealJsOptions = new RevealJSOptions(revealJsOptionMap)
        }
    }

    Slideshow processDoc(IntercomRepoDoc doc, boolean pdfMode = false) {
        File contentDir = new File(asciidocPath + '/in/' + doc.intercomRepo.name)
        if (!contentDir.exists()) {
            try {
                Git result = Git.cloneRepository()
                        .setURI(gitRootPath + '/' + doc.intercomRepo.name)
                        .setDirectory(contentDir)
                        .call()
                result.close()
            } catch (e) {
                log.error "Cannot clone local repo ${gitRootPath + '/' + doc.intercomRepo.name}: ${e.message}"
                throw e
            }
        } else {
            try {
                Git git = Git.open(contentDir)
                PullResult result = git.pull().call()
                RevCommit lastRev = git.log().call().first()

                if (lastRev.authorIdent.when != doc.lastRevWhen) {
                    IntercomRepoDoc.withNewTransaction {
                        doc.lastRevWhen = lastRev.authorIdent.when
                        doc.lastRevAuthor = lastRev.authorIdent.name
                        doc.lastRevMessage = lastRev.shortMessage
                        doc.save(flush: true)
                        if (doc.hasErrors()) log.error "${doc.errors}"
                    }
                }

                if (result.isSuccessful()) {
                    log.info "git pull OK"
                } else {
                    log.error "git pull failed ..."
                }
                git.close()
            } catch (e) {
                log.error "Cannot clone local repo ${gitRootPath + '/' + doc.intercomRepo.name}: ${e.message}"
                throw e
            }
        }
        processIndexFile(doc, pdfMode)
    }

    final Iterable<String> docHistory(IntercomRepoDoc doc) {
        File contentDir = new File(asciidocPath + '/in/' + doc.intercomRepo.name)
        Git git = Git.open(contentDir)
        Iterable<RevCommit> commitIterable = git.log().call()
        commitIterable.collect {
            PersonIdent p = it.getCommitterIdent()
            "${p.name}(${p.emailAddress}) at ${p.when}: <br><b>${it.getFullMessage()}</b>" as String
        }
    }

    final private String docPath(IntercomRepoDoc doc) {
        asciidocPath + '/in/' + doc.intercomRepo.name + "/" + doc.baseFilePath
    }

    final private static String docRelativeFilePath(IntercomRepoDoc doc) {
        doc.baseFilePath
    }

    final private static String docRelativeDirPath(IntercomRepoDoc doc) {
        if (doc.baseFilePath.contains('/')) "/" + doc.baseFilePath - doc.baseFilePath.tokenize('/').last()
        else "/"
    }

    final String docOutputTree(IntercomRepoDoc doc) {
        asciidocPath + "/out/" + doc.intercomRepo.name
    }

    final private String docContentTree(IntercomRepoDoc doc) {
        asciidocPath + "/in/" + doc.intercomRepo.name
    }

    private static Asciidoctor asciidoctor

    private static Asciidoctor initAsciidoctorJ() {
        if (!asciidoctor)
            try {
                asciidoctor = Asciidoctor.Factory.create()
            } catch (Exception e) {
                println e
            } catch (Throwable t) {
                println t
            }
        asciidoctor
    }

    private final Slideshow processIndexFile(final IntercomRepoDoc doc, boolean pdfMode = false) {
        final useRevealJS = doc.kind == IntercomDocumentKind.SLIDESHOW

        final File indexFile = new File(docPath(doc))
        final String rpd = docRelativeDirPath(doc)
        final String rp = docRelativeFilePath(doc)
        final String htmlFilePath = rp.replace('.adoc', pdfMode ? '.pdf' : '.html')
        final String outputTree = docOutputTree(doc)
        final String contentTree = docContentTree(doc)
        String stringHtml
        File html = new File("${outputTree}/${htmlFilePath}")
        Slideshow slideshow = null
        Asciidoctor asciidoctor = initAsciidoctorJ()
        if (useRevealJS) {
            final List<String> revealJsLines = indexFile.text.readLines().findAll { it.startsWith(":revealjs_") }
            final Map<String, String> revealJsOptions = revealJsLines.collectEntries {
                int pos = it.lastIndexOf(':')
                final String k = it.substring(10, pos)
                final String v = it.substring(pos + 2)
                new MapEntry(k, v)
            }
            slideshow = new Slideshow(html, revealJsOptions)
            asciidoctor.requireLibrary("asciidoctor-diagram", "asciidoctor-revealjs")
        } else {
            slideshow = new Slideshow(html, [:])
            asciidoctor.requireLibrary("asciidoctor-diagram", "asciidoctor-pdf")
        }
        asciidoctor.registerLogHandler(new LogHandler() { // (1)
            @Override
            void log(LogRecord logRecord) {
                println(logRecord.getMessage())
                //logRecord.getMessage()
            }
        })
        asciidoctor.javaExtensionRegistry().blockMacro(new SlideBlockMacroProcessor(outputTree))

        StringBuffer indexFileTransformed = new StringBuffer()
        boolean contextStartWithGnuplot = false
        boolean appendCd = false
        indexFile.eachLine {
            // TODO: add cd at the beginning of gnuplot
            if (it.startsWith("[gnuplot")) {
                contextStartWithGnuplot = true
            } else if (contextStartWithGnuplot) {
                if (it.startsWith("----"))
                    appendCd = true
                contextStartWithGnuplot = false
            }
            indexFileTransformed.append it
            indexFileTransformed.append "\n"
            if (appendCd) indexFileTransformed.append "cd ${contentTree}${rpd}\n"
            appendCd = false
        }

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            void process(Document document, PreprocessorReader reader) {
                StringBuffer content = new StringBuffer()
                reader.read().readLines().each {
                    if (it.startsWith("[gnuplot")) {
                        contextStartWithGnuplot = true
                    } else if (contextStartWithGnuplot) {
                        if (it.startsWith("----"))
                            appendCd = true
                        contextStartWithGnuplot = false
                    }
                    content.append it
                    content.append "\n"
                    if (appendCd) content.append "cd \"${contentTree}${rpd}\"\n" as String
                    appendCd = false
                }

                reader.pushInclude(content.toString(), reader.file, reader.file, 1, document.attributes)
            }
        })

        OptionsBuilder optionHasToc = Options.builder().option('parse_header_only', true)
        def loadedDoc = asciidoctor.load(indexFile.text, optionHasToc.build())
        boolean hasToc = loadedDoc.hasAttribute('toc')
        String taackCategory = loadedDoc.attributes['taack-category']
        Integer height = loadedDoc.attributes['taack-height']?.toString()?.toInteger()
        Integer id = (loadedDoc.attributes['taack-id']?.toString()?.toInteger() ?: random.nextInt((10 ** 5) as Integer)) as Integer
        AttributesBuilder attributes = Attributes.builder()
                .backend("${useRevealJS ? 'revealjs' : pdfMode ? 'pdf' : 'html5'}")
                .title(doc.abstractDesc)
                .attribute("revealjsdir", "/assets")
                .attribute('vg2png', vegaBinPath + '/vg2png')
                .attribute('vg2svg', vegaBinPath + '/vg2svg')

        OptionsBuilder options = Options.builder()
                .safe(SafeMode.UNSAFE)//.inPlace(true)
                .standalone(false)
                .baseDir(new File("${contentTree}/${rpd}"))

        if (hasToc) {
            attributes.tableOfContents2(Placement.LEFT)
            options.headerFooter(true)
        }

        if (!pdfMode) {
            attributes.attribute("webrootpath", "${outputTree}/${rpd}" as String)
                    .attribute("webimagesdir", "/intercom/media/${doc.id}?rdp=${rpd}" as String)
                    .attribute("datadir", "${contentTree}/${rpd}/data" as String)
                    .attribute("source-highlighter", "rouge")

            options.attributes(attributes.build())
            Document document = asciidoctor.load(indexFile.text, options.build())
//        dumpDoc(document)
            stringHtml = document.convert()
            asciidoctor.shutdown()

//            if (hasToc) stringHtml = stringHtml.substring(stringHtml.indexOf("</style>") + 8)
            if (useRevealJS) {
                stringHtml = decorateSlideshow(stringHtml, height, id)
                println "REVEAL.JS $stringHtml"
            }

            if (html.exists()) html.delete()
            FileUtils.touch(html)
            stringHtml = convertMediaToWebFormat(doc, stringHtml)
            println "REVEAL.JS2 ${html.name} $stringHtml"
            html << stringHtml
        } else {
            attributes.attribute("pdf-theme", "${taackUiConfiguration.root}/pdf/asciidoctor-pdf-themes/citel.yml" as String)
                    .attribute("pdf-fontsdir", "${taackUiConfiguration.root}/pdf/fonts;GEM_FONTS_DIR" as String)
                    .attribute("webimagesdir", contentTree + '/')
                    .attribute("source-highlighter", "rouge")

            options.attributes(attributes.build())
            options.toDir(new File(outputTree))
            asciidoctor.convertFile(indexFile, options.build())
        }
        slideshow
    }

    final File retrieveIndexFile(final IAsciidocAnchorRef asciidoctorAnchorRef, boolean pdfMode = false) {
        IntercomRepoDoc doc = IntercomRepoDoc.findByBaseFilePath(asciidoctorAnchorRef.docPath)
        if (doc) retrieveIndexFile(doc, pdfMode)
        else {
            log.warn "Cannot find ${asciidoctorAnchorRef.docPath}"
            return null
        }
    }

    final File retrieveIndexFile(final String docPath, boolean pdfMode = false) {
        IntercomRepoDoc doc = IntercomRepoDoc.findByBaseFilePath(docPath)
        if (doc) retrieveIndexFile(doc, pdfMode)
        else {
            log.warn "Cannot find ${docPath}"
            return null
        }
    }

    final File retrieveIndexFile(final IntercomRepoDoc doc, boolean pdfMode = false) {
        final useRevealJS = doc.kind == IntercomDocumentKind.SLIDESHOW

        final File indexFile = new File(docPath(doc))
        final String rpd = docRelativeDirPath(doc)
        final String rp = docRelativeFilePath(doc)
        final String htmlFilePath = rp.replace('.adoc', pdfMode ? '.pdf' : '.html')
        final String outputTree = docOutputTree(doc)
        final String contentTree = docContentTree(doc)
        String stringHtml
        new File("${outputTree}/${htmlFilePath}")
    }

    private final String convertMediaToWebFormat(IntercomRepoDoc doc, String htmlContent) {
        final String rpd = docRelativeDirPath(doc)
        final String outputTree = docOutputTree(doc)
        final String inputTree = docContentTree(doc)

        (htmlContent =~ /(?ms)( src="\/intercom\/media\/${doc.id}\?rdp=)${rpd}([a-z0-9\-\/]*)\.(png|jpeg|jpg)"/).findAll().each {
            final m = it as ArrayList<String>
            final toChange = m[0]
            final prefixToKeep = m[1]
            final pathPrefix = m[2]
            final fileNameSuffix = m[3]
            final fileNamePrefix = pathPrefix.split('/').last()
            final inputImgPath = new File("$inputTree/${pathPrefix}.${fileNameSuffix}").exists() ? "$inputTree/${pathPrefix}.${fileNameSuffix}" : "$outputTree/${pathPrefix}.${fileNameSuffix}"
            Process convert = Runtime.getRuntime().exec(["/usr/bin/convert", inputImgPath, "$outputTree/${fileNamePrefix}.webp"] as String[])
            final processError = convert.errorStream.text
            int retCode = convert.waitFor()
            log.info convert.text
            if (retCode != 0) log.error processError
            htmlContent = htmlContent.replace(toChange, prefixToKeep + fileNamePrefix + ".webp\"")
        }
        (htmlContent =~ /(?ms)( src="\/intercom\/media\/${doc.id}\?rdp=)${rpd}([a-z0-9\-\/]*)\.(webm|webp)"/).findAll().each {
            final m = it as ArrayList<String>
            final toChange = m[0]
            final prefixToKeep = m[1]
            final pathPrefix = m[2]
            final fileNameSuffix = m[3]
            final fileNamePrefix = pathPrefix.split('/').last()
            final inputImgPath = new File("$inputTree/${rpd}/${pathPrefix}.${fileNameSuffix}").exists() ? "$inputTree/${rpd}/${pathPrefix}.${fileNameSuffix}" : "$outputTree/${pathPrefix}.${fileNameSuffix}"
            Process copy = Runtime.getRuntime().exec(["/usr/bin/cp", inputImgPath, "$outputTree/${fileNamePrefix}.${fileNameSuffix}"] as String[])
            final processError = copy.errorStream.text
            int retCode = copy.waitFor()
            log.info copy.text
            if (retCode != 0) log.error processError
            htmlContent = htmlContent.replace(toChange, prefixToKeep + fileNamePrefix + ".${fileNameSuffix}\"")
        }
        (htmlContent =~ /(?ms)( data-background-image="\/intercom\/media\/${doc.id}\?rdp=)${rpd}([a-z0-9\-\/]*)\.(webm|webp)"/).findAll().each {
            final m = it as ArrayList<String>
            final toChange = m[0]
            final prefixToKeep = m[1]
            final pathPrefix = m[2]
            final fileNameSuffix = m[3]
            final fileNamePrefix = pathPrefix.split('/').last()
            final inputImgPath = new File("$inputTree/${rpd}/${pathPrefix}.${fileNameSuffix}").exists() ? "$inputTree/${rpd}/${pathPrefix}.${fileNameSuffix}" : "$outputTree/${pathPrefix}.${fileNameSuffix}"
            Process copy = Runtime.getRuntime().exec(["/usr/bin/cp", inputImgPath, "$outputTree/${fileNamePrefix}.${fileNameSuffix}"] as String[])
            final processError = copy.errorStream.text
            int retCode = copy.waitFor()
            log.info copy.text
            if (retCode != 0) log.error processError
            htmlContent = htmlContent.replace(toChange, prefixToKeep + fileNamePrefix + ".${fileNameSuffix}\"")
        }
        (htmlContent =~ /(?ms)( src="\/intercom\/media\/${doc.id}\?rdp=)${rpd}(^(?!diag-)[a-z0-9\-\/]*)\.(svg)"/).findAll().each {
            final m = it as ArrayList<String>
            final toChange = m[0]
            final prefixToKeep = m[1]
            final pathPrefix = m[2]
            final fileNameSuffix = m[3]
            final fileNamePrefix = pathPrefix.split('/').last()
            Process copy = Runtime.getRuntime().exec(["/usr/bin/cp", "$inputTree/${pathPrefix}.${fileNameSuffix}", "$outputTree/${fileNamePrefix}.${fileNameSuffix}"] as String[])
            final processError = copy.errorStream.text
            int retCode = copy.waitFor()
            log.info copy.text
            if (retCode != 0) log.error processError
            htmlContent = htmlContent.replace(toChange, prefixToKeep + fileNamePrefix + ".${fileNameSuffix}\"")
        }
        htmlContent
    }

    @Transactional
    void refreshDocMetaData(IntercomRepoDoc doc) {
        final File indexFile = new File(docPath(doc))
        Asciidoctor asciidoctor = initAsciidoctorJ()
        Document document = asciidoctor.load(indexFile.text, Options.builder().build())
        doc.docTitle = document.doctitle
        doc.authors = document.getAuthors().join(', ')
        doc.subtitle = document.structuredDoctitle.subtitle
    }

    private static String decorateSlideshow(String slideshowContent, Integer height, int id = 1) {
        return """
<style src="/assets/custom-reveal.css"></style>
            <div style="height: ${height ?: 512}px;">
                <div class="reveal deck${id ?: 1}">
                    <div class="slides">
                        ${slideshowContent}
                    </div>
                </div>
            </div>
<script postExecute="true">
    // More info about initialization & config:
    // - https://revealjs.com/initialization/
    // - https://revealjs.com/config/
    if (typeof Reveal != 'undefined' && document.querySelector( '.deck${id ?: 1}' ) && document.querySelectorAll(".deck${id ?: 1} canvas").length < 1) {
        let deck${id ?: 1} = Reveal(document.querySelector( '.deck${id ?: 1}' ), {
            embedded: true,
            keyboardCondition: 'focused' // only react to keys when focused
        })
        deck${id ?: 1}.initialize({
            hash: false,
            fragments: true,
            fragmentInURL: false,
            loop: true,
            transition: 'default',
            transitionSpeed: 'default',
            backgroundTransition: 'default',
            viewDistance: 3,
            width: 960,
            height: ${height ?: 512},
            controls: true,
            progress: true,
            autoSlide: 4000,
            plugins: [RevealHighlight]
        });
    }
</script>
        """
    }
}
