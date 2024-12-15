package intercom

import attachment.DocumentCategory
import crew.AttachmentController
import crew.CrewUiService
import crew.User
import crew.config.SupportedLanguage
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.domain.TaackSaveService
import taack.render.TaackUiProgressBarService
import taack.render.TaackUiService
import taack.ui.dsl.*
import taack.ui.dsl.block.BlockSpec
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.IconStyle
import taack.wysiwyg.Markdown

import static grails.async.Promises.task
/**
 * Independent App Managing Documentation via Git
 * Functionalities:
 *  - Manage users and there credentials
 *  - Create Repositories for one user (create, edit, read)
 *  - Repo templates
 *  - Update Render via Asciidoctor
 *  - List files
 *  - Generate PDFs (if read access)
 *  - Display Page with menu (if read access)
 *  - Organize Repo via hierarchy and tags
 *  - Search engine
 *  - Basic user activities (new Doc)
 *  - Comments
 *  - Display version tree
 *  - Notifications
 *  - Multiple doc per repo
 *  - Heading: UserGuides, HowTos, News and Noteworthy
 *
 *  Alternative to tree: tags, history, research*/
@GrailsCompileStatic
@Secured(["isAuthenticated()"])
class IntercomController {
    TaackUiService taackUiService
    TaackSaveService taackSaveService
    IntercomUiService intercomUiService
    IntercomAsciidoctorConverterService intercomAsciidoctorConverterService
    SpringSecurityService springSecurityService
    TaackUiProgressBarService taackUiProgressBarService
    IntercomSearchService intercomSearchService


    protected IntercomUser currentIntercomUser() {
        final User u = authenticatedUser as User
        IntercomUser.findByBaseUser(u)
    }

