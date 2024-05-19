package intercom.ssh


import grails.compiler.GrailsCompileStatic
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import taack.ast.type.FieldInfo
import taack.domain.TaackJdbcService
import taack.ssh.SshEventRegistry
import taack.ssh.vfs.FileCallback
import taack.ssh.vfs.FileTree
import taack.ssh.vfs.impl.VfsPath
import taack.ssh.vfs.impl.VfsPosixFileAttributes
import crew.User

import javax.annotation.PostConstruct
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path

@GrailsCompileStatic
final class IntercomSshService implements SshEventRegistry.VfsEvent {

    static lazyInit = false

    private final String VFS_FILE_NAME = "intercom"

    @Value('${intranet.root}')
    String intranetRoot

    Path getTmpUploadFolder() {
        Path.of intranetRoot, 'intercom', 'tmp'
    }

    private final String queriesGroovyFileName = 'Queries.groovy'

    Path getCodeGeneratedQueriesPath() {
        Path.of tmpUploadFolder.toString(), queriesGroovyFileName
    }

    private File codeGeneratedQueries

    @PostConstruct
    def initVfs() {
        log.info "IntercomSshService::initVfs +++"
        FileUtils.forceMkdir(new File(tmpUploadFolder.toString()))
        try {
            Files.delete(codeGeneratedQueriesPath)
        } catch (e) {
            log.error "${e.message}"
        } finally {
            codeGeneratedQueries = codeGeneratedQueriesPath.toFile()
            codeGeneratedQueries << codeGenerator()
        }
        SshEventRegistry.VfsProvider.initVfsEventProvider(VFS_FILE_NAME, this)
        log.info "IntercomSshService::initVfs ---"
    }

    private static String codeGenerator() {
        StringBuffer output = new StringBuffer(2048)
        List<String> simpleNames = TaackJdbcService.Jdbc.fieldInfoMap.keySet()*.simpleName
        simpleNames.addAll(
                String.simpleName,
                BigDecimal.simpleName,
                Long.simpleName,
                Integer.simpleName,
                Float.simpleName,
                Double.simpleName,
                Character.simpleName,
                long.simpleName,
                int.simpleName,
                short.simpleName,
                Short.simpleName,
                Date.simpleName
        )
        TaackJdbcService.Jdbc.fieldInfoMap.sort {
            it.key.simpleName
        }.each {
            output.append("\n\npublic class ${it.key.simpleName} {\n")
            for (FieldInfo f : it.value) {
                String targetType = "String"
                if (simpleNames.contains(f.fieldConstraint.field.type.simpleName)) {
                    targetType = f.fieldConstraint.field.type.simpleName
                }
                output.append("    public ${targetType} ${f.fieldName};\n")
            }
            output.append("}")
        }
        return output.toString()
    }

    private final class IntercomCodegenFiles implements FileCallback {

        final User user

        IntercomCodegenFiles(User user) {
            this.user = user
        }

        @Override
        VfsPosixFileAttributes onGetFileAttributes(FileTree fileTree, FileTree.File file) {
            log.trace "CrewCsvFiles onGetFileAttributes $fileTree ${file.fileName}"
            if (file.fileName == queriesGroovyFileName) {
                file.realFilePath = codeGeneratedQueries.path
                return file.getAttributes(0)
            }
            log.warn "onGetFileAttributes for file ${file.fileName} fails"
            return null
        }

        @Override
        SeekableByteChannel onFileOpen(FileTree fileTree, FileTree.File file, String s, Set<? extends OpenOption> options) {
            log.trace "CrewCsvFiles onFileOpen $fileTree ${file.fileName} $options"

            if (file.fileName == queriesGroovyFileName) {
                return Files.newByteChannel(codeGeneratedQueriesPath)
            }
            log.warn "onGetFileAttributes for file ${file.fileName} fails"
            return null
        }

        @Override
        void onFileClose(FileTree fileTree, FileTree.File file, String s) {

        }

        @Override
        void onRenameFile(FileTree fileTree, FileTree.File file, VfsPath vfsPath, VfsPath vfsPath1) {

        }
    }

    @Override
    FileTree initVfsAppEvent(String username) {
        log.info "initVfsAppEvent $username on Crew"
        User.withNewSession {
            try {
                User u = User.findByUsernameAndEnabled(username, true)
                if (u) {
                    FileTree fs = new FileTree(username)
                    IntercomCodegenFiles intercomCodegenFiles = new IntercomCodegenFiles(u)
                    fs.root = fs.createBuilder(VFS_FILE_NAME)
                            .addFiles(intercomCodegenFiles, false, queriesGroovyFileName)
                            .toFolder()
                    return fs
                }
            } catch(e) {
                log.error "cannot create FileTree: ${e.message}"
                e.printStackTrace()
                throw e
            }

            log.warn "initVfsAppEvent failed for $username"
            return null
        }
    }

    @Override
    void closeVfsConnection(String username) {
        log.info "closeVfsConnection $username"
    }
}
