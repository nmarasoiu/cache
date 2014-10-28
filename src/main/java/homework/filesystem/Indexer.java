package homework.filesystem;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static java.nio.file.Files.*;

/**
 * Manages 2 double linked lists, remembering the order of access, one for read and one for write.
 * The linked list is kept at filesystem level by means of files.
 * Each "link" is a file holding the path to an entry dir. An entry dir is base/hash/number.
 */
public class Indexer {
    public static final String TAIL_FILENAME = "tail", HEAD_FILENAME = "head";
    private final FileSystem fs;
    private final Path basePath;
    private final IndexType indexType;
    private final Path headLinkPath, tailLinkPath;

    public Indexer(IndexType indexType, Path basePath) {
        this.indexType = indexType;
        this.basePath = basePath;
        this.fs = this.basePath.getFileSystem();
        headLinkPath = basePath.resolve(HEAD_FILENAME);
        tailLinkPath = basePath.resolve(TAIL_FILENAME);
    }

    public void touch(Path entryDir) throws IOException {
        moveAtTheEnd(entryDir);
    }

    private void moveAtTheEnd(Path entryDir) throws IOException {
        //put the entry at the end of r/w access queue: link the prev to the next
        removeFromLinkedList(entryDir);
        append(entryDir);
    }

    private void append(Path entryDir) throws IOException {
        Optional<Path> optTail = readLink(tailLinkPath);
        if(optTail.isPresent()){
            Path currentTail = optTail.get();
            Path rightLink = pathForSiblingLink(currentTail, SiblingDirection.RIGHT);
            persistLink(rightLink, entryDir);
        }
        setTail(entryDir);
    }

    private void removeFromLinkedList(Path entryDir) throws IOException {
        Optional<Path> previousDir = getSibling(entryDir, SiblingDirection.LEFT);
        Optional<Path> nextDir = getSibling(entryDir, SiblingDirection.RIGHT);

        writeSibling(entryDir, previousDir, SiblingDirection.RIGHT, nextDir);
        writeSibling(entryDir, nextDir, SiblingDirection.LEFT, previousDir);
    }

    private Optional<Path> getSibling(Path entryDir, SiblingDirection siblingDirection) throws IOException {
        Path linkPath = pathForSiblingLink(entryDir, siblingDirection);
        return readLink(linkPath);
    }

    private Optional<Path> readLink(Path linkPath) throws IOException {
        if (!exists(linkPath)) {
            return Optional.empty();
        }
        return Optional.of(fs.getPath(readAllLines(linkPath).get(0)));
    }


    private void writeSibling(Path entryDir, Optional<Path> leftSibling, SiblingDirection siblingDirection, Optional<Path> rightSibling) throws IOException {
        if (leftSibling.isPresent()) {
            Path linkPath = pathForSiblingLink(leftSibling.get(), siblingDirection);
            if (!rightSibling.isPresent()) {
                if(exists(linkPath)) delete(linkPath);
            } else {
                persistLink(linkPath, rightSibling.get());
            }
        } else {
            if (siblingDirection == SiblingDirection.RIGHT) {
                setHead(entryDir);
            } else {
                setTail(entryDir);
            }
        }
    }

    private Path pathForSiblingLink(Path entryDir, SiblingDirection siblingDirection) {
        return entryDir.resolve(siblingFilename(siblingDirection));
    }

    private String siblingFilename(SiblingDirection siblingDirection) {
        return siblingDirection.name() + "_" + indexType.name();
    }

    private void setHead(Path entryPath) throws IOException {
        persistLink(headLinkPath, entryPath);
    }

    private void setTail(Path entryPath) throws IOException {
        persistLink(tailLinkPath, entryPath);
    }

    private void persistLink(Path destination, Path value) throws IOException {
        if(!exists(destination.getParent())){
            System.out.println();
        }
        Files.write(destination, Collections.singleton(value.toString()));
    }

}