    private UiMenuSpecifier buildMenu(String q = null) {
        def intercomUser = currentIntercomUser()
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu this.&index as MC
            menu this.&showLatestDocs as MC
            menu this.&showDocs as MC

            if (!intercomUser) {
                menuIcon ActionIcon.CREATE, this.&createIntercomUser as MC
            } else {
                menu this.&repoDocs as MC
                menu this.&repos as MC
                menu this.&intercomUsers as MC
                menuIcon ActionIcon.CONFIG_USER, this.&editIntercomUser as MC, intercomUser.id
                menuIcon ActionIcon.CREATE, this.&createRepo as MC
            }
            menuSearch IntercomController.&search as MC, q
            menuOptions(SupportedLanguage.EN)
        }
        m
    }

    private static UiFormSpecifier buildIntercomUserForm(final IntercomUser iu) {
        UiFormSpecifier form = new UiFormSpecifier()
        form.ui iu, {
            hiddenField iu.baseUser_
            hiddenField iu.userCreated_
            section 'Intercom User Settings', {
                field iu.pubKeyContent_
            }
            formAction IntercomController.&saveIntercomUser as MC, iu.id
        }
        form
    }

    private UiFormSpecifier buildIntercomRepoForm(IntercomRepo repo) {
        new UiFormSpecifier().ui repo, {
            section 'Repo', {
                field repo.name_
                field repo.keyWords_
                ajaxField repo.owner_, this.&selectO2MIntercomRepoUser as MC
                ajaxField repo.allowedEditors_, this.&selectO2MIntercomRepoUser as MC
            }
            formAction IntercomController.&saveIntercomRepo as MC, repo.id
        }
    }

    private static UiFormSpecifier buildIntercomDocForm(IntercomRepoDoc doc) {
        new UiFormSpecifier().ui doc, {
            hiddenField(doc.intercomRepo_)
            section 'Doc', {
                ajaxField doc.documentAccess_, AttachmentController.&selectDocumentAccess as MC
                ajaxField doc.documentCategory_, AttachmentController.&selectDocumentCategory as MC
                field doc.kind_
                field doc.abstractDesc_
                field doc.baseFilePath_
                field doc.theme_
            }
            formAction IntercomController.&saveIntercomRepoDoc as MC, doc.id
        }
    }

    def viewDoc(IntercomRepoDoc doc) {
        if (!doc) return false
        if (doc.kind == IntercomDocumentKind.SLIDESHOW) {
            taackUiService.show(new UiBlockSpecifier().ui {
                String r = intercomUiService.renderReveal(doc)
                custom(r)
            }, buildMenu())
        } else {
            def prez = intercomAsciidoctorConverterService.retrieveIndexFile(doc)
            taackUiService.showView('/intercom/asciidocInline', [pageAsciidocContent: prez.text], buildMenu())
        }
    }

    def viewDocInline(IntercomRepoDoc doc) {
        def prez = intercomAsciidoctorConverterService.retrieveIndexFile(doc)
        render(view: 'asciidocInline.gsp', model: [pageAsciidocContent: prez.text])
    }

    def viewDocModal(String docPath) {
        def prez = intercomAsciidoctorConverterService.retrieveIndexFile(docPath)
        render(template: "asciidocModal", model: [pageAsciidocContent: prez.text])
    }

    def histDoc(IntercomRepoDoc doc) {
        def listOfRev = intercomAsciidoctorConverterService.docHistory(doc)
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                table new UiTableSpecifier().ui({
                    for (String rev in listOfRev) row {
                        rowField rev
                    }
                })
            }
        })
    }

    def downloadBinDoc(IntercomRepoDoc doc) {
        def prez = intercomAsciidoctorConverterService.retrieveIndexFile(doc, true)
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment;filename=\"${prez.name}\"")
        response.outputStream << prez.bytes
        response.outputStream.flush()
        response.outputStream.close()
    }

    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def refreshDoc(IntercomRepoDoc doc) {
        String pId = taackUiProgressBarService.progressStart(BlockSpec.buildBlockSpec {
            show(new UiShowSpecifier().ui {
                inlineHtml("Ended")
            })
        }, 100)
        task {
            try {
                User.withNewSession {
                    def prez = intercomAsciidoctorConverterService.processDoc(doc)
                    intercomAsciidoctorConverterService.refreshDocMetaData(doc)
                    taackUiProgressBarService.progress(pId, 50)
                    if (doc.kind == IntercomDocumentKind.PAGE)
                        intercomAsciidoctorConverterService.processDoc(doc, true)
                    taackUiProgressBarService.progress(pId, 50)
                }
            } catch (e) {
                log.error(e.message)
                e.printStackTrace()
                taackUiProgressBarService.progressEndedClosure(pId, BlockSpec.buildBlockSpec({
                    show(new UiShowSpecifier().ui {
                        inlineHtml("Error: ${e.message}")
                    })
                }))
            }
            taackUiProgressBarService.progressEnded(pId)
        }
    }

    def media(IntercomRepoDoc doc) {
        String rdp = (String) params.rdp
        String fileName = rdp.tokenize('/').last()
        String imagePath = intercomAsciidoctorConverterService.docOutputTree(doc) + '/' + fileName
        File img = new File(imagePath)
        if (img.name.startsWith("diag")) {
            img = new File(intercomAsciidoctorConverterService.docOutputTree(doc) + '/' + img.name)
            if (!img.exists()) {
                img = new File(intercomAsciidoctorConverterService.docOutputTree(doc) + rdp)
            }
        }
        boolean isSvg = imagePath.endsWith('.svg')
        boolean isWebp = imagePath.endsWith('.webp')
        boolean isWebm = imagePath.endsWith('.webm')

        if (img.exists()) {
            try {
                response.setHeader("Content-disposition", "attachment; filename=" + img.name)
                response.setHeader("Cache-Control", "max-age=31536000")
                response.contentType = "image/${isWebp ? "webp" : isSvg ? "svg+xml" : isWebm ? "video/webm" : ""}"
                response.outputStream << img.bytes
            } catch (e) {
                log.error "image ${imagePath}: ${e.message}"
            }
        } else {
            log.warn "Image does not exist ${imagePath}"
            return false
        }

    }

    def selectO2MIntercomRepoUser() {
        def p = intercomUiService.buildIntercomUserList(true)
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                tableFilter p.aValue, p.bValue
            }
        })
    }

    def selectO2MIntercomRepoUserCloseModal(IntercomUser user) {
        taackUiService.closeModal(user.id, user.toString())
    }

    def index() {
        User u = springSecurityService.currentUser as User
        UiBlockSpecifier b = new UiBlockSpecifier().ui {
            ajaxBlock "index", {
                show new UiShowSpecifier().ui {
                    inlineHtml Markdown.getContentHtml("""\
                        # Configuring Intercom User (Linux and Mac)
                        
                        open a **terminal** and type:
                        ```
                        ssh-keygen -t ed25519
                        cat .ssh/id_ed25519.pub
                        ```
                        
                        Come back to the **Intercom app**, then click on **configure** icon
                        
                        - copy / paste the output of `.ssh/ed25519.pub` in the textarea
                        - click on save
                        
                        Then create a file in `~/.ssh/config` with the following content
                        ```
                        Host intra
                          User ${u.username}
                          HostName intranet.citel.fr
                          Port 22222
                          Compression yes
                          IdentityFile ~/.ssh/id_ed25519
                        ```

                        The file must be writable only from the logged user, if the loggin is unsuccessfull, check files permissions:
                        ```bash
                        [xXx@xXxbureau ~]\$ ls -l ~/.ssh
                        total 80
                        -rw------- 1 xXx xXx    95 août   5  2021 authorized_keys
                        -rw-r--r-- 1 xXx xXx   395 sept.  6  2021 config
                        -rw------- 1 xXx xXx   672 sept.  4  2017 id_dsa
                        -rw------- 1 xXx xXx   598 sept.  4  2017 id_dsa.pub
                        -rw------- 1 xXx xXx   399 août   5  2021 id_ed25519
                        -rw------- 1 xXx xXx    95 août   5  2021 id_ed25519.pub
                        -rw------- 1 xXx xXx  1675 sept.  4  2017 id_rsa
                        -rw------- 1 xXx xXx  1766 sept.  4  2017 id_rsa2
                        -rw------- 1 xXx xXx   394 sept.  4  2017 id_rsa2.pub
                        -rw------- 1 xXx xXx   394 sept.  4  2017 id_rsa.pub
                        -rw------- 1 xXx xXx 19184 sept. 21 13:01 known_hosts
                        ```
                        `xXx` is your home user.
                        
                        You have to repeate those steps for all your computers, you can append as many keys you want in the textarea.

                        You are ready to create a repository!

                        ## Creating and getting a repository

                        Click on the `+` sign, just 2 fields to enter and you are done.

                        ## Accesing your repository from your hard drive

                        Type from an empty folder (changing `theRepositoryName`):
                        ```
                        git clone intra:**theRepositoryName**
                        ```

                        You have it.. Now you can use git the usual way. See [PLM](/plm) app.

                        # Creating some documentation

                        Docs are writen using Asciidoc language, your files must end with the `.adoc` extension. A repository might
                        contain more than 1 adoc file, include directive is supported.

                        You can use the [Asciidoctor Diagram](https://docs.asciidoctor.org/diagram-extension/latest/) extensions.
                        
                        see [Asciidoctor Overview](https://intranet.citel.fr/intercom/viewDoc/2631529)
                        
                        """.stripIndent()), "markdown-body"
                }
            }
        }
        taackUiService.show(b, buildMenu())
    }

    def repos() {
        def p = intercomUiService.buildIntercomRepoList()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter p.aValue, p.bValue
        }, buildMenu())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def createRepo() {
        IntercomRepo intercomRepo = new IntercomRepo()
        taackUiService.showModal(buildIntercomRepoForm(intercomRepo))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def createDoc(IntercomRepo repo) {
        taackUiService.showModal(buildIntercomDocForm(new IntercomRepoDoc(intercomRepo: repo)))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def editDoc(IntercomRepoDoc doc) {
        taackUiService.showModal(buildIntercomDocForm(doc))
    }

    def editRepo(IntercomRepo intercomRepo) {
        taackUiService.showModal(buildIntercomRepoForm(intercomRepo))
    }

    def showLatestDocs() {
        taackUiService.show(new UiBlockSpecifier().ui {
            table new UiTableSpecifier().ui({
                for (IntercomRepoDoc doc in IntercomRepoDoc.findAll([max: 10, sort: "lastRevWhen", order: "desc"])) {
                    println "DOC $doc"

                    row {
                        rowField """
                            <b>${doc.docTitle ?: doc.baseFilePath}</b><br>
                            ${doc.lastRevWhen} <b>${doc.lastRevAuthor}</b><br>
                            ${doc.lastRevMessage} ${doc.abstractDesc}<br>
                        """
                        rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, IntercomController.&viewDoc as MC, doc.id
                        rowAction ActionIcon.EXPORT_PDF * IconStyle.SCALE_DOWN, IntercomController.&downloadBinDoc as MC, doc.id
                        rowAction ActionIcon.DETAILS * IconStyle.SCALE_DOWN, IntercomController.&histDoc as MC, doc.id
                    }
                }
            })
        }, buildMenu())
    }

    def showDocs(String documentCategory) {
        DocumentCategory docCat = documentCategory as DocumentCategory

        taackUiService.show(new UiBlockSpecifier().ui {
            table new UiTableSpecifier().ui({
                for (IntercomRepoDoc doc in IntercomRepoDoc.findAllByDocumentCategoryAndLastRevAuthorIsNotNull(docCat, [max: 10, sort: "lastRevWhen", order: "desc"])) {
                    println "DOC $doc"
                    row {
                        rowField """
                            <b>${doc.docTitle ?: doc.baseFilePath}</b><br>
                            ${doc.lastRevWhen} <b>${doc.lastRevAuthor}</b><br>
                            ${doc.lastRevMessage} ${doc.abstractDesc}<br>
                        """
                        rowAction ActionIcon.SHOW, IntercomController.&viewDoc as MC, doc.id
                        rowAction ActionIcon.EXPORT_PDF, IntercomController.&downloadBinDoc as MC, doc.id
                        rowAction ActionIcon.DETAILS, IntercomController.&histDoc as MC, doc.id
                    }
                }
            })
        }, buildMenu())
    }

    def repoDocs() {
        def p = intercomUiService.buildIntercomRepoDocList()
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter p.aValue, p.bValue
        }, buildMenu())
    }

    def intercomUsers() {
        def p = intercomUiService.buildIntercomUserList()
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter p.aValue, p.bValue
        }, buildMenu())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def createIntercomUser(User userToCreate) {
        userToCreate ?= authenticatedUser as User
        IntercomUser iu = IntercomUser.findByBaseUser(userToCreate)
        if (iu) {
            taackUiService.show CrewUiService.messageBlock("User ${userToCreate.username} already has a profile..")
            return
        }
        iu = new IntercomUser(baseUser: userToCreate, userCreated: authenticatedUser as User)
        taackUiService.showModal(buildIntercomUserForm(iu))
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def saveIntercomUser() {
        taackSaveService.saveThenRedirectOrRenderErrors(IntercomUser, this.&intercomUsers as MC)
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def deleteIntercomUser(IntercomUser intercomUser) {
        intercomUser.delete()
        taackUiService.ajaxReload()
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def saveIntercomRepo() {
        taackSaveService.saveThenRedirectOrRenderErrors(IntercomRepo, this.&repos as MC)
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_INTERCOM_DIRECTOR', 'ROLE_INTERCOM_MANAGER'])
    def saveIntercomRepoDoc() {
        taackSaveService.saveThenRedirectOrRenderErrors(IntercomRepoDoc, this.&repoDocs as MC)
    }

    def editIntercomUser(IntercomUser intercomUser) {
        taackUiService.showModal(buildIntercomUserForm(intercomUser))
    }

    def search(String q) {
        taackUiService.show(intercomSearchService.buildSearchBlock(q), buildMenu(q))
    }

    @Secured(["ROLE_ADMIN"])
    def refreshAllDocs() {
        List<IntercomRepoDoc> docList = IntercomRepoDoc.findAllByLastRevWhenIsNotNull([max: 10, sort: "lastRevWhen", order: "desc"])
        for (def doc in docList) intercomAsciidoctorConverterService.refreshDocMetaData(doc)

        render "OK"
    }
}
