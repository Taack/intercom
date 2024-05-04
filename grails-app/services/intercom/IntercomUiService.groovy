package intercom

import conf.IntercomConfig
import crew.CrewUiService
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import grails.util.Pair
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import taack.base.TaackSimpleFilterService
import taack.ui.base.UiFilterSpecifier
import taack.ui.base.UiTableSpecifier
import taack.ui.base.block.BlockSpec
import taack.ui.base.common.ActionIcon
import taack.ui.base.common.ActionIconStyleModifier
import org.taack.User

@GrailsCompileStatic
class IntercomUiService implements WebAttributes {
    MessageSource messageSource
    TaackSimpleFilterService taackSimpleFilterService
    CrewUiService crewUiService
    IntercomAsciidoctorConverterService intercomAsciidoctorConverterService

    @Autowired
    PageRenderer g

    protected String tr(final String code, final Locale locale = null, final Object[] args = null) {
        if (LocaleContextHolder.locale.language == "test") return code
        try {
            messageSource.getMessage(code, args, locale ?: LocaleContextHolder.locale)
        } catch (e1) {
            try {
                messageSource.getMessage(code, args, new Locale("en"))
            } catch (e2) {
                code
            }
        }
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomUserList(boolean selectMode = false) {
        IntercomUser iu = new IntercomUser(baseUser: new User(manager: new User()))
        UiFilterSpecifier filter = new UiFilterSpecifier()

        filter.ui IntercomUser, {
            section tr("default.user.label"), {
                filterField iu.baseUser_, iu.baseUser.username_
                filterField iu.baseUser_, iu.baseUser.lastName_
                filterField iu.baseUser_, iu.baseUser.firstName_
                filterField tr("user.manager.label"), iu.baseUser_, iu.baseUser.manager_, iu.baseUser.manager.username_
                filterField iu.baseUser_, iu.baseUser.mainSubsidiary_
                filterField iu.baseUser_, iu.baseUser.allowedSubsidiaries_
                filterField iu.baseUser_, iu.baseUser.enabled_
            }
        }
        UiTableSpecifier t = new UiTableSpecifier()
        t.ui IntercomUser, {
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
                    sortableFieldHeader tr("user.username.label"), iu.baseUser_, iu.baseUser.username_
                    sortableFieldHeader tr("default.mainSubsidiary.label"), iu.baseUser_, iu.baseUser.mainSubsidiary_
                }
                fieldHeader tr("intercomUser.pubKeyContent.label")
            }
            def objects = taackSimpleFilterService.list(IntercomUser, 15)
            paginate(15, params.long("offset"), objects.bValue)
            for (final cui in objects.aValue) {
                row {
                    rowColumn {
                        rowField cui.dateCreated
                        rowField cui.userCreated.toString()
                    }
                    rowColumn {
                        rowField cui.lastUpdated
                        rowField cui.userUpdated.toString()
                    }
                    rowColumn {
                        if (selectMode) {
                            rowLink "Select", ActionIcon.SELECT * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&selectO2MIntercomRepoUserCloseModal as MethodClosure, cui.id, true
                        } else {
                            if (crewUiService.canManage(cui.baseUser)) rowLink "Edit", ActionIcon.EDIT * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&editIntercomUser as MethodClosure, cui.id, true
                        }
                        rowField cui.baseUser.toString()
                        rowField cui.baseUser.mainSubsidiary.toString()
                    }
                    rowField cui.pubKeyContent
                }
            }
        }
        new Pair<UiFilterSpecifier, UiTableSpecifier>(filter, t)
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomRepoList(boolean selectMode = false) {
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
        t.ui IntercomUser, {
            header {
                sortableFieldHeader ir.dateCreated_
                fieldHeader tr("intercomRepo.owner.label")
                sortableFieldHeader ir.name_
                fieldHeader "URL"
                fieldHeader tr("default.actions.label")
            }
            def objects = taackSimpleFilterService.list(IntercomRepo)
            for (IntercomRepo repo : objects.aValue) {
                row {
                    rowField repo.dateCreated
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
                        rowLink tr("default.button.create.label"), ActionIcon.CREATE * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&createDoc as MethodClosure, repo.id
                        rowLink tr("default.button.edit.label"), ActionIcon.EDIT * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&editRepo as MethodClosure, repo.id
                    }
                }
            }
        }
        new Pair<UiFilterSpecifier, UiTableSpecifier>(filter, t)
    }

    Pair<UiFilterSpecifier, UiTableSpecifier> buildIntercomRepoDocList(final IntercomConfig.DocumentCategory category = null, final boolean latest = false) {
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
                filterField id.category_
                filterField id.kind_
                filterField id.baseFilePath_
                filterField id.abstractDesc_
            }
        }

        UiTableSpecifier t = new UiTableSpecifier()
        t.ui IntercomRepoDoc, {
            header {
                column {
                    sortableFieldHeader id.dateCreated_
                    fieldHeader tr("intercomRepo.owner.label")
                }
                column {
                    sortableFieldHeader id.category_
                    sortableFieldHeader id.kind_
                }
                fieldHeader tr("intercomRepoDoc.abstractDesc.label")
                column {
                    fieldHeader tr("intercomRepoDoc.baseFilePath.label")
                    fieldHeader tr("intercomRepo.name.label")
                }
                fieldHeader tr("default.actions.label")
            }
            def objects = taackSimpleFilterService.list(IntercomRepoDoc, 20, filter)
            for (IntercomRepoDoc doc : objects.aValue) {
                row {
                    rowColumn {
                        rowField doc.dateCreated
                        rowField doc.intercomRepo.owner.baseUser.username
                    }
                    rowColumn {
                        rowField doc.category.toString()
                        rowField doc.kind.toString()
                    }
                    rowField doc.abstractDesc
                    rowColumn {
                        rowField doc.baseFilePath
                        rowField doc.intercomRepo.name
                    }
                    rowColumn {
                        if (doc.lastRevAuthor) {
                            rowLink "View", ActionIcon.SHOW * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&viewDoc as MethodClosure, doc.id, false

                            if (doc.kind == IntercomConfig.DocumentKind.PAGE)
                                rowLink "PDF", ActionIcon.EXPORT_PDF * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&dlDoc as MethodClosure, doc.id, false
                            else if (doc.kind == IntercomConfig.DocumentKind.SLIDESHOW)
                                rowLink "PDF", ActionIcon.EXPORT_PDF * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&viewDoc as MethodClosure, doc.id, ["print-pdf": true], false
                        }
                        rowLink "History", ActionIcon.DETAILS * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&histDoc as MethodClosure, doc.id, true
                        rowLink "Edit", ActionIcon.EDIT * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&editDoc as MethodClosure, doc.id, true
                        rowLink "Refresh", ActionIcon.REFRESH * ActionIconStyleModifier.SCALE_DOWN, IntercomController.&refreshDoc as MethodClosure, doc.id, true
                    }
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