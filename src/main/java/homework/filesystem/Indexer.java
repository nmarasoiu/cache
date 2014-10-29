package homework.filesystem;

import homework.option.Option;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllLines;

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
        Option<Path> optTail = readLink(tailLinkPath);
        if (optTail.isPresent()) {
            Path currentTail = optTail.get();
            Path rightLink = pathForSiblingLink(currentTail, SiblingDirection.RIGHT);
            persistLink(rightLink, entryDir);
        }
        setTail(entryDir);
    }

    private void removeFromLinkedList(Path entryDir) throws IOException {
        Option<Path> previousDir = getSibling(entryDir, SiblingDirection.LEFT);
        Option<Path> nextDir = getSibling(entryDir, SiblingDirection.RIGHT);

        writeSibling(entryDir, previousDir, SiblingDirection.RIGHT, nextDir);
        writeSibling(entryDir, nextDir, SiblingDirection.LEFT, previousDir);
    }

    private Option<Path> getSibling(Path entryDir, SiblingDirection siblingDirection) throws IOException {
        Path linkPath = pathForSiblingLink(entryDir, siblingDirection);
        return readLink(linkPath);
    }

    private Option<Path> readLink(Path linkPath) throws IOException {
        if (!exists(linkPath)) {
            return Option.empty();
        }
        return Option.of(fs.getPath(readAllLines(linkPath).get(0)));
    }


    private void writeSibling(Path entryDir, Option<Path> leftSiblingOption, SiblingDirection siblingDirection, Option<Path> rightSiblingOption) throws IOException {
        leftSiblingOption.ifPresent(leftSibling -> {
            Path linkPath =
                    pathForSiblingLink(leftSibling, siblingDirection);
            rightSiblingOption
                    .ifPresent(rightSibling ->
                            uncheckIOException(() ->
                                    persistLink(linkPath, rightSibling)))
                    .orElse(() -> {
                        if (exists(linkPath))
                            uncheckIOException(() ->
                                    delete(linkPath));
                    });
        }).orElse(() ->
                uncheckIOException(() -> {
                    if (siblingDirection == SiblingDirection.RIGHT) {
                        setHead(entryDir);
                    } else {
                        setTail(entryDir);
                    }
                }));
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
        if (!exists(destination.getParent())) {
            System.out.println();
        }
        Files.write(destination, Collections.singleton(value.toString()));
    }

}
