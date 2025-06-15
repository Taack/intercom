package intercom.ssh.git

import grails.compiler.GrailsCompileStatic
import intercom.IntercomRepo
import intercom.IntercomUser
import jakarta.annotation.PostConstruct
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.springframework.beans.factory.annotation.Value
import taack.ssh.SshEventRegistry
import crew.User


@GrailsCompileStatic
class IntercomGitRepoRegisterService implements SshEventRegistry.UserGitRepoChecker {

    static lazyInit = false

    @Value('${intranet.root}')
    String intranetRoot

    String getGitRootPath() {
        intranetRoot + '/intercom/git-root'
    }

    @PostConstruct
    def initVfs() {
        log.info "Init Intercom Git Starts"
        FileUtils.forceMkdir(new File(gitRootPath))
        SshEventRegistry.GitHelper.registerUserGitRepoChecker(this)
        log.info "Init Intercom Git Ends"
    }

    @Override
    String rootRepoDirectory(String username, String repoName) {
        User.withNewSession {
            User u = User.findByUsernameAndEnabled(username, true)
            if (u) {
                IntercomUser iu = IntercomUser.findByBaseUser(u)
                if (iu) {
                    IntercomRepo repo = IntercomRepo.findByName(repoName)
                    if (repo) {
                        return gitRootPath + '/' + repoName
                    }
                    else log.warn "rootRepoDirectory $username $repoName does not exists"
                } else log.warn "rootRepoDirectory $username not a Intercom User"
            } else log.warn "rootRepoDirectory no active user $username"
            null
        }
    }

    @Override
    boolean userCanAccessGitRepo(String username, String repoName) {
        log.info "userCanAccessGitRepo $username $repoName"
        User.withNewSession {
            User u = User.findWhere(username: username, enabled: true) as User
            if (u) {
                IntercomUser iu = IntercomUser.findByBaseUser(u)
                if (iu) {
                    IntercomRepo repo = IntercomRepo.findByName(repoName)
                    if (repo && (repo.allowedEditors*.id.contains(iu.id) || repo.owner.id == iu.id)) {
                        return true
                    } else log.warn "userCanAccessGitRepo $username cannot access $repoName"
                } else log.warn "userCanAccessGitRepo $username not a Intercom User"
            } else log.warn "userCanAccessGitRepo no active user $username"
            false
        }
    }

    @Override
    boolean userCanCreateGitRepo(String username, String repoName) {
        User.withNewSession {
            User u = User.findByUsernameAndEnabled(username, true)
            if (u) {
                IntercomUser iu = IntercomUser.findByBaseUser(u)
                if (iu) {
                    IntercomRepo repo = IntercomRepo.findByName(repoName)
                    if (repo && repo.ownerId == iu.id) {
                        return true
                    } else log.warn "userCanCreateGitRepo $username cannot create $repoName"
                } else log.warn "userCanCreateGitRepo $username not a Intercom User"
            } else log.warn "userCanCreateGitRepo no active user $username"
            false
        }
    }
}
