package intercom.ssh

import grails.compiler.GrailsCompileStatic
import intercom.IntercomUser
import org.springframework.beans.factory.annotation.Value
import taack.ssh.SshEventRegistry
import crew.User

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@GrailsCompileStatic
final class IntercomUserPubKeyRetrieverService implements SshEventRegistry.UserPubKeyRetriever {

    static lazyInit = false

    @Value('${intranet.noSsh}')
    boolean noSsh

    @PostConstruct
    def initVfs() {
        log.info "initVfs start $noSsh"
        if (!noSsh) {
            SshEventRegistry.SshUserRegistrar.initUserPubKeyRetriever(this)
            SshEventRegistry.SshUserRegistrar.initSsh()
        }
        log.info "initVfs ends"
    }

    @PreDestroy
    def destroyVfs() {
        log.info "destroyVfs start"
        SshEventRegistry.SshUserRegistrar.destroySsh()
        log.info "destroyVfs ends"
    }

    @Override
    String userPubKeys(String username) {
        log.info "userPubKeys $username"
        User u = null
        String pk = null
        User.withNewSession {
            u = User.findByUsernameAndEnabled(username, true)
            if (u) {
                IntercomUser iu = IntercomUser.findByBaseUser(u)
                pk = iu.pubKeyContent
            }
        }
        return pk
    }
}
