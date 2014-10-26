package homework.filesystem;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;

/**
 * Manages 2 double linked lists, remembering the order of access, one for read and one for write.
 * The linked list is kept at filesystem level by means of files.
 * Each "link" is a file holding the path to an entry dir. An entry dir is base/hash/number.
 */
public class Indexer {
    protected final FileSystem fs;
    protected final Path basePath;

    public Indexer(Path basePath) {
        this.basePath = basePath;
        this.fs = this.basePath.getFileSystem();
    }

    protected final Map<EndType, Map<IndexType, Optional<Path>>> ends = getEndTypeMapHashMap();


    private void reindex(Optional<Path> entryDirOptional) {
        if (entryDirOptional.isPresent()) {
            Path entryDir = entryDirOptional.get();
//            rearrangeLinkedList(entryDir);
        } else {

        }
    }

    public void reindexAfterGet(Optional<Path> entryDirOptional) {

    }


    private void rearrangeLinkedList(Path entryDir) {
        //put the entry at the end of r/w access queue: link the prev to the next, and replace the endPointer
        rearrangeLinkedList(entryDir, IndexType.READ);
    }

    private void rearrangeLinkedList(Path entryDir, IndexType indexType) {
        removeFromLinkedList(entryDir, indexType);
        tails().get(indexType).ifPresent(tail -> {

        });
//            tails.put(indexType, Optional.of())
    }

    private void removeFromLinkedList(Path entryDir, IndexType indexType) {
        Path previousDir = getSibling(entryDir, SiblingType.LEFT, indexType);
        Path nextDir = getSibling(entryDir, SiblingType.RIGHT, indexType);
        writeSibling(previousDir, SiblingType.RIGHT, indexType, nextDir);
    }

    private void writeSibling(Path entryDir, SiblingType siblingType, IndexType indexType, Path siblingPath) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        uncheckIOException(() ->
                        write(linkPath, Collections.singleton(siblingPath.toString()))
        );
    }

    private Path getSibling(Path entryDir, SiblingType siblingType, IndexType indexType) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        return uncheckIOException(() ->
                        fs.getPath(readAllLines(linkPath).get(0))
        );
    }

    private Path pathForSiblingLink(Path entryDir, SiblingType siblingType, IndexType indexType) {
        return entryDir.resolve(siblingType.name() + "_" + indexType.name());
    }

    private HashMap<EndType, Map<IndexType, Optional<Path>>> getEndTypeMapHashMap() {
        HashMap<EndType, Map<IndexType, Optional<Path>>> m = new HashMap<>();
        m.put(EndType.HEAD, initialTails());
        m.put(EndType.TAIL, initialTails());
        return m;
    }

    private HashMap<IndexType, Optional<Path>> initialTails() {
        HashMap<IndexType, Optional<Path>> m = new HashMap<>();
        m.put(IndexType.READ, Optional.<Path>empty());
        m.put(IndexType.WRITE, Optional.<Path>empty());
        return m;
    }

    private Map<IndexType, Optional<Path>> heads() {
        return ends.get(EndType.HEAD);
    }

    private Map<IndexType, Optional<Path>> tails() {
        return ends.get(EndType.TAIL);
    }
}
