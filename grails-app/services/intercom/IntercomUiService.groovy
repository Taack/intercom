package intercom

import attachment.DocumentCategory
import crew.CrewController
import crew.CrewSecurityService
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import grails.util.Pair
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import org.springframework.beans.factory.annotation.Autowired
import crew.User
import taack.domain.TaackFilterService
import taack.render.TaackUiEnablerService
import taack.ui.dsl.UiFilterSpecifier
import taack.ui.dsl.UiTableSpecifier
import taack.ui.dsl.block.BlockSpec
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.IconStyle

import javax.annotation.PostConstruct

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
class IntercomUiService implements WebAttributes {
    TaackFilterService taackFilterService
    CrewSecurityService crewSecurityService
    IntercomAsciidoctorConverterService intercomAsciidoctorConverterService

    @Autowired
    PageRenderer g

    private securityClosure(Long id, Map p) {
        if (!id) return true
        crewSecurityService.canEdit(User.read(id))
    }

    @PostConstruct
    void init() {
        TaackUiEnablerService.securityClosure(
                this.&securityClosure,
                IntercomController.&createIntercomUser as MC,
                IntercomController.&saveIntercomUser as MC,
                CrewController.&saveUser as MC)
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomUserList(boolean selectMode = false) {
        IntercomUser iu = new IntercomUser(baseUser: new User(manager: new User()))
        UiFilterSpecifier filter = new UiFilterSpecifier()

        filter.ui IntercomUser, {
            section tr("default.user.label"), {
                filterField iu.baseUser_, iu.baseUser.username_
                filterField iu.baseUser_, iu.baseUser.lastName_
                filterField iu.baseUser_, iu.baseUser.firstName_
                filterField iu.baseUser_, iu.baseUser.manager_, iu.baseUser.manager.username_
                filterField iu.baseUser_, iu.baseUser.subsidiary_
                filterField iu.baseUser_, iu.baseUser.enabled_
            }
        }
        UiTableSpecifier t = new UiTableSpecifier()
        t.ui {
            header {
                column {
                    sortableFieldHeader iu.dateCreated_
                    sortableFieldHeader iu.userCreated_
                }
                column {
                    sortableFieldHeader iu.lastUpdated_
                    sortableFieldHeader iu.userCreated_
                }
                column {
                    sortableFieldHeader iu.baseUser_, iu.baseUser.username_
                    sortableFieldHeader iu.baseUser_, iu.baseUser.subsidiary_
                }
                label tr("intercomUser.pubKeyContent.label")
            }
            iterate(taackFilterService.getBuilder(IntercomUser).setMaxNumberOfLine(15).build()) { IntercomUser cui ->

                rowColumn {
                    rowField cui.dateCreated_
                    rowField cui.userCreated.toString()
                }
                rowColumn {
                    rowField cui.lastUpdated_
                    rowField cui.userUpdated.toString()
                }
                rowColumn {
                    if (selectMode) {
                        rowAction ActionIcon.SELECT * IconStyle.SCALE_DOWN, IntercomController.&selectO2MIntercomRepoUserCloseModal as MC, cui.id
                    } else {
                        rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, IntercomController.&editIntercomUser as MC, cui.id
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, IntercomController.&deleteIntercomUser as MC, cui.id
                    }
                    rowField cui.baseUser_
                    rowField cui.baseUser.subsidiary_
                }
                rowField cui.pubKeyContent
            }
        }
        new Pair<UiFilterSpecifier, UiTableSpecifier>(filter, t)
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomRepoList() {
        IntercomRepo ir = new IntercomRepo(owner: new IntercomUser(baseUser: new User()))
        UiFilterSpecifier filter = new UiFilterSpecifier()
        filter.ui IntercomRepo, {
            section "Repo", {
                filterField ir.name_
                filterField ir.keyWords_
                filterField tr("intercomRepo.owner.label"), ir.owner_, ir.owner.baseUser_, ir.owner.baseUser.username_
            }
        }

        UiTableSpecifier t = new UiTableSpecifier()
        t.ui {
            header {
                sortableFieldHeader ir.dateCreated_
                label ir.owner_
                sortableFieldHeader ir.name_
                label "URL"
                label tr("default.actions.label")
            }
            iterate(taackFilterService.getBuilder(IntercomRepo).build()) { IntercomRepo repo ->
                rowField repo.dateCreated_
                rowField repo.owner.baseUser.username
                rowField repo.name
                String names = ""
                boolean isFirst = true
                for (def doc : IntercomRepoDoc.findAllByIntercomRepo(repo) as Collection<IntercomRepoDoc>) {
                    if (!isFirst) {
                        names += ", "
                    }
                    isFirst = false
                    names += '<b>' + doc.baseFilePath + '</b>'

                }
                rowField "git clone intra:${repo.name} (contains following docs: $names)"
                rowColumn {
                    rowAction ActionIcon.CREATE * IconStyle.SCALE_DOWN, IntercomController.&createDoc as MC, repo.id
                    rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, IntercomController.&editRepo as MC, repo.id
                }
            }
        }
        new Pair<UiFilterSpecifier, UiTableSpecifier>(filter, t)
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomRepoDocList(final DocumentCategory category = null) {
        IntercomRepoDoc id = new IntercomRepoDoc(
                intercomRepo: new IntercomRepo(owner: new IntercomUser(baseUser: new User())),
                category: category
        )
        UiFilterSpecifier filter = new UiFilterSpecifier()
        filter.ui IntercomRepoDoc, {
            section "Repo", {
                filterField id.intercomRepo_, id.intercomRepo.owner_, id.intercomRepo.owner.baseUser_, id.intercomRepo.owner.baseUser.username_
                filterField id.intercomRepo_, id.intercomRepo.name_
            }
            section "Doc", {
                filterField id.docTitle_
                filterField id.subtitle_
                filterField id.documentCategory_
                filterField id.kind_
                filterField id.baseFilePath_
                filterField id.abstractDesc_
            }
        }

        UiTableSpecifier t = new UiTableSpecifier()
        t.ui {
            header {
                column {
                    sortableFieldHeader id.dateCreated_
                    label id.intercomRepo_, new IntercomRepo().owner_
                }
                column {
                    sortableFieldHeader id.documentCategory_
                    sortableFieldHeader id.kind_
                }
                label id.abstractDesc_
                column {
                    label id.baseFilePath_
                    label id.docTitle_
                }
                label tr("default.actions.label")
            }
            iterate(taackFilterService.getBuilder(IntercomRepoDoc).build()) { IntercomRepoDoc doc ->
                rowColumn {
                    rowField doc.dateCreated_
                    rowField doc.intercomRepo.owner.baseUser.username
                }
                rowColumn {
                    rowField doc.documentCategory.toString()
                    rowField doc.kind.toString()
                }
                rowField doc.abstractDesc
                rowColumn {
                    rowField doc.baseFilePath
                    rowField doc.intercomRepo.name
                }
                rowColumn {
                    if (doc.lastRevAuthor) {
                        rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, IntercomController.&viewDoc as MC, doc.id

                        if (doc.kind == IntercomDocumentKind.PAGE)
                            rowAction ActionIcon.EXPORT_PDF * IconStyle.SCALE_DOWN, IntercomController.&dlDoc as MC, doc.id
                        else if (doc.kind == IntercomDocumentKind.SLIDESHOW)
                            rowAction ActionIcon.EXPORT_PDF * IconStyle.SCALE_DOWN, IntercomController.&viewDoc as MC, doc.id, ["print-pdf": true]
                    }
                    rowAction ActionIcon.DETAILS * IconStyle.SCALE_DOWN, IntercomController.&histDoc as MC, doc.id
                    rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, IntercomController.&editDoc as MC, doc.id
                    rowAction ActionIcon.REFRESH  * IconStyle.SCALE_DOWN, IntercomController.&refreshDoc as MC, doc.id
                }
            }
        }
        new Pair<UiFilterSpecifier, UiTableSpecifier>(filter, t)
    }

    String renderAsciidocInline(IAsciidocAnchorRef ref) {
        def prez = intercomAsciidoctorConverterService.retrieveIndexFile(ref)
        if (!prez) {
            return "Doc being writen ..."
        } else {
            g.render(template: "/intercom/asciidocInline", model: [pageAsciidocContent: prez.text]) as String
        }
    }

    Closure<BlockSpec> buildBlockAsciidocModal(IAsciidocAnchorRef ref) {
        BlockSpec.buildBlockSpec {
            ajaxBlock "showHelpIFrame", {
                custom g.render(template: "/intercom/asciidocModal", model: [docPath: ref.docPath, docAnchor: ref.docAnchor]) as String
            }
        }
    }
}